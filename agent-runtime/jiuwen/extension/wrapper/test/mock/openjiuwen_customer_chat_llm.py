import ast
import asyncio
import json
import os
import random
import re
import time
from typing import List, Optional, Any, AsyncIterable, Union

import aiohttp
import jieba
import yaml
from jiuwen.common.net import Connector
from openjiuwen.core.common.clients.client_registry import get_client_registry
from openjiuwen.core.common.logging import llm_logger

# 导入必要的openjiuwen模块
from openjiuwen.core.foundation.llm.model_clients.base_model_client import (
    BaseModelClient,
)
from openjiuwen.core.foundation.llm.schema.config import (
    ModelRequestConfig,
    ModelClientConfig,
)
from openjiuwen.core.foundation.llm.schema.generation_response import (
    ImageGenerationResponse,
    AudioGenerationResponse,
    VideoGenerationResponse,
)
from openjiuwen.core.foundation.llm.schema.message import (
    AssistantMessage,
    BaseMessage,
    UserMessage,
)
from openjiuwen.core.foundation.llm.schema.message_chunk import AssistantMessageChunk
from openjiuwen.core.foundation.tool import ToolInfo

# 读取yaml中的mock预期结果
current_dir = os.path.dirname(os.path.abspath(__file__))
mock_data_path = os.path.join(current_dir, "mock_data.yaml")
with open(mock_data_path, "r", encoding="utf-8") as file:
    data = yaml.safe_load(file)
mock_data: dict = {}
for k, v in data.items():
    value = {"mock_index": 0, "mock_result": v}
    mock_data.update({str(k): value})

# 读取意图识别yaml中的mock预期结果
mock_intention_path = os.path.join(current_dir, "mock_intention.yaml")
with open(mock_intention_path, "r", encoding="utf-8") as file:
    intention_data = yaml.safe_load(file)

# 读取mock deepseek prompt自优化
deepseek_data_path = os.path.join(current_dir, "mock_deepseek.jsonl")
with open(deepseek_data_path, "r", encoding="utf-8") as f:
    deepseek_data = [line.strip() for line in f]
deepseek_count = 0

deepseek_prompt_list = [
    "你是一个信息收集助手",
    "你是一个答案校验专家",
    "您将获得一个任务描述和指令",
    "作为提示词优化专家",
    "你是一位提示词优化专家",
    "你是一名提示词优化专家",
    "你好，我是智能助手",
]

# 读取mock deepseek prompt自优化
mutil_modal_data_path = os.path.join(current_dir, "mock_mutil_modal.jsonl")
with open(mutil_modal_data_path, "r", encoding="utf-8") as f:
    mutil_modal_data = [ast.literal_eval(line.strip()) for line in f]
mutil_modal_count = 0
mutil_modal_prompt_list = [
    "你是一位专业的图像分析和知识解答助手",
    "你的任务是根据提供的图片、文本问题和文本选项进行解答。",
]

time.sleep(0)  # 时延


def get_mock_result(query, default_result="mock LLM"):
    """根据query获取mock_result"""
    mock_index_str = "mock_index"
    # agent调用workflow
    if "调用" in re.findall("^.{2}", query) and "输入" in query:
        query = re.findall("输入(.*)", query)[0]
    # 正常流程
    if query in mock_data:
        query_data = mock_data.get(query)
        try:
            answer = query_data.get("mock_result")[query_data.get(mock_index_str)]
        except Exception:
            mock_data[query][mock_index_str] = 0
            answer = query_data.get("mock_result")[query_data.get(mock_index_str)]
        mock_data[query][mock_index_str] += 1
    else:
        if "日期是" in query:
            answer = f'{{"time": "{query.replace("日期是", "")}"}}'  # 护栏指定query
        else:
            answer = default_result
    return answer


def get_scene(query):
    """根据query获取场景"""
    answer = "none"
    scenes = {
        "帮我还一下信用卡": "scene_credit_repay",
        "查询kpi数据": "scene_analyse_kpi",
        "先转账，再赎回理财": "scene_manage_money",
        "先赎回理财，再转账": "scene_manage_money",
        "赎回理财两次": "scene_manage_money",
    }
    for key_word, scene in scenes.items():
        if key_word in query:
            answer = scene
            break
    return answer


def get_intention_result(query):
    """根据query获取intention_result"""
    answer = 1
    # agent调用workflow
    if "调用" in re.findall("^.{2}", query) and "输入" in query:
        query = re.findall("输入(.*)", query)[0]
    # 正常流程
    if query in intention_data:
        try:
            answer = intention_data.get(query, [])[0]
        except Exception as e:
            llm_logger.error(f"Failed to get intention result: {e}")
    # 使用re.match检查answer是否符合定义的模式
    if isinstance(answer, int):
        # 如果匹配成功，返回指定格式的结果
        return f'{{"result": "{answer}", "reason": "mock 理由"}}'
    # 如果不匹配，直接返回answer
    return answer


def get_expect_param(_type, _value):
    """将参数转换为预期类型"""
    if _type == "string":
        func = str
    elif _type == "integer":
        func = int
    elif _type == "number":
        func = float
    elif _type == "boolean":
        func = bool
    else:
        func = json.loads

    try:
        return func(_value)
    except Exception as e:
        raise e


class MockModelClient(BaseModelClient):
    """适配openjiuwen的Mock Model Client实现"""

    __client_name__ = "Mock"
    __client_type__ = "llm"

    def __init__(
        self, model_config: ModelRequestConfig, model_client_config: ModelClientConfig
    ):
        super().__init__(model_config, model_client_config)
        self.expect_prompt = (
            "##人设\n您好，我是一个专门的知识问答助手：mock prompt，拥有大量的知识库和信息检索能力。"
            "我的特长是能够快速准确地回答各种问题，不论是学术问题还是日常生活中的问题。\n\n##任务描述\n"
            "我旨在为用户提供准确、及时的知识问答服务，解答用户在学习、工作和生活中遇到的各种问题。"
            "我预期对用户的积极影响是提高他们的知识水平，帮助他们解决问题，提升他们的生活和工作效率。\n\n"
            "##约束条件\n1.我将按照您的问题，进行准确的回答。\n2.我的回答将以文字的形式呈现。\n"
            "3.我将在回答中，避免使用过于复杂的专业术语，以便于用户理解。\n"
            "4.我将在回答中，尽量提供详细的解答，包括问题的背景、原因、解决方法等。\n\n"
            "##执行步骤\n1.接收到用户的问题后，我将通过我的知识库和信息检索能力，寻找最准确的答案。\n"
            "2.在找到答案后，我将以易于理解的方式，将答案呈现给用户。\n3.如果用户对答案有疑问或者不满意，我将继续提供服务，"
            "直到用户满意为止。\n\n##输出格式\n1.我的回答将以您好，您好开头，以示尊重。\n2.我的回答将以文字的形式呈现，"
            "避免使用过于复杂的专业术语。\n3.我的回答将尽量详细，包括问题的背景、原因、解决方法等。"
        )

    def _validate_config(self):
        """重写验证方法，mock模型不需要api_key和api_base"""
        # 跳过父类的验证，因为mock模型不需要实际的API配置
        pass

    def get_mock_url(self):
        """根据modelName获取mock大模型的url"""
        if self.model_config.model_name == "mock_model_cut":
            return "http://fakeIp:5379/api/chat_cut"
        return "http://fakeIp:5379/api/chat"

    async def invoke(
        self,
        messages: Union[str, List[BaseMessage], List[dict]],
        *,  # 强制使用关键字参数
        tools: Union[List[ToolInfo], List[dict], None] = None,
        temperature: Optional[float] = None,
        top_p: Optional[float] = None,
        model: str = None,
        max_tokens: Optional[int] = None,
        stop: Union[Optional[str], None] = None,
        output_parser: Optional[Any] = None,
        timeout: float = None,
        **kwargs,
    ) -> AssistantMessage:
        """模型调用接口"""
        global deepseek_count, mutil_modal_count

        llm_logger.info(f"MockModelClient invoke messages: {messages}")
        llm_logger.info(f"MockModelClient invoke tools: {tools}")
        llm_logger.info(f"MockModelClient invoke kwargs: {kwargs}")

        # 转换消息格式
        messages_dict = self._convert_messages_to_dict(messages)

        # 处理 Questioner 场景（优先于 deepseek 提示词优化检查）
        # Questioner 系统模板包含 "Specified Parameters"，借此与 deepseek 提示词区分
        if (
            messages_dict
            and len(messages_dict) == 2
            and messages_dict[0]["role"] == "system"
            and messages_dict[1]["role"] == "user"
            and "Specified Parameters" in messages_dict[0]["content"]
        ):
            # 从对话历史中提取用户消息
            user_inputs = re.findall(r"user[：:]\s*(.+)", messages_dict[-1]["content"])
            full_text = " ".join(user_inputs)

            # 从系统消息中解析指定参数名
            param_names = set()
            for line in messages_dict[0]["content"].split("\n"):
                line = line.strip()
                param_match = re.match(r"^(\w+):\s+\S", line)
                if param_match:
                    param_names.add(param_match.group(1))

            # 基于规则的字段提取
            field_patterns = {
                "tel": r"(?:电话|手机|号码)[是为：：]*(\d+)",
                "time": r"故障发生[在是为：：]*(.+?)(?:，|,|$)",
                "switch": r"(?:移动)?开关[是为：：]*(开启|关闭|开|关)",
                "signals": r"(?:车机端)?(有|无)(?:显示)?信号",
                "flow_rate": r"(?:车机端)?(有|无)(?:剩余)?流量",
            }

            result = {}
            for fname, pattern in field_patterns.items():
                if fname in param_names:
                    match = re.search(pattern, full_text)
                    if match:
                        result[fname] = match.group(1).strip()

            # 未匹配到的参数设为 null
            for fname in param_names:
                if fname not in result:
                    result[fname] = None

            return AssistantMessage(content=json.dumps(result, ensure_ascii=False))

        # 处理deepseek prompt自优化
        if messages_dict and messages_dict[0]["role"] == "system":
            if any(s in messages_dict[0]["content"] for s in deepseek_prompt_list):
                content = deepseek_data[deepseek_count]
                if deepseek_count == len(deepseek_data) - 1:
                    deepseek_count = 0
                else:
                    deepseek_count += 1
                return AssistantMessage(content=content)

        # 处理工具调用返回
        if messages_dict and messages_dict[-1]["role"] == "tool":
            tool_content = str(messages_dict[-1]["content"])
            # Agent 工具调用链：calculator_tool 返回后 → 调用 mock_plugin_add
            if "啦啦啦" in tool_content:
                return AssistantMessage(content="18啦啦啦")
            if "'18'" in tool_content or '"18"' in tool_content:
                from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

                return AssistantMessage(
                    content="",
                    tool_calls=[
                        ToolCall(
                            id="call_1",
                            type="function",
                            name="mock_plugin_add",
                            arguments='{"query": "18"}',
                        )
                    ],
                )
            answer = messages_dict[-1]["content"]
            return AssistantMessage(content=answer)

        # mock 多模态提示词优化
        if messages_dict and messages_dict[0]["role"] == "user":
            try:
                # 尝试解析多模态消息
                if isinstance(messages_dict[0]["content"], list):
                    text = messages_dict[0]["content"][0]["text"]
                else:
                    text = messages_dict[0]["content"]

                if any(s in text for s in mutil_modal_prompt_list):
                    content = mutil_modal_data[mutil_modal_count]
                    llm_logger.info(
                        f"【content】：\n{content}\n【mutil_modal_count】：{mutil_modal_count}"
                    )
                    if mutil_modal_count == len(mutil_modal_data) - 1:
                        mutil_modal_count = 0
                    else:
                        mutil_modal_count += 1
                    time.sleep(random.randint(3, 6))
                    return AssistantMessage(content=content)
            except Exception as e:
                llm_logger.error(f"Failed to handle multi-modal message: {e}")

        # 处理记忆引擎场景
        if messages_dict and len(messages_dict) > 1:
            if (
                messages_dict[0]["role"] == "system"
                and "你是一个记忆引擎" in messages_dict[0]["content"]
            ):
                if messages_dict[-1]["role"] == "user":
                    match = re.findall(r"user: (.*)\\n", messages_dict[-1]["content"])
                    if match:
                        query = match[0]
                        answer = get_mock_result(query, "mock memory")
                        answer = answer.replace("\\n", "\n")
                        return AssistantMessage(content=answer)

            # 处理信息分析专家场景
            if (
                messages_dict[0]["role"] == "system"
                and "你是一个信息分析专家" in messages_dict[0]["content"]
            ):
                if messages_dict[-1]["role"] == "user":
                    match = re.findall(
                        r"BaseMessage\(type='user', content='(.*)', name=None",
                        messages_dict[-1]["content"],
                    )
                    if match:
                        query = match[0]
                        answer = get_mock_result(query, "mock memory")
                        answer = answer.replace("\\n", "\n")
                        return AssistantMessage(content=answer)

        # 处理特定请求场景
        if messages_dict and messages_dict[-1]["role"] == "user":
            content = messages_dict[-1]["content"]
            if "我要坐飞机去上海参加会议" in content:
                return AssistantMessage(
                    content='{"location":"上海","traveltool":"飞机"}'
                )

        # 处理意图分类器场景
        if (
            messages_dict
            and messages_dict[0]["role"] == "system"
            and '你是"意图分类器"' in messages_dict[0]["content"]
        ):
            if messages_dict[-1]["role"] == "user":
                match = re.findall("当前输入\n(.*)", messages_dict[-1]["content"])
                if match:
                    query = match[0]
                    answer = get_mock_result(query, "意图不明")
                    if '"result"' not in answer:
                        intention = answer
                        answer = '{"result": "1"}'
                        classification = {
                            f"分类{num}": name
                            for num, name in re.findall(
                                r"分类(\d+):\s*([^\n]+)", messages_dict[0]["content"]
                            )
                        }
                        for cls, name in classification.items():
                            if intention in name:
                                answer = f'{{"result": "{cls.split("分类")[-1]}"}}'
                                break
                    return AssistantMessage(content=answer)

        # 处理多步骤执行判断场景
        if (
            messages_dict
            and messages_dict[0]["role"] == "system"
            and "判断用户请求是否需要多步骤执行" in messages_dict[0]["content"]
        ):
            if messages_dict[-1]["role"] == "user":
                match = re.findall("当前请求：(.*)", messages_dict[-1]["content"])
                if match:
                    query = match[0]
                    answer = get_mock_result(query, "")
                    if "steps" in answer:
                        answer = "1"
                    else:
                        answer = "0"
                    return AssistantMessage(content=answer)

        # 处理场景匹配助手场景
        if (
            messages_dict
            and len(messages_dict) > 1
            and messages_dict[0]["role"] == "system"
            and "你是一个场景匹配助手" in messages_dict[0]["content"]
        ):
            if messages_dict[1]["role"] == "user":
                match = re.findall("用户请求\n\n(.*)", messages_dict[1]["content"])
                if match:
                    query = match[0]
                    answer = get_scene(query)
                    return AssistantMessage(content=answer)

        # 处理任务规划助手场景
        if (
            messages_dict
            and len(messages_dict) > 1
            and messages_dict[0]["role"] == "system"
            and "你是一个任务规划助手" in messages_dict[0]["content"]
        ):
            if messages_dict[1]["role"] == "user":
                match = re.findall("用户请求\n\n(.*)", messages_dict[1]["content"])
                if match:
                    query = match[0]
                    answer = get_mock_result(query, "")
                    answer = answer.replace("\\n", "\n")
                    return AssistantMessage(content=answer)

        # Agent 工具调用场景（invoke 路径）
        if (
            messages_dict
            and messages_dict[-1]["role"] == "user"
            and "先调用calculator计算9+9" in messages_dict[-1]["content"]
        ):
            from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

            content = messages_dict[-1]["content"]
            if (
                content
                == "先调用calculator计算9+9，再将计算结果调用插件mock_plugin_add，返回结果"
            ):
                return AssistantMessage(
                    content="",
                    tool_calls=[
                        ToolCall(
                            id="call_0",
                            type="function",
                            name="calculator_tool",
                            arguments='{"expression": "9+9"}',
                        )
                    ],
                )

        # 处理不同场景
        if len(messages_dict) == 2 and messages_dict[0]["role"] == "system":
            # 处理控制器或提问器场景
            if (
                "Perform variable value extraction and return results as a valid JSON object"
                in messages_dict[-1]["content"]
            ):
                # 记忆变量
                query = re.findall(
                    r"<Conversation>[\r\n]*\s*(.*?)\s*[\r\n]*</Conversation>",
                    messages_dict[-1]["content"],
                    re.DOTALL,
                )[0]
                answer = get_mock_result(query, "mock memory")
                time.sleep(2)  # 注意：该sleep为处理异步逻辑
            else:
                # mock控制器不指定顺序
                # 还可能是questioner_reflection。这个不支持，需要把提问器参数的关掉
                # 提取分类信息，分类信息为activeWorkflows中配置的workflow，ir中的description
                classification = {
                    f"分类{num}": name
                    for num, name in re.findall(
                        r"分类(\d+):\s*([^\n]+)", messages_dict[-1]["content"]
                    )
                }
                # 提取当前输入
                current_input_match = re.search(
                    r"当前输入：\n([^\n]+)", messages_dict[-1]["content"]
                )
                if not current_input_match:
                    return AssistantMessage(content="mock LLM response")
                current_input = current_input_match.group(1)
                # 根据用户输入匹配分类
                answer = '{"class": "分类1"}'
                for cls, name in classification.items():
                    if name in current_input:
                        answer = f'{{"class": "{cls}"}}'
                        break
            return AssistantMessage(content=answer)

        # 处理普通消息
        if messages_dict:
            last_message = messages_dict[-1]
            if last_message["role"] == "user":
                content = last_message["content"]

                # 检查是否包含特定标签
                label = re.findall("以下是(.*)的元模板", content)
                if label:
                    label = label[0]

                    if label == "控制器":
                        # mock 控制器
                        query = re.findall("当前输入：\n(.*)\n\n请根据", content)[0]
                        answer = get_mock_result(query, "mock_controller")
                        if not bool(
                            re.fullmatch(r'^\{\s*"class"\s*:\s*"分类\d".*?\}$', answer)
                        ):
                            # 多agent场景，一个query需要多次判断意图，通过正则去匹配
                            intention = answer
                            answer = '{"class": "分类1"}'
                            classification = {
                                f"分类{num}": name
                                for num, name in re.findall(
                                    r"分类(\d+):\s*([^\n]+)", content
                                )
                            }
                            for cls, name in classification.items():
                                if intention in name:
                                    answer = f'{{"class": "{cls}"}}'
                                    break
                    elif label == "提问器":
                        # mock 提问器
                        query = re.findall(
                            "用户输入： (.*)''；required parameter", content
                        )[0]
                        if query == "prompt":
                            answer = json.dumps(
                                {"prompt": messages_dict[0]["content"]}
                            )  # 特殊场景，返回提问器的prompt
                        elif query == "raise Exception":
                            raise Exception("模拟抛出异常")
                        else:
                            answer = get_mock_result(query, "mock_questioner")
                    elif label == "意图识别":
                        # mock 意图识别
                        try:
                            query = re.findall("用户：(.*)\n", content)[
                                -1
                            ]  # 从对话历史里面取
                        except Exception:
                            query = re.findall("当前输入：\n(.*?)\n", content)[
                                -1
                            ]  # 从用户变量里面取
                        answer = get_intention_result(query)
                    elif label == "markdown":
                        # mock prompt
                        answer = self.expect_prompt
                    else:
                        # mock LLM
                        query = content.split("\n")[-1]
                        answer = get_mock_result(query, "mock LLM")
                else:
                    # mock LLM
                    query = content.split("\n")[-1]
                    answer = get_mock_result(query, "mock LLM")

                # 确保query变量有定义
                query = query if "query" in locals() else "未知query"
                llm_logger.warning(
                    f"当前query为：{query}，label为：{label}，answer为：{answer}"
                )

                # 调用外部mock URL时添加异常处理
                try:
                    connector = Connector().get_tcp_connector()
                    async with aiohttp.ClientSession(
                        connector=connector, connector_owner=connector is None
                    ) as session:
                        await session.post(
                            self.get_mock_url(),
                            json={"prompt": answer, "stream": False},
                        )
                except Exception as e:
                    llm_logger.error(f"Failed to call mock URL: {e}")

            return AssistantMessage(content=answer)

        # 默认返回
        return AssistantMessage(content="mock LLM response")

    async def stream(
        self,
        messages: Union[str, List[BaseMessage], List[dict]],
        *,  # 强制使用关键字参数
        tools: Union[List[ToolInfo], List[dict], None] = None,
        temperature: Optional[float] = None,
        top_p: Optional[float] = None,
        model: str = None,
        max_tokens: Optional[int] = None,
        stop: Union[Optional[str], None] = None,
        output_parser: Optional[Any] = None,
        timeout: float = None,
        **kwargs,
    ) -> AsyncIterable[AssistantMessageChunk]:
        """模型流式调用接口"""
        llm_logger.info(f"MockModelClient stream messages: {messages}")
        llm_logger.info(f"MockModelClient stream tools: {tools}")
        llm_logger.info(f"MockModelClient stream kwargs: {kwargs}")

        # 转换消息格式
        messages_dict = self._convert_messages_to_dict(messages)

        # 智能任务执行助手场景
        if (
            messages_dict
            and messages_dict[0]["role"] == "system"
            and "你是一个智能任务执行助手" in messages_dict[0]["content"]
        ):
            if messages_dict[-1]["role"] == "user":
                content = messages_dict[-1]["content"]
                tools_calls = []
                answer = ""

                if "所有步骤已完成" in content:
                    # 直接调用llm做总结
                    if len(messages_dict) > 2:
                        answer = get_mock_result(
                            messages_dict[-2]["content"], "所有步骤已完成"
                        )
                    else:
                        answer = "所有步骤已完成"
                elif "步：查询信用卡账单" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        if "工具执行失败" in messages_dict[-2]["content"]:
                            answer = "请提供您的信用卡ID哟 [ASK_USER]"
                        else:
                            answer = "[STEP_DONE]"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="parent_workflow_plugin_query_bill",
                                arguments='{"card_id": "123456"}',
                            )
                        ]
                elif "步：查询账户余额" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "[STEP_DONE]"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="workflow_plugin_query_account",
                                arguments='{"account_id": "555444"}',
                            )
                        ]
                elif "步：动态判断" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "余额不足，转账完成[STEP_DONE]"
                    else:
                        try:
                            if len(messages_dict) > 3:
                                account = float(
                                    re.findall(
                                        "账户余额为：(.*)", messages_dict[-3]["content"]
                                    )[-1]
                                )
                                if account >= 500.0:
                                    answer = "余额充足，无需转账[STEP_DONE]"
                                    from openjiuwen.core.foundation.llm.schema.tool_call import (
                                        ToolCall,
                                    )

                                    tools_calls = [
                                        ToolCall(
                                            id="call_0",
                                            type="function",
                                            name="workflow_plugin_transfer",
                                            arguments='{"from_account": "999888", "to_account": "999888", "amount": 0.1}',
                                        )
                                    ]
                        except Exception as e:
                            llm_logger.error(f"Dynamic judge error: {e}")
                            from openjiuwen.core.foundation.llm.schema.tool_call import (
                                ToolCall,
                            )

                            tools_calls = [
                                ToolCall(
                                    id="call_0",
                                    type="function",
                                    name="workflow_plugin_transfer",
                                    arguments='{"from_account": "999888", "to_account": "999888", "amount": 0.1}',
                                )
                            ]
                elif "步：确认还款" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "信用卡还款任务已完成"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="parent_workflow_plugin_repay_credit",
                                arguments='{"card_id": "whatever"}',
                            )
                        ]
                elif "步：查询kpi数据" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "[STEP_DONE]"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="workflow_kpi",
                                arguments='{"query": "whatever"}',
                            )
                        ]
                elif "步：分析kpi数据" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "[STEP_DONE]"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="analyse_kpi",
                                arguments='{"kpi": 800}',
                            )
                        ]
                elif "步：发送邮件" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "[STEP_DONE]"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="send_mail",
                                arguments='{"mail_id": "abc"}',
                            )
                        ]
                elif "步：转账汇款" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "[STEP_DONE]"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="转账汇款",
                                arguments='{"query": "whatever"}',
                            )
                        ]
                elif "步：赎回理财" in content:
                    if len(messages_dict) > 2 and messages_dict[-2]["role"] == "tool":
                        answer = "[STEP_DONE]"
                    else:
                        from openjiuwen.core.foundation.llm.schema.tool_call import (
                            ToolCall,
                        )

                        tools_calls = [
                            ToolCall(
                                id="call_0",
                                type="function",
                                name="理财服务",
                                arguments='{"query": "whatever"}',
                            )
                        ]
                else:
                    answer = "所有步骤已完成"

                if tools_calls:
                    yield AssistantMessageChunk(content="", tool_calls=tools_calls)
                else:
                    yield AssistantMessageChunk(content=answer)
                return

        # 处理工具调用场景
        if messages_dict and messages_dict[-1]["role"] == "user":
            content = messages_dict[-1]["content"]

            if "开启思考模式" in content:
                # 带有思考内容的流式输出
                yield AssistantMessageChunk(
                    content="mock streaming", reasoning_content="这是思考内容,啦啦啦"
                )
            elif (
                "先调用calculator计算9+9，再将计算结果调用插件mock_plugin_add，返回结果"
                in content
            ):
                # agent组件
                if (
                    messages_dict[-1]["content"]
                    == "先调用calculator计算9+9，再将计算结果调用插件mock_plugin_add，返回结果"
                ):
                    # 调用第一个插件
                    from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="calculator_tool",
                        arguments='{"expression": "9+9"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "18" in content:
                    # 调用第二个插件
                    from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="mock_plugin_add",
                        arguments='{"query": "18"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                else:
                    yield AssistantMessageChunk(content="18啦啦啦")
            elif (
                "先调用get_now_weather输入深圳，在调用workflow_react_tools，输出结果。"
                in content
            ):
                # agent组件
                from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

                if (
                    messages_dict[-1]["content"]
                    == "先调用get_now_weather输入深圳，在调用workflow_react_tools，输出结果。"
                ):
                    # 调用第一个插件
                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="get_now_weather",
                        arguments='{"location": "深圳"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "深圳今日天气晴朗" in content:
                    # 调用工作流
                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="workflow_react_tools",
                        arguments='{"query": "深圳今日天气晴朗"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "深圳今日天气晴朗气温20度" in content:
                    # 输出结果
                    yield AssistantMessageChunk(content="深圳今日天气晴朗气温20度")
                else:
                    yield AssistantMessageChunk(content="get_now_weather插件执行失败")
            elif "先调用get_now_weather输入北京，在调用workflow_react_f" in content:
                # agent组件
                from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

                if (
                    messages_dict[-1]["content"]
                    == "先调用get_now_weather输入北京，在调用workflow_react_f"
                ):
                    # 调用第一个插件
                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="get_now_weather",
                        arguments='{"location": "北京"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "北京今日天气晴朗" in content:
                    # 调用工作流
                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="workflow_react_f",
                        arguments='{"query": "北京今日天气晴朗"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "北京今日天气晴朗子工作流" in content:
                    # 输出结果
                    yield AssistantMessageChunk(content="北京今日天气晴朗子工作流")
                else:
                    yield AssistantMessageChunk(content="插件执行失败")
            elif (
                "先调用calculator计算9+9在调用workflow_react_tools，最后调用插件mock_plugin_add，返回结果"
                in content
            ):
                # 多步骤agent组件
                from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

                if (
                    messages_dict[-1]["content"]
                    == "先调用calculator计算9+9在调用workflow_react_tools，最后调用插件mock_plugin_add，返回结果"
                ):
                    # 调用第一个插件
                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="calculator_tool",
                        arguments='{"expression": "9+9"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "18" in content and "气温20度" not in content:
                    # 调用工作流
                    tool_call = ToolCall(
                        name="workflow_react_tools", arguments='{"query": "18"}'
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "18气温20度" in content:
                    # 调用第三个插件
                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name="mock_plugin_add",
                        arguments='{"query": "18气温20度"}',
                    )
                    yield AssistantMessageChunk(content="", tool_calls=[tool_call])
                elif "18气温20度啦啦啦" in content:
                    # 输出结果
                    yield AssistantMessageChunk(content="18气温20度啦啦啦")
                else:
                    yield AssistantMessageChunk(content="插件执行失败")
            elif tools is None:
                # 使用jieba分词的mock prompt
                answer = jieba.cut(self.expect_prompt, cut_all=False)
                for word in answer:
                    await asyncio.sleep(0.1)
                    yield AssistantMessageChunk(content=word)
            elif tools and "调用" in re.findall("^.{2}", str(content)):
                # mock知识型agent调用工具
                try:
                    tool = re.findall("调用(.*)输入", content)[0]
                    tool_dict = next(
                        (i for i in tools if tool == i.get("name")), tools[0]
                    )
                    parameter_list = list(
                        tool_dict.get("parameters").get("properties").keys()
                    )
                    if "required" in parameter_list:
                        parameter_list.remove("required")
                    parameter = parameter_list[-1]  # 优先给用户变量赋值

                    # 判断是否为工作流异常结束组件变量赋值
                    required_keys = [
                        "query",
                        "error_code",
                        "error_message",
                        "error_code_2",
                        "error_message_2",
                    ]
                    if len(parameter_list) == 5 and all(
                        key for key in required_keys if key in parameter_list
                    ):
                        parameter = parameter_list[0]

                    _value = re.findall("输入(.*)", content)[0]
                    if _value == "None":
                        _value = None

                    param_info = (
                        tool_dict.get("parameters", {})
                        .get("properties", {})
                        .get(parameter, {})
                    )
                    if isinstance(
                        param_info, dict
                    ):  # 若不为dict，则为workflow；若为dict，则为插件
                        _type = param_info.get("type", "")
                        if _type != "string":
                            _value = get_expect_param(
                                _type, _value
                            )  # 根据参数类型给插件传值

                    args = {parameter: _value}
                    from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall

                    tool_call = ToolCall(
                        id="call_0",
                        type="function",
                        name=tool_dict.get("name"),
                        arguments=json.dumps(args),
                    )
                    yield AssistantMessageChunk(
                        content=f"{tool_dict.get('name')}|{json.dumps(args)}",
                        tool_calls=[tool_call],
                    )
                except Exception as e:
                    llm_logger.error(f"Tool call parsing error: {e}")
                    yield AssistantMessageChunk(content="mock streaming error")
            else:
                # 普通流式输出
                for char in "mock streaming response":
                    await asyncio.sleep(0.1)  # 模拟流式延迟
                    yield AssistantMessageChunk(content=char)
        else:
            # 处理工具调用返回
            if messages_dict and messages_dict[-1]["role"] == "tool":
                answer = messages_dict[-1]["content"]
                yield AssistantMessageChunk(content=answer)
            elif (
                messages_dict
                and len(messages_dict) > 1
                and messages_dict[-1]["role"] == "user"
            ):
                # 处理记忆变量场景
                if "记忆" in messages_dict[-1]["content"] and len(messages_dict) > 2:
                    answer = get_mock_result(
                        messages_dict[-2]["content"].split("\n")[-1], "mock LLM"
                    )
                elif (
                    "Perform variable value extraction" in messages_dict[-1]["content"]
                ):
                    # 记忆变量
                    query = re.findall(
                        r"<Conversation>[\r\n]*\s*(.*?)\s*[\r\n]*</Conversation>",
                        messages_dict[-1]["content"],
                        re.DOTALL,
                    )[0]
                    answer = get_mock_result(query, "mock momery")
                elif (
                    "user_name:user_Tom\nuser_age:666.0" in messages_dict[-1]["content"]
                ):
                    # mock LLM json
                    answer = get_mock_result(
                        messages_dict[-1]["content"], "mock LLM json"
                    )
                else:
                    # 默认mock LLM
                    answer = get_mock_result(
                        messages_dict[-1]["content"].split("\n")[-1], "mock LLM"
                    )

                # 调用外部mock URL
                connector = Connector().get_tcp_connector()
                async with aiohttp.ClientSession(
                    connector=connector, connector_owner=connector is None
                ) as session:
                    async with session.post(
                        url=self.get_mock_url(), json={"prompt": answer}
                    ) as response:
                        async for line in response.content:
                            if line:
                                decoded_line = line.decode("utf-8")
                                if decoded_line.startswith("data: "):
                                    _data = decoded_line[6:].strip()
                                    if _data != "[DONE]":
                                        yield AssistantMessageChunk(
                                            content=json.loads(_data).get("content", "")
                                        )

                yield AssistantMessageChunk(content=answer)
            else:
                # 默认流式输出
                for char in "mock streaming response":
                    await asyncio.sleep(0.1)  # 模拟流式延迟
                    yield AssistantMessageChunk(content=char)

    async def generate_image(
        self,
        messages: List[UserMessage],
        *,  # 强制使用关键字参数
        model: Optional[str] = None,
        size: Optional[str] = "1664*928",
        negative_prompt: Optional[str] = None,
        n: Optional[int] = 1,
        prompt_extend: bool = True,
        watermark: bool = False,
        seed: int = 0,
        **kwargs,
    ) -> ImageGenerationResponse:
        """模拟图片生成"""
        llm_logger.info(f"MockModelClient generate_image messages: {messages}")
        return ImageGenerationResponse(
            images=["mock_image_url"],
            model=model or "mock_image_model",
            prompt=messages[0].content if messages else "",
            size=size,
            n=n,
        )

    async def generate_speech(
        self,
        messages: List[UserMessage],
        *,  # 强制使用关键字参数
        model: Optional[str] = None,
        voice: Optional[str] = "Cherry",
        language_type: Optional[str] = "Auto",
        **kwargs,
    ) -> AudioGenerationResponse:
        """模拟语音生成"""
        llm_logger.info(f"MockModelClient generate_speech messages: {messages}")
        return AudioGenerationResponse(
            audio_url="mock_audio_url",
            model=model or "mock_speech_model",
            voice=voice,
            language_type=language_type,
        )

    async def generate_video(
        self,
        messages: List[UserMessage],
        *,  # 强制使用关键字参数
        img_url: Optional[str] = None,
        audio_url: Optional[str] = None,
        model: Optional[str] = None,
        size: Optional[str] = None,
        resolution: Optional[str] = None,
        duration: Optional[int] = 5,
        prompt_extend: bool = True,
        watermark: bool = False,
        negative_prompt: Optional[str] = None,
        seed: Optional[int] = None,
        **kwargs,
    ) -> VideoGenerationResponse:
        """模拟视频生成"""
        llm_logger.info(f"MockModelClient generate_video messages: {messages}")
        return VideoGenerationResponse(
            video_url="mock_video_url",
            model=model or "mock_video_model",
            size=size,
            resolution=resolution,
            duration=duration,
        )


# 动态注册MockModelClient到客户端注册表
def register_mock_model():
    """动态注册MockModelClient到客户端注册表"""
    registry = get_client_registry()
    registry.register_class(MockModelClient)
    llm_logger.info("MockModelClient已成功注册到客户端注册表")


# 确保模块被导入时注册客户端
register_mock_model()

__all__ = ["MockModelClient", "register_mock_model"]
