#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2026. All rights reserved.

"""
This module contains the FlowCard class, which is a subclass of Invokable.
FlowCard is responsible for returning formatted card outputs in workflow executing process.
"""

import datetime

from jiuwen.common.exception.base import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from jiuwen.controller.common.task_type import TaskType
from jiuwen.orchestration import Invokable
from jiuwen.orchestration.callbacks.span import Span
from jiuwen.orchestration.flow.components.message_base import (
    MessageComponentBase,
    STRUCT_OUTPUT_TEMPLATE,
    ENABLE_STRUCT_MESSAGE,
)
from jiuwen.orchestration.flow.components.util import process_generator_values_of_dict
from jiuwen.orchestration.flow.constant import WORKFLOW_CHAT_MANAGER, USER_FIELDS
from jiuwen.orchestration.flow.enum import NodeType
from jiuwen.orchestration.flow.model.workflow_data_class import (
    WorkflowMetadata,
    WorkflowStreamingData,
)
from jiuwen.orchestration.flow.outputmode import OutputMode
from jiuwen.orchestration.flow.runtime_context import RuntimeContext
from jiuwen.prompt import Prompt, Template
from pydantic import BaseModel, StrictStr, Field, ValidationError

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
        if self.event and self.event.get("type", "") == TaskType.TASK_COMPLETION:
            self.end_interrupt = True

    def invoke(self, inputs, **kwargs):
        ...

    async def ainvoke(self, inputs, **kwargs):
        """Invoke to assemble card outputs."""
        try:
            flow_card = await self.template.ainvoke(inputs.get(USER_FIELDS))
        except JiuWenBaseException:
            raise
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_TEMPLATE_ASSEMBLE_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_TEMPLATE_ASSEMBLE_ERROR.code,
            ) from e

        try:
            # a list which refers from global runtime_context
            card_outputs = self.runtime_context.get(CARD_RUNTIME_KEYWORD)
            card_outputs.append(self._format_card_output(output=flow_card))
            return dict(result=flow_card)

        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_INVOKE_UNEXPECTED_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_INVOKE_UNEXPECTED_ERROR.code,
            ) from e

    async def ainvoke(self, inputs: dict, **kwargs):
        """Asynchronous invoke to assemble end outputs."""
        stream_callback = kwargs.get("stream_callback")
        if stream_callback is None:
            return self.invoke(inputs, **kwargs)

        inputs = inputs.get(USER_FIELDS)
        span: Span = kwargs.get("span")
        stream_related_info = WorkflowStreamingData(
            stream_callback=stream_callback, execution_id=span.trace_id
        )
        try:
            output_mode_val = self.output_mode.value if self.output_mode else None
            inputs = await process_generator_values_of_dict(
                origin_template=self.origin_template_conf,
                inputs=inputs,
                workflow_streaming_data=stream_related_info,
                metadata=self.metadata,
                output_mode=output_mode_val,
            )
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_AINVOKE_UNEXPECTED_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_AINVOKE_UNEXPECTED_ERROR.code,
            ) from e

        try:
            final_res = await self.template.ainvoke(inputs)
        except JiuWenBaseException:
            raise
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_TEMPLATE_ASSEMBLE_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_TEMPLATE_ASSEMBLE_ERROR.code,
            ) from e

        try:
            output_mode_val = self.output_mode.value if self.output_mode else None
            struct_answer_item = (
                dict(
                    struct_output_template=self.struct_output_template, variables=inputs
                )
                if self.enable_struct_message and self.struct_output_template
                else {}
            )
            await self.format_message_output(
                origin_output=final_res,
                struct_answer_item=struct_answer_item,
                metadata=self.metadata,
                stream_data=stream_related_info,
                output_mode=output_mode_val,
            )
            return dict(result=final_res)
        except Exception as e:
            raise JiuWenBaseException(
                message=StatusCode.WORKFLOW_CARD_AINVOKE_UNEXPECTED_ERROR.errmsg,
                error_code=StatusCode.WORKFLOW_CARD_AINVOKE_UNEXPECTED_ERROR.code,
            ) from e

    def _format_card_output(self, output):
        return dict(
            node_type=self.node_type,
            output=output,
            time_stamp=datetime.datetime.now(tz=BJ_TZ).isoformat(),
        )

    def _validate_configs(self):
        try:
            FlowCardComponentConfig.model_validate(
                dict(template=self.origin_template_conf)
            )
        except ValidationError as e:
            raise JiuWenBaseException(
                error_code=StatusCode.WORKFLOW_CARD_INIT_ERROR.code,
                message=StatusCode.WORKFLOW_CARD_INIT_ERROR.errmsg,
            ) from e
