#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""for assembling prompt template"""

from __future__ import annotations

import copy
import json
from copy import deepcopy
from enum import Enum
from typing import List, Any, Dict

from jiuwen.executor_sdk.operator.impl.common.utils import executor_logger
from jiuwen.prompt.agent.assemble.selectors.tailored_plugin_result_selector import (
    TailoredPluginResultSelector,
)
from jiuwen.prompt.agent.assemble.variables.variable_formatter import VariableFormatter
from jiuwen.prompt.agent.common.utils import CHAT_HISTORY_LITERAL, TOOLS_LITERAL


class ToolCallFormat(Enum):
    STANDARD = "standard"
    CUSTOMIZED = "customized"


class DeployMode(Enum):
    """
    LLM组件的两种使用模式
    internal: 内部编排模式
    workflow: 可视化编排模式
    """

    INTERNAL = "internal"
    WORKFLOW = "workflow"


TOKEN_TO_CHAR_RATIO = 1.5
LLM_RESPONSE_LENGTH = int(500 * TOKEN_TO_CHAR_RATIO)


class PromptAssembler:
    """
    Prompt assembler
    """

    def __init__(
        self,
        template: List[dict],
        tool_call_format: ToolCallFormat,
        deploy_mode: DeployMode,
        variable_format_schema: dict = None,
        refiner_config: dict = None,
    ):
        self.message_delimiter = "`#message_delimiter#`"
        self.tool_call_format = tool_call_format
        self.deploy_mode = deploy_mode
        self.template = template
        self.variable_format_schema = variable_format_schema
        self.refiner_config = refiner_config
        self.full_history = None
        self.max_prompt_length = (
            self.refiner_config.get("llmContextLength", None)
            if self.refiner_config
            else None
        )
        if self.max_prompt_length:
            self.max_prompt_length = int(self.max_prompt_length * TOKEN_TO_CHAR_RATIO)
        self.limit_template_length = (
            int(self.max_prompt_length / 4)
            if self.max_prompt_length is not None
            else None
        )
        self.limit_history_length = (
            int(self.max_prompt_length / 4 - LLM_RESPONSE_LENGTH)
            if self.max_prompt_length is not None
            else None
        )
        self.limit_tools_length = (
            int(self.max_prompt_length / 2)
            if self.max_prompt_length is not None
            else None
        )

    @staticmethod
    def format_history(history: List[dict]) -> str:
        """format_history_as_string"""
        history_str_list = []
        for message in history:
            if message["role"] == "assistant":
                history_str_list.append(
                    f"{message['role']}: {message.get('llm_content') or message.get('content')}"
                )
            else:
                history_str_list.append(f"{message['role']}: {message.get('content')}")

        return "\n".join(history_str_list)

    @staticmethod
    def format_tools(tools: List[dict]) -> str:
        """format_tools"""
        if not tools:
            return ""
        res = []
        for tool in tools:
            function_description = tool.get("function", {})
            res.append(
                json.dumps(
                    {
                        "name": function_description.get("name"),
                        "description": function_description.get("description"),
                        "parameters": function_description.get("parameters"),
                    },
                    ensure_ascii=False,
                )
            )
        return "\n".join(res)

    @classmethod
    def from_config(
        cls,
        template: List[dict],
        tool_call_format: str = "standard",
        deploy_mode: str = "internal",
        variable_format_schema: dict = None,
        refiner_config: dict = None,
    ) -> PromptAssembler:
        """from_config"""
        return cls(
            template,
            tool_call_format=ToolCallFormat(tool_call_format),
            deploy_mode=DeployMode(deploy_mode),
            variable_format_schema=variable_format_schema,
            refiner_config=refiner_config,
        )

    def select_history(self, history, tools_template_query_length):
        """select history"""
        #  保留最近三轮的对话
        new_history = []
        user_count = 0
        history_length = len(str(json.dumps(history, ensure_ascii=False)))

        if history_length > self.limit_history_length:
            for index in range(len(history) - 1, -1, -1):
                new_history.insert(0, history[index])
                if history[index]["role"] == "user":
                    user_count += 1
                if user_count == 3:
                    break
        else:
            new_history = copy.deepcopy(history)
        #     判断当前是否满足条件
        if (
            len(str(json.dumps(new_history, ensure_ascii=False)))
            + tools_template_query_length
            < self.max_prompt_length
        ):
            return new_history

        for item in new_history:
            if item["role"] == "assistant":
                item["content"] = item["content"][:150]
                item["llm_content"] = item["llm_content"][:150]
            elif item["role"] == "tool":
                item["content"] = item["content"][:150]
            elif item["role"] == "user":
                item["content"] = item["content"][:150]

        if (
            len(str(json.dumps(new_history, ensure_ascii=False)))
            + tools_template_query_length
            < self.max_prompt_length
        ):
            return new_history

        if (
            len(str(json.dumps(new_history, ensure_ascii=False)))
            > self.limit_history_length
        ):
            quota = self.limit_history_length
            temp_history = []
            for entry in new_history:
                his_length = len(json.dumps(entry, ensure_ascii=False))
                if his_length < quota:
                    temp_history.append(entry)
                    quota -= his_length
                else:
                    entry["content"] = entry["content"][:quota]
                    temp_history.append(entry)
                    break
            new_history = temp_history

        return new_history

    def select_tools(self, tools, tools_length):
        """select history"""
        new_tools = []
        if tools_length > self.limit_tools_length:
            quota = self.limit_tools_length
            truncate_tools = []
            for tool in tools:
                tool_length = len(json.dumps(tool, ensure_ascii=False))
                if tool_length < quota:
                    truncate_tools.append(tool)
                    quota -= tool_length
                else:
                    break
            new_tools = truncate_tools
        return new_tools

    def select_template_tools_history(self, template, tools, history, query=""):
        """select template tools history"""
        template_length = len(str(json.dumps(template, ensure_ascii=False)))
        tools_length = len(str(json.dumps(tools, ensure_ascii=False)))
        executor_logger().info(
            f"Before history truncation: template_length={template_length}, "
            f"template_length={tools_length}, history={len(str(json.dumps(history, ensure_ascii=False)))}"
        )

        #  保留最近三轮的对话
        tools_template_query_length = tools_length + template_length + len(str(query))
        new_history = self.select_history(
            history=history, tools_template_query_length=tools_template_query_length
        )

        if (
            len(str(json.dumps(new_history, ensure_ascii=False)))
            + tools_template_query_length
            < self.max_prompt_length
        ):
            return template, tools, new_history

        new_tools = self.select_tools(tools=tools, tools_length=tools_length)

        if (
            len(str(json.dumps(new_history, ensure_ascii=False)))
            + template_length
            + len(str(json.dumps(new_tools, ensure_ascii=False)))
            + len(str(query))
            < self.max_prompt_length
        ):
            return template, new_tools, new_history

        new_template = []
        dynamic_limit_template_length = int(
            self.max_prompt_length
            - len(str(json.dumps(new_tools, ensure_ascii=False)))
            - len(str(json.dumps(new_history, ensure_ascii=False)))
            - LLM_RESPONSE_LENGTH
        )
        if template_length > dynamic_limit_template_length:
            quota = dynamic_limit_template_length
            template_length = 0
            for msg in reversed(template):
                if quota <= 0:
                    content = ""
                else:
                    content = msg["content"][-quota:]
                    quota -= len(content)
                template_length += len(content)
                new_template = [{**msg, "content": content}] + new_template
        else:
            new_template = copy.deepcopy(template)
        executor_logger().info(
            f"After history truncation: template_length= "
            f"{len(str(json.dumps(new_template, ensure_ascii=False)))},"
            f" tools_length={len(str(json.dumps(new_tools, ensure_ascii=False)))}, "
            f"history_length={len(str(json.dumps(new_history, ensure_ascii=False)))}"
        )
        return new_template, new_tools, new_history

    def fill_template(self, template: list, variables: dict):
        """fill template"""
        concatenated_string_template = self.message_delimiter.join(
            message.get("content", "") for message in template
        )
        format_method = VariableFormatter(
            text=concatenated_string_template,
            variable_format_schema=self.variable_format_schema,
        )
        format_method.update(**variables)
        filled_template = self._format(format_method.generate())
        return filled_template

    def assemble(
        self,
        variables: Dict[str, Any],
        history: List[dict] = None,
        tools: List[dict] = None,
        query: str = None,
    ) -> tuple:
        """assemble"""
        new_history = copy.deepcopy(history) if history else []
        new_tools = copy.deepcopy(tools) if tools else []
        new_template = copy.deepcopy(self.template) if self.template else []

        if (
            self.tool_call_format == ToolCallFormat.CUSTOMIZED
            and len(new_template) == 1
            and new_template[0]["role"] == "system"
        ):
            #   转换成B标
            new_template[0]["role"] = "user"
            new_template.insert(0, {"role": "system", "content": ""})
            self.template = new_template

        # 1先填充template
        filled_template = self.fill_template(new_template, variables)

        # 2.校验history、tools、template是否超长，统计各自的长度
        history_length = len(str(json.dumps(new_history, ensure_ascii=False)))
        tools_length = len(str(json.dumps(new_tools, ensure_ascii=False)))
        template_length = len(str(json.dumps(filled_template, ensure_ascii=False)))

        # 3.超长则截断
        executor_logger().info(
            f"current max_prompt_length, max_prompt_length={self.max_prompt_length}"
        )
        if (
            self.max_prompt_length
            and history_length + tools_length + template_length > self.max_prompt_length
        ):
            executor_logger().info("begin truncation")
            filled_template, new_tools, new_history = (
                self.select_template_tools_history(
                    template=filled_template, tools=new_tools, history=new_history
                )
            )

        self.full_history = deepcopy(new_history)

        new_history = TailoredPluginResultSelector(
            refiner_config=self.refiner_config
        ).select_function_results(new_history)

        res_prompt = filled_template
        if self.tool_call_format == ToolCallFormat.CUSTOMIZED:
            new_variables = {
                TOOLS_LITERAL: self.format_tools(new_tools),
                CHAT_HISTORY_LITERAL: self.format_history(new_history),
            }
            res_prompt = self.fill_template(filled_template, new_variables)

        # 最后对内容进行长度校验，不符合进行统一的截断，待处理
        if self.tool_call_format == ToolCallFormat.STANDARD:
            if self.deploy_mode == DeployMode.INTERNAL:
                # STANDARD prompt tools都不为空
                res_prompt = res_prompt + new_history
            else:
                res_prompt = new_history + res_prompt
        return res_prompt, new_tools

    def _format(self, prompt) -> List[dict]:
        """_format"""
        message_contents = prompt.split(self.message_delimiter)
        formatted_message = []
        for idx, message in enumerate(self.template):
            formatted_message.append({**message, "content": message_contents[idx]})
        return formatted_message
