#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
DeepSeek service
"""

import json
import os
from typing import List, Optional, Any, Dict, Tuple, AsyncIterator

import aiohttp
import requests
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.language_model.base import (
    BaseChatModel,
    LanguageModelOutput,
)
from jiuwen.common.llm_service.messages import (
    BaseMessage,
    AIMessage,
    Tool,
    UsageMetadata,
    ToolCall,
)
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.common.log.base import logger
from jiuwen.serve.common.context import request_ctx
from pydantic import Field, BaseModel

MESSAGES_STR = "messages"
# siliconflow 非流式 让模型增加functioncall能力适配的代码
SPECIAL_TOKEN = ["[unused5]", "[unused6]", "[unused7]", "[unused8]"]


def get_x_request_id() -> str:
    """Returns the request executionId of current thread. Empty str if nothing found."""
    try:
        return request_ctx.get().get("x-request-id", "")
    except Exception:
        return ""


class Nl2AgentBuilderModel(BaseModel, BaseChatModel):
    """agentBuilder LLM chat models API."""

    @property
    def _default_params(self) -> Dict[str, Any]:
        """Get the default parameters for calling agent-builderAPI."""
        params = {
            "model": self.model_name,
            "stream": self.streaming,
            "temperature": self.temperature,
            "top_p": self.top_p,
        }
        return params

    """Base auth for API requests"""
    api_key: Optional[str] = Field(default="", alias="api_key")
    """Base URL path for API requests"""
    api_base: Optional[str] = Field(default="", alias="api_base")
    """Model name for API requests"""
    model_name: Optional[str] = Field(default="", alias="model")
    temperature: float = Field(default=0.95)
    top_p: float = Field(default=0.1)
    """Whether to stream the results or not."""
    streaming: bool = Field(default=False, alias="stream")
    """agentBuilder model NetworkGate token"""
    x_auth_token: Optional[str] = Field(default="", alias="x_auth_token")
    """agentBuilder model name"""
    agentBuilder_model_name: Optional[str] = Field(
        default="", alias="agentBuilder_model_name"
    )
    """agentBuilder model workspace_id"""
    workspace_id: Optional[str] = Field(default="", alias="workspace_id")
    """agentBuilder model auth_id"""
    auth_id: Optional[str] = Field(default="", alias="auth_id")

    if model_name == "" or api_key == "":
        raise JiuWenBaseException(
            StatusCode.LLM_CONFIG_MISS_ERROR.code,
            StatusCode.LLM_CONFIG_MISS_ERROR.errmsg.format(
                error_msg="please check qwen-max api_base, model_name value."
            ),
        )

    class Config:
        """Configuration for this pydantic object."""

        populate_by_name = True

    @property
    def _llm_type(self) -> str:
        """Return type of llm."""
        return "qwen"

    @staticmethod
    def _convert_tools_format(tools):
        if tools is None:
            return []
        converted_tools = []
        for tool in tools:
            if tool["parameters"]["properties"].get("required"):
                del tool["parameters"]["properties"]["required"]
            converted_tool = {
                "type": "function",
                "function": {
                    "name": tool["name"],
                    "description": tool["description"],
                    "parameters": tool["parameters"],
                },
            }
            converted_tools.append(converted_tool)
        return converted_tools

    @staticmethod
    def _convert_messages_format(messages, new_tools):
        for message in messages:
            if message.get("role") == "assistant":
                if message.get("function_call", None):
                    tool_calls = [
                        {
                            "index": 0,
                            "id": message.get("function_call").get("name"),
                            "type": "function",
                            "function": {
                                "name": message.get("function_call").get("name"),
                                "arguments": message.get("function_call").get(
                                    "arguments"
                                ),
                            },
                        }
                    ]
                    del message["function_call"]
                    message["tool_calls"] = tool_calls
                if "DeepseekAnswer" in message.get("content"):
                    message["content"] = str(
                        message.get("content")[
                            str(message.get("content")).index("DeepseekAnswer") :
                        ]
                    ).replace("DeepseekAnswer", "")
            if message.get("role") == "function":
                message["role"] = "tool"
                message["tool_call_id"] = message.pop("name")
            if message.get("role") == "system":
                if (
                    "人设" not in message["content"]
                    and "工具调用逻辑" not in message["content"]
                    and "工具调用格式" not in message["content"]
                ):
                    message["content"] = message["content"]
                else:
                    message["content"] = (
                        message["content"]
                        + "\n当前可用插件如下：\n"
                        + str(new_tools)
                        + """\n插件调用格式如下：\n[unused7]function_name|{"argument_name":argument_value,...}
                        [unused8]若存在信息缺失，则向用户提问"""
                    )

        return messages

    def _create_message_dicts(
        self, messages: List[BaseMessage]
    ) -> Tuple[List[Dict[str, Any]], Dict[str, Any]]:
        """Convert BaseMessage to a message_dict."""
        message_dicts = [self._convert_message_to_dict(m) for m in messages]
        params = self._default_params
        return message_dicts, params

    def _request_params(
        self, messages: List[BaseMessage], tools: [Dict[str, Any]] = None
    ):
        """agentBuilder Construct the information required for the request."""
        headers = {"Authorization": self.api_key, "Content-Type": "application/json"}
        message_dicts, params = self._create_message_dicts(messages)
        top_p, temperature = ModelUtil.truncate_params(params)
        payload = {**params, MESSAGES_STR: message_dicts, "functions": tools}
        new_tools = self._convert_tools_format(tools)

        request_data = {
            "model": payload.get("model"),
            "top_p": top_p,
            "temperature": temperature,
            "messages": self._convert_messages_format(message_dicts, new_tools),
        }

        return request_data, headers

    def _chat(
        self,
        messages: List[BaseMessage],
        tools: List[Tool] = None,
        **kwargs: Any,
    ) -> LanguageModelOutput:

        resquest_data, headers = self._request_params(messages, tools)
        resquest_data.update({"stream": False})
        res = requests.post(
            url=self.api_base,
            json=resquest_data,
            headers=headers,
            verify=ModelUtil.parse_ssl_verify(),
        )
        if res.status_code == 200:
            res_json = res.json()
            usage_metadata = UsageMetadata(
                code=0,
                errmsg="成功",
                task_id=res_json.get("id"),
                model_name=res_json.get("model"),
                finish_reason="stop",
            )
            res_content = res_json.get("choices")[0].get("message").get("content")
            name, args = ModelUtil.create_tools_result(res_content, SPECIAL_TOKEN)
            if res_content is None:
                res_content = ""
            final_content = res_content
            if name and args:
                name = str(name).strip()
                args = str(args).strip()
                flag, args = ModelUtil.check_and_trans2json(args)
                if flag:
                    tools_call = ToolCall(name=name, args=args)
                    usage_metadata.finish_reason = "function_call"
                else:
                    tools_call = {}
                    usage_metadata.finish_reason = "stop"
            else:
                tools_call = {}
                usage_metadata.finish_reason = "stop"
            return AIMessage(
                content=final_content,
                usage_metadata=usage_metadata,
                tool_calls=tools_call,
            )
        else:
            raise JiuWenBaseException(
                StatusCode.LLM_REQUEST_ERROR.code,
                StatusCode.LLM_REQUEST_ERROR.errmsg.format(
                    error_msg=res.json().get("message", "please check model service.")
                ),
            )

    async def _stream(
        self, messages: List[BaseMessage], tools: List[Tool] = None, **kwargs: Any
    ) -> AsyncIterator[LanguageModelOutput]:
        """agentBuilder stream function."""
        resquest_data, headers = self._request_params(messages, tools)
        resquest_data.update({"stream": True})

        gate_api_base = os.getenv("MODEL_ROUTER_API")
        gata_headers = {
            "X-Auth-Token": self.x_auth_token,
            "Content-Type": "application/json",
            "X-Workspace-Id": self.workspace_id,
            "safety-barrier": "false",
        }
        if self.auth_id:
            gata_headers["X-Auth-Id"] = self.auth_id
        request_id = get_x_request_id()
        if request_id:
            gata_headers["X-Request-Id"] = request_id

        resquest_data["messages"] = ensure_user_message_exists(
            resquest_data["messages"]
        )

        gate_requests_data = {
            "model": self.agentBuilder_model_name,
            "stream": True,
            "top_p": resquest_data["top_p"],
            "temperature": resquest_data["temperature"],
            "frequency_penalty": 0.0,
            "messages": resquest_data["messages"],
        }
        logger.info(
            f"model call request data: {json.dumps(gate_requests_data, ensure_ascii=False)}"
        )

        # 转换超时设置: requests (connect, read) -> aiohttp ClientTimeout
        timeout = aiohttp.ClientTimeout(connect=15, sock_read=600)

        # 处理 SSL 校验
        verify = ModelUtil.parse_ssl_verify()
        # aiohttp 的 ssl 参数通常接受 SSLContext 或 False (不校验)
        # 如果 verify 是路径字符串，需要额外创建 SSLContext，此处假设主要处理 False 的情况
        ssl_context = False if verify is False else None

        try:
            async with aiohttp.ClientSession(timeout=timeout) as session:
                async with session.post(
                    url=gate_api_base,
                    json=gate_requests_data,
                    headers=gata_headers,
                    ssl=ssl_context,
                ) as res:
                    if res.status == 200:
                        total_message = ""
                        usage_metadata = UsageMetadata(prompt="null")
                        model_response_content = []
                        # 异步迭代读取流式内容
                        async for line in res.content:
                            if line:
                                line_str = (
                                    line.decode("utf-8").strip().removeprefix("data: ")
                                )
                                if line_str == "[DONE]":
                                    # 模型输出结束的标识
                                    name, args = ModelUtil.create_tools_result(
                                        total_message, SPECIAL_TOKEN
                                    )
                                    final_content = total_message
                                    usage_metadata.type = kwargs.get("type")
                                    if name and args:
                                        name = str(name).strip()
                                        args = str(args).strip()
                                        flag, args = ModelUtil.check_and_trans2json(
                                            args
                                        )
                                        if flag:
                                            tools_call = ToolCall(name=name, args=args)
                                            usage_metadata.finish_reason = (
                                                "function_call"
                                            )
                                        else:
                                            usage_metadata.finish_reason = "stop"
                                            tools_call = {}
                                    else:
                                        tools_call = {}
                                        usage_metadata.finish_reason = "stop"

                                    yield AIMessage(
                                        content=final_content,
                                        usage_metadata=usage_metadata,
                                        tool_calls=tools_call,
                                    )
                                else:
                                    try:
                                        response_data = json.loads(line_str)
                                        if (
                                            "choices" in response_data
                                            and response_data["choices"]
                                            and "delta" in response_data["choices"][0]
                                        ):
                                            delta = response_data["choices"][0]["delta"]

                                            if delta:
                                                content = delta.get("content")
                                                if content:
                                                    total_message += f"{content}"
                                                    usage_metadata.code = 0
                                                    usage_metadata.errmsg = "成功"
                                                    usage_metadata.model_name = (
                                                        self.model_name
                                                    )
                                                    usage_metadata.finish_reason = (
                                                        "null"
                                                    )
                                                    usage_metadata.type = kwargs.get(
                                                        "type"
                                                    )
                                                    model_response_content.append(
                                                        content
                                                    )
                                                    yield AIMessage(
                                                        raw_content="",
                                                        content=content,
                                                        usage_metadata=usage_metadata,
                                                        tool_calls={},
                                                    )
                                    except json.JSONDecodeError:
                                        continue
                        logger.info(
                            f"model call response data:{''.join(model_response_content)}"
                        )

                    else:
                        try:
                            # 异步获取响应文本
                            error_response_text = await res.text()
                        except Exception as e:
                            logger.warning(f"读取非200响应体时出错: {e}")
                            error_response_text = "无法读取响应体"

                        not_200_error_msg = (
                            f"LLM 请求失败 | "
                            f"URL: {gate_api_base} | "
                            f"Status Code: {res.status} | "
                            f"Response: {error_response_text[:500]}..."
                        )
                        logger.error(not_200_error_msg, exc_info=True)

                        error_msg = (
                            f"Status: {res.status}. "
                            f"Detailed Error: {error_response_text}"
                        )

                        raise JiuWenBaseException(
                            StatusCode.LLM_REQUEST_ERROR.code,
                            StatusCode.LLM_REQUEST_ERROR.errmsg.format(
                                error_msg=error_msg
                            ),
                        )

        except Exception as e:
            # 捕获 aiohttp 相关的异常或上述抛出的业务异常
            # 如果是业务异常直接抛出，否则包装
            if isinstance(e, JiuWenBaseException):
                raise e
            raise JiuWenBaseException(
                StatusCode.LLM_REQUEST_ERROR.code,
                StatusCode.LLM_REQUEST_ERROR.errmsg.format(error_msg=str(e)),
            )


def ensure_user_message_exists(messages, default_trigger="请根据上述指示进行分析。"):
    """
    检查 messages 列表中是否存在 role='user' 的消息。
    如果不存在，自动追加一条默认的 user 消息以符合 API 规范。

    Args:
        messages (list): 消息列表
        default_trigger (str): 自动补充的用户内容

    Returns:
        list: 修正后的消息列表
    """
    if messages is None:
        messages = []

    # 1. 检查当前列表中是否有 user 角色
    # 使用 generator expression 高效查找
    has_user = any(msg.get("role") == "user" for msg in messages)

    # 2. 如果没有 user，追加一个触发消息
    if not has_user:
        logger.info(
            f"The message list is missing the 'user' role. "
            f"NL2WORKFLOW is automatically "
            f"supplementing the trigger message: '{default_trigger}'"
        )

        new_user_msg = {"role": "user", "content": default_trigger}
        messages.append(new_user_msg)

    # 删除首位换行符
    for msg in messages:
        if msg.get("content"):
            msg["content"] = msg["content"].strip()

    return messages
