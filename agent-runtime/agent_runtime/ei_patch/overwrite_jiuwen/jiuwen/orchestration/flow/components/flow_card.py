#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
This module contains the FlowCard class, which is a subclass of Invokable.
FlowCard is responsible for returning formatted card outputs in workflow executing process.
"""

import datetime
import json
import uuid

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.common.log.base import logger
from jiuwen.controller.common.task_type import TaskType
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.callbacks.span import Span
from jiuwen.orchestration.flow.components.message_base import (
    MessageComponentBase,
    STRUCT_OUTPUT_TEMPLATE,
    ENABLE_STRUCT_MESSAGE,
)
from jiuwen.orchestration.flow.components.util import (
    flow_card_process_generator_values_of_dict,
)
from jiuwen.orchestration.flow.constant import WORKFLOW_CHAT_MANAGER, USER_FIELDS
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.orchestration.flow.model.workflow_data_class import (
    WorkflowMetadata,
    WorkflowStreamingData,
)
from jiuwen.orchestration.flow.outputmode import OutputMode
from jiuwen.orchestration.flow.runtime_context import RuntimeContext
from jiuwen.prompt import Prompt, Template
from pydantic import BaseModel, StrictStr, Field

CONFIG_TEMPLATE_KEYWORD = "template"
CARD_RUNTIME_KEYWORD = "card_outputs"
BJ_TZ = datetime.timezone(
    datetime.timedelta(hours=8),
    name="Asia/Beijing",
)


class FlowCardComponentConfig(BaseModel):
    """
    卡片组件 配置校验
    """

    template: StrictStr = Field(min_length=1)


class FlowCard(Invokable, MessageComponentBase):
    """Card node."""

    def __init__(
        self, conf: dict, runtime_context: RuntimeContext, metadata: WorkflowMetadata
    ):
        if CONFIG_TEMPLATE_KEYWORD not in conf:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_TEMPLATE_NOT_FOUND_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_TEMPLATE_NOT_FOUND_ERROR.code,
            )
        raw_mode = conf.get("outputMode")
        self.output_mode = OutputMode.from_str(raw_mode)
        self.origin_template_conf = conf[CONFIG_TEMPLATE_KEYWORD]
        self.metadata = metadata
        self.template = Prompt(
            template=Template(name="card_template", content=self.origin_template_conf)
        )
        if not isinstance(runtime_context.get(CARD_RUNTIME_KEYWORD), list):
            runtime_context.set(CARD_RUNTIME_KEYWORD, [])
        # give global runtime_context reference to this class instance, and update in invoke()
        self.runtime_context = runtime_context
        self.chat_manager = runtime_context.get(WORKFLOW_CHAT_MANAGER)
        self.node_type = NodeType.CARD.value
        self.end_interrupt = False
        self.event = conf.get("event", {})
        self.struct_output_template = conf.get(STRUCT_OUTPUT_TEMPLATE, "")
        self.enable_struct_message = conf.get(ENABLE_STRUCT_MESSAGE, False)
        # 卡片节点超参数
        raw_metadata = conf.get("metaData", "")
        config_metadata = self._safe_load_config_metadata(
            raw_metadata
        )  # IR中此字段未转义
        self.card_metadata = {
            "version": conf.get("version"),
            "resourceType": conf.get("resourceType"),
            "resourceUrl": conf.get("resource_url"),
            "card_code": conf.get("cardCode"),
            "config_metadata": config_metadata,
            "pack": conf.get("pack"),
            "instance_id": str(uuid.uuid4()),  # 运行时生成instance_id
        }

        if self.event and self.event.get("type", "") == TaskType.TASK_COMPLETION:
            self.end_interrupt = True

    def invoke(self, inputs, **kwargs):
        """Invoke to assemble card outputs."""
        pass

    async def ainvoke(self, inputs: dict, **kwargs):
        """Asynchronous invoke to assemble end outputs."""
        stream_callback = kwargs.get("stream_callback")

        inputs = inputs.get(USER_FIELDS)
        span: Span = kwargs.get("span")
        stream_related_info = WorkflowStreamingData(
            stream_callback=stream_callback, execution_id=span.trace_id
        )
        try:
            output_mode_val = self.output_mode.value if self.output_mode else None
            inputs = await flow_card_process_generator_values_of_dict(
                origin_template=self.origin_template_conf,
                inputs=inputs,
                workflow_streaming_data=stream_related_info,
                metadata=self.metadata,
                output_mode=output_mode_val,
                card_metadata=self.card_metadata,
            )
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_AINVOKE_UNEXPECTED_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_AINVOKE_UNEXPECTED_ERROR.code,
            ) from e
        try:
            final_res = self.template.invoke(inputs)
        except JiuWenBaseException:
            raise
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_TEMPLATE_ASSEMBLE_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_TEMPLATE_ASSEMBLE_ERROR.code,
            ) from e

        return dict(result=final_res)

    def _safe_load_config_metadata(self, raw_metadata: str) -> dict:
        """
        安全地从 conf['metadata'] 中加载 JSON 配置。
        支持转义字符串（如 "\\r\\n", "\\\""），返回 dict 或空 dict。

        参数:
        raw_metadata (str): 原始的元数据字符串，期望是 JSON 格式。

        返回:
        dict: 解析后的 JSON 数据，如果解析失败则返回空字典。

        异常:
        UnicodeDecodeError: 如果元数据字符串无法解码。
        json.JSONDecodeError: 如果元数据字符串不是有效的 JSON。
        Exception: 捕获并记录任何其他未预期的异常。
        """

        if not raw_metadata or not isinstance(raw_metadata, str):
            return {}

        try:
            # 解析 JSON
            config_metadata = json.loads(raw_metadata)

            if not isinstance(config_metadata, dict):
                logger.warning(
                    "metadata parsed as non-dict: %s", type(config_metadata).__name__
                )
                return {}

            return config_metadata

        except UnicodeDecodeError as e:
            logger.warning("Card Failed to unescape metadata string: %s", e)
        except json.JSONDecodeError as e:
            logger.warning(
                "Card IR Invalid JSON in metadata: %s (raw: %r)", e, raw_metadata[:100]
            )
        except Exception as e:
            logger.error("Unexpected error parsing metadata: %s", e, exc_info=True)
        return {}
