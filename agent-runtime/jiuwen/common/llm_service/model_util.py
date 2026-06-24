#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
"""
ModelUtil
"""

import json
import os
import re
from typing import List, Dict, Any, Optional

from jiuwen.common.configs.env_constants import (
    MODEL_CFG_SSL_CERT_PATH_KEY,
    MODEL_CFG_SSL_MODE_KEY,
)
from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.llm_service.messages import (
    SystemMessage,
    HumanMessage,
    FunctionMessage,
    AIMessage,
    ToolMessage,
    ToolCall,
)
from jiuwen.controller.common.constants import FUNCTION_ROLE
from jiuwen.plugin.models.function import Function
from jiuwen.prompt import TemplateManager

ROLE_STR = "role"
SYSTEM_STR = "system"
PARA_STR = "parameters"


class ModelUtil:
    """ModelUtil class"""

    system_message_prompt = "llm"
    system_message_prompt_without_tool = "llm_without_tool"

    @classmethod
    def find_first_punctuation_from_right(cls, text):
        """find a punctuation in text.

        Args:
            text: The text.

        Returns:
            index.
        """

        punctuation = {
            "。",
            "，",
            ",",
            "！",
            "？",
            "；",
            "：",
            ":",
            "“",
            "”",
            "‘",
            "’",
            "【",
            "】",
            "（",
            "）",
            "《",
            "》",
            "、",
            "——",
            "…",
            "·",
            "\n",
        }
        for index, char in enumerate(reversed(text)):
            if char in punctuation:
                return len(text) - index - 1
        return -1

    @classmethod
    def switch_message(cls, messages: list):
        """switch message"""
        content_str = "content"
        if not messages:
            raise ValueError("model request messages is none.")

        request_mess = list()
        for mess in messages:
            if mess.get(ROLE_STR) == "system":
                request_mess.append(SystemMessage(content=mess.get(content_str)))
            elif mess.get(ROLE_STR) == "user":
                request_mess.append(HumanMessage(content=mess.get(content_str)))
            elif mess.get(ROLE_STR) == "function":
                request_mess.append(
                    FunctionMessage(
                        content=mess.get(content_str), name=mess.get("name", "")
                    )
                )
            elif mess.get(ROLE_STR) == "tool":
                request_mess.append(
                    ToolMessage(
                        content=mess.get(content_str),
                        tool_call_id=mess.get("tool_call_id", ""),
                        name=mess.get("name", ""),
                    )
                )
            elif mess.get(ROLE_STR) == "assistant":
                cls.switch_assistant_msg(content_str, mess, request_mess)
        return request_mess

    @classmethod
    def switch_assistant_msg(cls, content_str, mess, request_mess):
        """switch assistant message"""
        function_call = mess.get("tool_calls", {})
        ai_msg = None
        if not function_call:
            ai_msg = AIMessage(content=mess.get(content_str))
        elif isinstance(function_call, dict):
            args_ment = function_call.get("arguments", {})
            args_ment_json = dict()
            tool_call_id = function_call.get("tool_call_id", "")
            if isinstance(args_ment, str):
                try:
                    args_ment_json = json.loads(args_ment)
                except json.decoder.JSONDecodeError as error:
                    raise JiuWenBaseException(
                        error_code=StatusCode.LLM_RESPONSE_SCHEMA_ERROR.code,
                        message=StatusCode.LLM_RESPONSE_SCHEMA_ERROR.errmsg.format(
                            error_msg="arguments invalid json formatting."
                        ),
                    ) from error
            elif isinstance(args_ment, dict):
                args_ment_json = args_ment
            ai_msg = AIMessage(
                content=mess.get(content_str),
                tool_calls=ToolCall(
                    name=function_call.get("name", ""),
                    id=tool_call_id,
                    args=args_ment_json,
                ),
            )
        elif isinstance(function_call, list):
            tool_calls = list()
            for item_fun in function_call:
                item = item_fun.get("function", {})
                arguments_json = dict()
                arguments = item.get("arguments", {})
                tool_call_id = item_fun.get("id", {})
                if isinstance(arguments, str):
                    try:
                        arguments_json = json.loads(arguments)
                    except json.decoder.JSONDecodeError as error:
                        raise JiuWenBaseException(
                            error_code=StatusCode.LLM_RESPONSE_SCHEMA_ERROR.code,
                            message=StatusCode.LLM_RESPONSE_SCHEMA_ERROR.errmsg.format(
                                error_msg="arguments invalid json formatting."
                            ),
                        ) from error
                elif isinstance(arguments, dict):
                    arguments_json = arguments
                tool_calls.append(
                    ToolCall(
                        name=item.get("name", ""), args=arguments_json, id=tool_call_id
                    )
                )
            ai_msg = AIMessage(content=mess.get(content_str), tool_calls=tool_calls)

        if ai_msg:
            request_mess.append(ai_msg)

    @classmethod
    def check_and_trans2json(cls, text: str):
        """check and trans to json"""
        try:
            json_text = json.loads(text, strict=False)
            return True, json_text
        except json.JSONDecodeError:
            pass
        replaced_text = re.sub(
            r"\b(True|TRUE|False|FALSE)\b", lambda match: match.group(0).lower(), text
        )
        try:
            json_text = json.loads(replaced_text, strict=False)
            return True, json_text
        except json.JSONDecodeError:
            return False, None

    @classmethod
    def replace_special_token(cls, text: str, special_token: list):
        """replace special token"""
        for item in special_token:
            text = text.replace(item, "")
        return text

    @classmethod
    def parse_ssl_verify(cls):
        """trans_str_to_bool, according to application.yaml loaded rule, variables set to env will json.dumps first

        Args:
            value(str): input ssl verify env value.

        Returns:
            if value is exist and value is str, return json.loads(value).
            else ssl verify set to default: True.
        """
        ssl_verify = os.getenv(MODEL_CFG_SSL_MODE_KEY)
        ssl_mode = True
        if ssl_verify and isinstance(ssl_verify, str):
            try:
                ssl_mode = json.loads(ssl_verify)
            except json.decoder.JSONDecodeError as error:
                raise JiuWenBaseException(
                    error_code=StatusCode.PROMPT_MODEL_SSL_VERIFY_ENV_ERROR.code,
                    message=StatusCode.PROMPT_MODEL_SSL_VERIFY_ENV_ERROR.errmsg.format(
                        error_msg="model ssl error"
                    ),
                ) from error
        certification_path_verify = True
        if ssl_mode:
            certification_path = os.getenv(MODEL_CFG_SSL_CERT_PATH_KEY)
            if certification_path:
                certification_path_verify = certification_path
        else:
            certification_path_verify = ssl_mode
        return certification_path_verify

    @classmethod
    def create_tools_result(cls, response: str, special_token: list):
        """Convert response to a tool.

        Args:
            response: The text.

        Returns:
            The name, arguments.
        """
        if special_token[3] not in response:
            return "", ""

        right_index = response.rfind(special_token[3])
        left_index = (
            response.rfind(special_token[2]) if special_token[2] in response else 0
        )
        truncate_res = response[left_index + len(special_token[1]) : right_index]

        if "|" in truncate_res:
            delimiter_index = truncate_res.find("|")
            punctuation_index = ModelUtil.find_first_punctuation_from_right(
                truncate_res[:delimiter_index]
            )
            name_start_index = punctuation_index + 1 if punctuation_index >= 0 else 0
            name = truncate_res[name_start_index:delimiter_index]
            name = name.strip()
            arguments = truncate_res[delimiter_index + 1 :]
            return name, arguments
        return "", ""

    @classmethod
    def create_multi_tools_result(cls, response: str, special_token: list):
        """新增多工具参数解析

        Args:
            response: The text.
            special_token: token列表

        Returns:
            The name, arguments.
        """
        tool_list = []
        while special_token[3] in response:
            left_index = (
                response.find(special_token[2]) if special_token[2] in response else 0
            )
            right_index = response.find(special_token[3])
            truncate_res = response[left_index + len(special_token[2]) : right_index]
            response = response[right_index + len(special_token[3]) :]
            if "|" in truncate_res:
                delimiter_index = truncate_res.find("|")
                punctuation_index = ModelUtil.find_first_punctuation_from_right(
                    truncate_res[:delimiter_index]
                )
                name_start_index = (
                    punctuation_index + 1 if punctuation_index >= 0 else 0
                )
                name = truncate_res[name_start_index:delimiter_index].strip()
                arguments = truncate_res[delimiter_index + 1 :]
                tool_list.append((name, arguments))
        return ModelUtil.parse_tool_results(tool_list)

    @classmethod
    def parse_tool_results(cls, tool_list: list):
        """parse_tool_results"""
        name, poc_args = "", ""
        # 未提取参数|多个不同工具只执行第一个|多个相同工具一起执行
        if len(tool_list) == 0:
            tool_list.append(("", ""))
        if any(x[0] != tool_list[0][0] for x in tool_list):
            tool_list = tool_list[:1]
        if len(tool_list) == 1:
            name, poc_args = tool_list[0]
        else:
            poc_args_list = []
            for n, p in tool_list:
                # 此处已进行Json校验
                check_result, trans_result = ModelUtil.check_and_trans2json(p)
                if n and check_result:
                    name = n
                    poc_args_list.append(trans_result)
            # 并行工具调用将参数合并为multi_queries参数
            poc_args = json.dumps({"multi_queries": poc_args_list})
        return name, poc_args

    @classmethod
    def function_dict_process(
        cls,
        prompt: str,
        special_token: list[str],
        functions_dict: Optional[List[Dict[str, Any]]],
    ):
        """function dict process"""
        if functions_dict:
            tmp = []
            for item in functions_dict:
                if PARA_STR in item:
                    item.update(dict(arguments=item.get(PARA_STR)))
                    item.pop(PARA_STR)
                tmp.append(json.dumps(item, ensure_ascii=False))

            functions_data = (
                "\n你可以调用各种用户自定义的工具来解决用户的问题。\n可使用工具：\n"
                + "\n".join(tmp).rstrip("\n")
            )
            second_sys_index = prompt.find(
                special_token[0] + "系统", prompt.find(special_token[0] + "系统") + 1
            )
            if second_sys_index != -1:
                unused10_index = prompt.find(special_token[1], second_sys_index)
                prompt = f"{prompt[:unused10_index]}{functions_data}{prompt[unused10_index:]}"
        return prompt

    @classmethod
    def convert_message_to_prompt(
        cls,
        messages_dict: List[Dict[str, Any]],
        special_token: list[str],
        functions_dict: Optional[List[Dict[str, Any]]] = None,
        model_name: str = None,
    ) -> str:
        """Convert a message and tools to a prompt.

        Args:
            messages_dict: The message.
            functions_dict: The tool.
            special_token: The special token.
            model_name: model name

        Returns:
            The prompt.
        """
        count = 0
        prompt = ""
        if not functions_dict:
            system_prompt = TemplateManager().get(
                name=cls.system_message_prompt_without_tool, filters={"tag": model_name}
            )
        else:
            system_prompt = TemplateManager().get(
                name=cls.system_message_prompt, filters={"tag": model_name}
            )
        if not system_prompt:
            raise ValueError("system prompt is None")
        if not isinstance(system_prompt.content, list):
            raise ValueError("system prompt type error")
        system_prompt.content.extend(messages_dict)
        for idx, line in enumerate(system_prompt.content):
            content = line["content"]
            if line[ROLE_STR] == SYSTEM_STR:
                count += 1
                if count < 3:
                    prompt += f"{special_token[0]}系统：{content}{special_token[1]}"
                elif count == 3:
                    index_2 = prompt.find(
                        special_token[1],
                        prompt.find(special_token[1]) + len(special_token[1]),
                    )
                    prompt = (
                        prompt[:index_2]
                        + content
                        + special_token[1]
                        + prompt[index_2 + len(special_token[1]) :]
                    )
            elif line[ROLE_STR] == "user":
                if idx != len(system_prompt.content) - 1:
                    prompt += f"{special_token[0]}用户：{content}{special_token[1]}"
            elif line[ROLE_STR] == "assistant":
                function_call = line.get("function_call", {})
                if isinstance(function_call, dict) and function_call:
                    prompt += (
                        f"{special_token[0]}助手：{special_token[2]}"
                        f"{function_call['name']}|{function_call['arguments']}"
                        f"{special_token[3]}{special_token[1]}"
                    )
                elif isinstance(function_call, list) and function_call:
                    for item in function_call:
                        prompt += (
                            f"{special_token[0]}助手：{special_token[2]}{item.get('name', '')}|"
                            f"{item.get('arguments', '')}{special_token[3]}{special_token[1]}"
                        )
                else:
                    prompt += f"{special_token[0]}助手：{content}{special_token[1]}"
            elif line[ROLE_STR] == FUNCTION_ROLE:
                prompt += (
                    f"{special_token[0]}工具：{line['name']}|"
                    f"{content}{special_token[1]}"
                )

        return ModelUtil.function_dict_process(prompt, special_token, functions_dict)

    @classmethod
    def truncate_params(cls, payload: Dict[str, Any]):
        """Truncate temperature and top_p parameters between [0.01, 1.0].

        Avoid model parameters going out of bounds.
        """
        temperature = payload.get("temperature", 0.1)
        top_p = payload.get("top_p", 0.1)
        if temperature is not None:
            temperature = max(0.01, min(1.0, temperature))
        if top_p is not None:
            top_p = max(0.01, min(1.0, top_p))
        return top_p, temperature

    @classmethod
    def sophon_splicing(cls, query: str, context: []):
        """Splicing query and context to messages"""
        messages = [{"role": "user", "content": query}]
        return cls.switch_message(messages=messages)


class ModelUtilForAgentBuilder(ModelUtil):
    @classmethod
    def convert_message_to_prompt(
        cls,
        messages_dict: List[Dict[str, Any]],
        special_token: list[str],
        functions_dict: Optional[List[Dict[str, Any]]] = None,
        model_name: str = None,
    ) -> str:
        """For agent-builderbeta: Convert a message and tools to a prompt"""
        count = 0
        prompt = ""
        for idx, line in enumerate(messages_dict, start=1):
            content = line["content"]
            if line[ROLE_STR] == SYSTEM_STR:
                count += 1
                if count < 3:
                    prompt += f"{special_token[0]}系统：{content}{special_token[1]}"
                elif count == 3:
                    index_2 = prompt.find(
                        special_token[1],
                        prompt.find(special_token[1]) + len(special_token[1]),
                    )
                    prompt = (
                        prompt[:index_2]
                        + content
                        + special_token[1]
                        + prompt[index_2 + len(special_token[1]) :]
                    )
            elif line[ROLE_STR] == "user":
                if idx != len(messages_dict):
                    prompt += f"{special_token[0]}用户：{content}{special_token[1]}"
            elif line[ROLE_STR] == "assistant":
                function_call = line.get("function_call", {})
                if function_call:
                    if function_call.get("name", "") == "python_interpreter":
                        prompt += (
                            f"{special_token[0]}助手：{content}{special_token[2]}{function_call['name']}|"
                            f"{json.loads(function_call.get('arguments', '{}')).get('code', '')}"
                            f"{special_token[3]}{special_token[1]}"
                        )
                    else:
                        prompt += (
                            f"{special_token[0]}助手：{special_token[2]}工具："
                            f"{function_call['name']}|{function_call['arguments']}"
                            f"{special_token[3]}{special_token[1]}"
                        )
                else:
                    prompt += f"{special_token[0]}助手：{content}{special_token[1]}"
            elif line[ROLE_STR] == "function":
                if "你是数学专家，擅长数值比较，回复简洁直接" in prompt:
                    prompt += f"{special_token[0]}工具：{content}{special_token[1]}"
                else:
                    tool_dict = dict(intent=line["name"], result=content)
                    if line["name"] == "python_interpreter":
                        prompt += f"{special_token[0]}工具：{content}{special_token[1]}"
                    else:
                        prompt += (
                            f"{special_token[0]}工具：{line['name']}|"
                            f"{json.dumps(tool_dict, ensure_ascii=False)}{special_token[1]}"
                        )
        return ModelUtil.function_dict_process(prompt, special_token, functions_dict)

    @classmethod
    def add_user_query(cls, query, prompt, special_token: list[str]):
        """add user query"""
        if query:
            prompt = prompt + special_token[0] + "用户：" + query + special_token[1]
        prompt = prompt + special_token[0] + "助手："
        return prompt

    @classmethod
    def add_user_query_for_oai(cls, query, special_token: list[str]):
        """add user query"""
        if not isinstance(special_token, list) or any(
            not isinstance(token, str) for token in special_token
        ):
            raise ValueError("special_token must be a list of strings")
        messages = []
        if query:
            user_msg = {"role": "user", "content": [{"type": "text", "text": query}]}
            messages.append(user_msg)
        return messages

    @classmethod
    def format_function(cls, tool: Function):
        """format function info for LLM"""
        required = []
        properties = {}
        params = tool.params
        for param in params:
            if not param.visible:
                continue
            properties[param.name] = dict(
                description=param.description, type=param.type
            )
            if param.required:
                required.append(param.name)

        function = dict(name=tool.name, description=tool.description)
        if tool.name == "python_interpreter":
            function.update(
                dict(parameters=dict(type="string", description="python代码"))
            )
        else:
            function.update(
                dict(
                    parameters=dict(
                        type="object", properties=properties, required=required
                    )
                )
            )

        return function

    @classmethod
    def create_tools_result(cls, response: str, special_token: list):
        """Convert response to a tool."""
        if special_token[3] not in response:
            return "", "", ""

        right_index = response.rfind(special_token[3])
        left_index = (
            response.rfind(special_token[2]) if special_token[2] in response else 0
        )
        truncate_res = response[left_index + len(special_token[1]) : right_index]
        left_response = response[:left_index]

        if "|" in truncate_res:
            delimiter_index = truncate_res.find("|")
            punctuation_index = ModelUtil.find_first_punctuation_from_right(
                truncate_res[:delimiter_index]
            )
            name_start_index = punctuation_index + 1 if punctuation_index >= 0 else 0
            name = truncate_res[name_start_index:delimiter_index]
            name = name.strip()
            arguments = truncate_res[delimiter_index + 1 :]
            return name, arguments, left_response
        return "", "", ""
