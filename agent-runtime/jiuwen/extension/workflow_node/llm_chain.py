# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
LLMChain 组件 - 完整迁移自商用版本
适配到 openjiuwen 开源框架

功能特性:
- 完整的配置验证（包括思考模式验证）
- 对话历史管理
- 模板占位符安全验证
- 思考模式支持（reasoning_content）- 完整的流式处理
- 响应格式化（JSON/Markdown/Text）
- 流式输出处理
- 输出配置处理
- Token 使用统计
"""

import asyncio
import json
import re
import time
from typing import Any, List, Optional, AsyncGenerator

from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error
from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.foundation.llm import Model
from openjiuwen.core.foundation.prompt import PromptTemplate
from openjiuwen.core.session.stream.base import CustomSchema
from openjiuwen.core.workflow import WorkflowComponent
from pydantic import BaseModel, StrictStr, Field, ValidationError

USER_FIELDS = "userFields"
PARTIAL_CONTENT = "partial_content"
JIUWEN_LLM_TYPE = "jiuwen.LLMComponent"

CHAT_HISTORY_MAX_TURN = 3
_ROLE = "role"
_CONTENT = "content"
ROLE_MAP = {"user": "用户", "assistant": "助手", "system": "系统"}
MESSAGE_TYPE_TO_ROLE = {"human": "user", "ai": "assistant", "system": "system"}
_TYPE = "type"


class LLMChainModelConfig(BaseModel):
    model_name: StrictStr = Field(alias="modelName")
    model_type: StrictStr = Field(alias="modelType")
    hyper_parameters: dict = Field(alias="hyperParameters", default={})
    extension: dict = Field(default={})


class LLMChainConfig(BaseModel):
    model: LLMChainModelConfig
    deploy_mode: StrictStr = Field(alias="deployMode")
    template_content: list = Field(alias="templateContent")
    response_format: dict = Field(alias="responseFormat")
    enable_history: bool = Field(alias="enableHistory", default=True)
    user_fields: dict = Field(alias="userFields")


class LLMChainState:
    """LLMChain 状态管理"""

    def __init__(self):
        self.initialized = False
        self.llm = None


class ValidationUtils:
    """验证工具类"""

    @staticmethod
    def raise_invalid_params_error(error_msg: str = "") -> None:
        raise build_error(StatusCode.COMPONENT_LLM_CONFIG_INVALID, error_msg=error_msg)


class LLMChain(WorkflowComponent):
    """
    LLMChain 类 - 用于生成对话回复
    完全基于商用版本 jiuwen 中的 LLMChain 实现，适配到 openjiuwen 开源框架
    """

    def __init__(self, conf: dict = None):
        super().__init__()
        self._conf = conf or {}
        self._runtime_context = None
        self._llm = None
        self._metadata = None
        self._state = LLMChainState()
        self._session = None
        self._context = None
        self._initialized = False
        self._stream_final_output: dict | None = None
        self.mem_conf = None

    def init(self, conf: dict, **kwargs):
        """初始化组件配置"""
        self._conf = conf
        self._runtime_context = kwargs.get("runtime_context")
        self._metadata = kwargs.get("metadata")
        self.mem_conf = conf.get("memory", {})
        self._validate_config()

    async def _initialize_if_needed(self):
        """按需初始化模型"""
        if not self._initialized:
            try:
                self._llm = await self._create_llm_instance()
                self._initialized = True
            except Exception as e:
                raise build_error(
                    StatusCode.COMPONENT_LLM_INIT_FAILED,
                    error_msg="Failed to initialize LLM",
                ) from e

    async def _create_llm_instance(self):
        """创建 LLM 模型实例"""
        try:
            from agent_runtime.common.model_adapters import adapt_llm_chain_config
            from agent_runtime.common.model_providers import IRModelConfigProvider

            provider = IRModelConfigProvider()
            adapted_conf = adapt_llm_chain_config(self._conf)
            llm_comp_config = provider.get_llm_config(adapted_conf)

            model = Model(
                model_client_config=llm_comp_config.model_client_config,
                model_config=llm_comp_config.model_config,
            )
            return model

        except ValidationError as e:
            raise build_error(
                StatusCode.COMPONENT_LLM_CONFIG_INVALID,
                error_msg=f"LLM config validation failed: {str(e)}",
            ) from e

    @staticmethod
    def _get_prompt_instance_by_template(template) -> PromptTemplate:
        """获取提示模板实例"""
        return PromptTemplate(template=template)

    async def invoke(self, inputs: dict[str, Any], session, context):
        """LLM 组件非流式调用接口"""
        await self._initialize_if_needed()
        self._session = session
        self._context = context

        inputs_data = inputs.get(USER_FIELDS, {})
        self._process_inputs(inputs_data)

        language_model_inputs = self._get_model_input(inputs=inputs_data)

        try:
            llm_output = await self._llm.invoke(messages=language_model_inputs)

            formatted_res = self._format_response(
                llm_output.content,
                self._get_response_format().get("type"),
                (
                    llm_output.reasoning_content
                    if hasattr(llm_output, "reasoning_content")
                    else None
                ),
            )

            final_output = {USER_FIELDS: formatted_res}
            self._append_usage_metadata(llm_output, final_output)
            return final_output

        except Exception as e:
            raise build_error(
                StatusCode.COMPONENT_LLM_INVOKE_CALL_FAILED,
                error_msg=f"LLM invoke failed: {str(e)}",
            ) from e

    def get_stream_output(self) -> dict | None:
        """Return final batch output after stream completes for downstream ${node_llm...} refs."""
        return self._stream_final_output

    async def stream(self, inputs: dict[str, Any], session, context):
        """LLM 组件流式调用接口 - 支持思考模式"""
        await self._initialize_if_needed()
        self._session = session
        self._context = context
        self._stream_final_output = None

        inputs_data = inputs.get(USER_FIELDS, {})
        self._process_inputs(inputs_data)

        if self._get_response_format().get("type") == "json":
            result = await self.invoke(inputs, session, context)
            self._stream_final_output = result
            yield result
        else:
            language_model_inputs = self._get_model_input(inputs=inputs_data)

            try:
                # 获取用户配置的输出字段名，默认使用 "raw_output"
                outputs_list = self._get_outputs_list_from_conf()
                output_id = "raw_output"
                if outputs_list and len(outputs_list) > 0:
                    output_id = outputs_list[0].get("id", "raw_output")
                if self._is_thinking_enabled():
                    (
                        raw_output_gen,
                        reasoning_content,
                    ) = await self._process_thinking_stream(
                        self._llm.stream(messages=language_model_inputs),
                        language_model_inputs,
                    )
                    async for chunk_data in raw_output_gen:
                        raw_output = chunk_data.get(output_id) or chunk_data.get(
                            "final_output", ""
                        )
                        if chunk_data.get("final_output"):
                            yield {
                                USER_FIELDS: {"reasoning_content": reasoning_content}
                            }
                        else:
                            yield {USER_FIELDS: {output_id: raw_output}}
                else:
                    accumulated_content = ""
                    async for chunk in self._llm.stream(messages=language_model_inputs):
                        if chunk.content and chunk.content != "":
                            if getattr(chunk, "finish_reason", "null") == "null":
                                accumulated_content += chunk.content
                            yield {USER_FIELDS: {output_id: chunk.content}}

                    formatted_res = self._format_response(
                        accumulated_content,
                        self._get_response_format().get("type"),
                    )
                    self._stream_final_output = {USER_FIELDS: formatted_res}
                    yield {USER_FIELDS: {"final_output": formatted_res}}

            except Exception as e:
                raise build_error(
                    StatusCode.COMPONENT_LLM_INVOKE_CALL_FAILED,
                    error_msg=f"LLM stream failed: {str(e)}",
                ) from e

    async def _process_thinking_stream(
        self, model_result, llm_inputs: dict
    ) -> tuple[AsyncGenerator, str]:
        """
        预消费模型流，分离 content 和 reasoning_content

        工作流程：
        1. 完整遍历模型返回流
        2. 收集所有 content chunks 到列表
        3. 累积所有 reasoning_content 到字符串
        4. 基于缓存的 content_chunks 构造异步迭代器

        Returns:
            tuple: (rawOutput generator, reasoning_content 完整字符串)
        """
        content_chunks: list[str] = []
        reasoning_content: str = ""

        state = {
            "start_time": time.perf_counter(),
            "is_first_token": True,
        }

        try:
            if asyncio.iscoroutine(model_result):
                result_iter = await model_result
            else:
                result_iter = model_result

            reasoning_content = await self._consume_llm_stream(
                content_chunks, reasoning_content, result_iter, state
            )

        except Exception as e:
            raise build_error(
                StatusCode.COMPONENT_LLM_INVOKE_CALL_FAILED,
                error_msg=f"LLM stream failed: {str(e)}",
            ) from e

        accumulated_content = "".join(content_chunks)
        formatted_res = self._format_response(
            accumulated_content,
            self._get_response_format().get("type"),
            reasoning_content,
        )
        self._stream_final_output = {USER_FIELDS: formatted_res}

        async def raw_output_generator():
            """基于缓存的 content_chunks 重放生成流式数据"""
            accumulated_content = ""
            # 获取用户配置的输出字段名，默认使用 "raw_output"
            outputs_list = self._get_outputs_list_from_conf()
            output_id = "raw_output"
            if outputs_list and len(outputs_list) > 0:
                output_id = outputs_list[0].get("id", "raw_output")

            for chunk in content_chunks:
                if getattr(chunk, "finish_reason", "null") == "null":
                    accumulated_content += chunk
                yield {output_id: chunk}

            formatted_res = self._format_response(
                accumulated_content,
                self._get_response_format().get("type"),
                reasoning_content,
            )
            # 确保 output 中有 reasoning_content 字段
            if isinstance(formatted_res, dict):
                if (
                    "reasoning_content" not in formatted_res
                    or not formatted_res["reasoning_content"]
                ):
                    formatted_res["reasoning_content"] = reasoning_content

            yield {"final_output": formatted_res}

        return raw_output_generator(), reasoning_content

    async def _consume_llm_stream(
        self, content_chunks, reasoning_content, result_iter, state
    ):
        """消费 LLM 流，分离 content 和 reasoning_content"""
        node_id = self._session.get_component_id() if self._session else ""
        node_name = self._conf.get("name") or node_id
        index = 0

        async for item in result_iter:
            if state["is_first_token"]:
                workflow_logger.info(
                    f"first_token<llm>|{round((time.perf_counter() - state['start_time']) * 1000)}"
                )
                state["is_first_token"] = False

            if hasattr(item, "reasoning_content") and item.reasoning_content:
                reasoning_content += item.reasoning_content
                if self._session:
                    await self._session.write_custom_stream(
                        CustomSchema(
                            type=PARTIAL_CONTENT,
                            index=index,
                            data={
                                "answer": "",
                                "think": item.reasoning_content,
                                "node_id": node_id,
                                "node_name": node_name,
                                "node_type": JIUWEN_LLM_TYPE,
                                "should_interrupt": False,
                            },
                        )
                    )
                    index += 1

            if item.content:
                content_chunks.append(item.content)

        return reasoning_content

    def _format_response(
        self, content: str, response_type: str, reasoning_content: str = None
    ) -> dict:
        """格式化响应 - 支持 JSON/Markdown/Text 格式"""
        outputs_list = self._get_outputs_list_from_conf()
        result = {}

        if response_type == "json":
            parsed = self._extract_json(content)
            for output in outputs_list:
                output_id = output.get("id")
                if not output_id:
                    continue
                if output_id == "reasoning_content" and reasoning_content is not None:
                    result[output_id] = reasoning_content
                elif parsed and output_id in parsed:
                    result[output_id] = parsed[output_id]
                else:
                    result[output_id] = content
        else:
            for output in outputs_list:
                output_id = output.get("id")
                if not output_id:
                    continue
                if output_id == "reasoning_content" and reasoning_content is not None:
                    result[output_id] = reasoning_content
                else:
                    result[output_id] = content

        if not result:
            result = {"raw_output": content}

        return result

    @staticmethod
    def _extract_json(text: str) -> Optional[dict]:
        """从 LLM 输出中提取 JSON 对象"""
        if not text:
            return None
        text = text.strip()
        try:
            parsed = json.loads(text)
            if isinstance(parsed, dict):
                return parsed
        except json.JSONDecodeError:
            pass
        match = re.search(r"```(?:json)?\s*\n?(.*?)\n?```", text, re.DOTALL)
        if match:
            try:
                parsed = json.loads(match.group(1).strip())
                if isinstance(parsed, dict):
                    return parsed
            except json.JSONDecodeError:
                pass
        return None

    def _get_chat_history(self) -> list[dict]:
        """从 context 获取对话历史"""
        if not self._context:
            return []
        try:
            messages = self._context.get_messages(with_history=True)
            return [self._message_to_dict(msg) for msg in messages]
        except Exception:
            return []

    @staticmethod
    def _message_to_dict(msg) -> dict:
        """将 BaseMessage 转换为 {role, content} 字典"""
        if isinstance(msg, dict):
            return msg
        role = getattr(msg, "type", "") or getattr(msg, "role", "")
        content = getattr(msg, "content", "")
        role = MESSAGE_TYPE_TO_ROLE.get(role, role)
        return {"role": role, "content": content}

    def _get_history(
        self, user_prompt: str, system_prompt: Optional[str] = None
    ) -> list[dict]:
        """构建包含对话历史的消息列表"""
        chat_history = self._get_chat_history()
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        if chat_history:
            # 历史消息不含最后一条（当前用户消息由模板渲染提供）
            for history in chat_history[:-1][-CHAT_HISTORY_MAX_TURN:]:
                role = history.get("role", "user")
                content = history.get("content", "")
                if role in ("user", "assistant", "system") and content:
                    messages.append({"role": role, "content": content})
        messages.append({"role": "user", "content": user_prompt})
        return messages

    def _append_usage_metadata(self, llm_output, final_output: dict):
        """添加使用信息"""
        if (
            llm_output
            and hasattr(llm_output, "usage_metadata")
            and llm_output.usage_metadata
        ):
            usage = llm_output.usage_metadata
            final_output.update(
                {
                    "input_tokens": getattr(usage, "input_tokens", 0),
                    "output_tokens": getattr(usage, "output_tokens", 0),
                    "total_tokens": getattr(usage, "total_tokens", 0),
                }
            )

    def _get_model_input(self, inputs: dict) -> List[dict]:
        """获取模型输入，包含系统提示和对话历史"""
        prompt_template = self._get_template_content()
        try:
            self._validate_prompt_template(prompt_template)
            user_prompt = self._render_prompt(prompt_template, inputs)
        except Exception as e:
            raise build_error(
                StatusCode.COMPONENT_LLM_CONFIG_INVALID,
                error_msg="Failed to assemble llm template",
            ) from e

        system_template = self._get_system_template_content()
        system_prompt = None
        if system_template:
            try:
                self._validate_prompt_template(system_template)
                system_prompt = self._render_prompt(system_template, inputs)
            except Exception:
                system_prompt = system_template

        if self._get_enable_history():
            return self._get_history(user_prompt, system_prompt)
        messages = []
        if system_prompt:
            messages.append({"role": "system", "content": system_prompt})
        messages.append({"role": "user", "content": user_prompt})
        return messages

    def _validate_prompt_template(self, template: str) -> None:
        """
        在 invoke 之前校验模板源，拦截危险占位符（如 query.__class__、表达式等）。
        只依赖模板字符串，不依赖运行时 inputs。
        """
        if not template:
            return

        texts: list[str] = [template]

        placeholder_find_pattern = re.compile(r"\{\{([^{}]*)\}\}")
        placeholder_safe_pattern = re.compile(
            r"^[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z_][a-zA-Z0-9_]*)*$"
        )

        for text in texts:
            for match in placeholder_find_pattern.finditer(text):
                placeholder = match.group(1).strip()

                if (
                    not placeholder
                    or "__" in placeholder
                    or not placeholder_safe_pattern.fullmatch(placeholder)
                ):
                    raise ValueError(
                        f"Invalid or dangerous placeholder: '{placeholder}'"
                    )

    def _render_prompt(self, template: str, inputs: dict) -> str:
        """渲染提示模板"""
        result = template
        for key, value in inputs.items():
            placeholder = f"{{{{{key}}}}}"
            if placeholder in result:
                result = result.replace(placeholder, str(value))
        return result

    def _get_template_content(self) -> str:
        """获取用户模板内容"""
        template_content_list = self._conf.get("templateContent", [])
        user_prompts = [
            element for element in template_content_list if element.get(_ROLE) == "user"
        ]

        if not user_prompts or not isinstance(user_prompts[0], dict):
            raise build_error(
                StatusCode.COMPONENT_LLM_CONFIG_INVALID,
                error_msg="Failed to retrieve llm template content",
            )

        return str(user_prompts[0].get("content", ""))

    def _get_system_template_content(self) -> Optional[str]:
        """获取系统模板内容"""
        template_content_list = self._conf.get("templateContent", [])
        system_prompts = [
            element
            for element in template_content_list
            if element.get(_ROLE) == "system"
        ]
        if not system_prompts:
            return None
        return str(system_prompts[0].get("content", ""))

    def _get_outputs_list_from_conf(self) -> List[dict]:
        """获取输出列表"""
        return self._conf.get(USER_FIELDS, {}).get("outputs", [])

    def _get_outputs_conf(self) -> dict:
        """
        获取特定格式的conf中定义的组件输出
        example:
            {"xxx": {"type": "xx", "description": "xx"}}
        """
        outputs_config_list = self._get_outputs_list_from_conf()
        return {config.get("id"): config for config in outputs_config_list}

    def _get_response_format(self):
        """获取响应格式"""
        return self._conf.get("responseFormat", {"type": "text"})

    def _validate_config(self):
        """验证配置"""
        try:
            LLMChainConfig.model_validate(self._conf)
            self._validate_thinking_mode()

            response_config = self._get_response_format()
            if not self._is_thinking_enabled() and response_config.get(_TYPE) in [
                "markdown",
                "text",
            ]:
                if len(self._get_outputs_list_from_conf()) != 1:
                    raise build_error(
                        StatusCode.COMPONENT_LLM_CONFIG_INVALID,
                        error_msg="When type in responseFormat is markdown or text, there is only one user-defined output",
                    )
            else:
                if len(self._get_outputs_list_from_conf()) < 1:
                    raise build_error(
                        StatusCode.COMPONENT_LLM_CONFIG_INVALID,
                        error_msg="When type in responseFormat is set to JSON, at least one user-defined output is required",
                    )

        except ValidationError as e:
            raise build_error(
                StatusCode.COMPONENT_LLM_CONFIG_INVALID,
                error_msg=f"Invalid LLM config: {str(e)}",
            )

    def _validate_thinking_mode(self):
        """验证思考模式配置"""
        thinking_config = (
            self._conf.get("model", {}).get("hyperParameters", {}).get("thinking")
        )
        if thinking_config and thinking_config.get("type") not in [
            "enabled",
            "disabled",
        ]:
            raise build_error(
                StatusCode.COMPONENT_LLM_CONFIG_INVALID,
                error_msg="model thinking type is not valid",
            )

    def _is_thinking_enabled(self) -> bool:
        """检查是否开启了思考模式"""
        thinking_config = (
            self._conf.get("model", {}).get("hyperParameters", {}).get("thinking", {})
        )
        if not thinking_config.get("type"):
            thinking_config["type"] = "enabled"
        return thinking_config.get("type") == "enabled"

    def _get_enable_history(self) -> bool:
        """获取是否启用历史记录"""
        return self._conf.get("enableHistory", False)

    def _process_inputs(self, inputs: dict):
        """处理输入数据 - 预埋 CHAT_HISTORY 变量"""
        chat_history = self._get_chat_history()
        # 排除最后一条（当前用户消息），只保留历史对话
        history_only = chat_history[:-1] if chat_history else []
        full_input = ""
        for history in history_only[-CHAT_HISTORY_MAX_TURN:]:
            full_input += "{}：{}\n".format(
                ROLE_MAP.get(history.get("role", "user"), "用户"),
                history.get("content"),
            )
        inputs.update({"CHAT_HISTORY": full_input})
