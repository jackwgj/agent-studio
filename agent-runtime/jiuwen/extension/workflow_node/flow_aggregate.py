# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
"""
Variable aggregate node (aligned with jiuwen ``FlowAggregate``).

Merges multiple upstream user-field keys into named outputs using a strategy.
Currently supports ``first-non-null`` (same semantics as jiuwen).
"""

from __future__ import annotations

from typing import Any, AsyncGenerator, Union

from openjiuwen.core.common.constants.constant import USER_FIELDS
from openjiuwen.core.common.exception.codes import StatusCode
from openjiuwen.core.common.exception.errors import build_error
from openjiuwen.core.common.logging import LogEventType, workflow_logger
from openjiuwen.core.context_engine import ModelContext
from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.session.node import Session
from openjiuwen.core.workflow.components.component import WorkflowComponent
from pydantic import BaseModel, ConfigDict, Field


class GroupConfig(BaseModel):
    """One aggregate group: output id + ordered source keys to scan."""

    id: str
    value_list: list[str] = Field(default_factory=list)

    model_config = ConfigDict(extra="ignore")


class AggregateConfig(BaseModel):
    """IR / runtime configuration for :class:`Aggregate`."""

    mode: str = Field(default="first-non-null", alias="mode")
    groups: Union[dict[str, Any], list[Any], None] = None

    model_config = ConfigDict(populate_by_name=True, extra="ignore")

    def groups_map(self) -> dict[str, list[str]]:
        raw = self.groups
        if raw is None:
            return {}
        if isinstance(raw, dict):
            normalized: dict[str, list[str]] = {}
            for k, v in raw.items():
                if isinstance(v, (list, tuple)):
                    normalized[str(k)] = [str(x) for x in v]
                else:
                    normalized[str(k)] = [str(v)]
            return normalized
        if isinstance(raw, list):
            out: dict[str, list[str]] = {}
            for item in raw:
                if isinstance(item, GroupConfig):
                    out[item.id] = list(item.value_list)
                elif isinstance(item, dict):
                    gc = GroupConfig.model_validate(item)
                    out[gc.id] = list(gc.value_list)
            return out
        return {}


class Aggregate(WorkflowComponent):
    """
    Picks one value per output group from ``userFields`` (or flat inputs).

    Return shape matches jiuwen: ``{ "userFields": { out_key: value, ... } }``.
    """

    def __init__(self, conf: Union[AggregateConfig, dict, None] = None):
        super().__init__()
        if not conf:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="aggregate",
                reason="conf is required",
                workflow="n/a",
            )
        try:
            self._conf = (
                AggregateConfig.model_validate(conf)
                if not isinstance(conf, AggregateConfig)
                else conf
            )
        except Exception as e:
            raise build_error(
                StatusCode.WORKFLOW_COMPONENT_SCHEMA_INVALID,
                comp_id="aggregate",
                reason=str(e),
                workflow="n/a",
                cause=e,
            )

    def component_type(self) -> str:
        return "aggregate"

    @staticmethod
    def _first_non_empty(lst: list) -> Any:
        if not lst:
            return None
        if not isinstance(lst[0], str):
            return next((x for x in lst if x is not None), None)
        return next((x for x in lst if x), None)

    @staticmethod
    def _validate_param_type(lst: list) -> None:
        if not lst:
            return
        # None values are valid in first-non-null mode (unexecuted branches)
        non_null = [item for item in lst if item is not None]
        if not non_null:
            return
        if isinstance(non_null[0], dict):
            Aggregate._validate_dict_value_types(non_null)
            return
        for item in non_null:
            if not isinstance(item, type(non_null[0])):
                raise build_error(
                    StatusCode.WORKFLOW_EXECUTE_INPUT_INVALID,
                    inputs="aggregate",
                    reason="group value types differ",
                    workflow="n/a",
                )

    @staticmethod
    def _validate_dict_value_types(lst: list) -> None:
        template = lst[0]
        keys = template.keys()
        for item in lst:
            for key in keys:
                if key not in item or not isinstance(item[key], type(template[key])):
                    raise build_error(
                        StatusCode.WORKFLOW_EXECUTE_INPUT_INVALID,
                        inputs="aggregate",
                        reason=f"dict group key '{key}' type mismatch",
                        workflow="n/a",
                    )

    async def invoke(
        self, inputs: Input, session: Session, context: ModelContext
    ) -> Output:
        workflow_logger.debug(
            "Aggregate component invoke started",
            event_type=LogEventType.WORKFLOW_COMPONENT_START,
            component_id=session.get_component_id(),
            component_type_str="Aggregate",
            session_id=session.get_session_id(),
        )
        mode = self._conf.mode
        groups = self._conf.groups_map()
        user: dict[str, Any] = {}
        if isinstance(inputs, dict):
            inner = inputs.get(USER_FIELDS)
            user = inner if isinstance(inner, dict) else inputs
        res: dict[str, Any] = {}
        if mode == "first-non-null":
            for out_key, item_list in groups.items():
                value_list = [user.get(item, None) for item in item_list]
                self._validate_param_type(value_list)
                res[out_key] = self._first_non_empty(value_list)
        else:
            raise build_error(
                StatusCode.WORKFLOW_EXECUTE_INPUT_INVALID,
                inputs="aggregate",
                reason=f"unsupported aggregate mode: {mode}",
                workflow="n/a",
            )
        workflow_logger.debug(
            "Aggregate component invoke succeed",
            event_type=LogEventType.WORKFLOW_COMPONENT_END,
            component_id=session.get_component_id(),
            component_type_str="Aggregate",
            session_id=session.get_session_id(),
        )
        return {USER_FIELDS: res}

    async def collect(
        self, inputs: Input, session: Session, context: ModelContext
    ) -> Output:
        """COLLECT pattern: consume stream input from upstream LLMChain etc.,
        resolve all async generators to concrete values, then apply aggregate logic.
        """
        workflow_logger.info(
            "Aggregate component collect started",
            event_type=LogEventType.WORKFLOW_COMPONENT_START,
            component_id=session.get_component_id(),
            component_type_str="Aggregate",
            session_id=session.get_session_id(),
        )
        resolved_inputs = await self._resolve_stream_inputs(inputs)
        result = await self.invoke(resolved_inputs, session, context)
        workflow_logger.debug(
            "Aggregate component collect succeed",
            event_type=LogEventType.WORKFLOW_COMPONENT_END,
            component_id=session.get_component_id(),
            component_type_str="Aggregate",
            session_id=session.get_session_id(),
        )
        return result

    @staticmethod
    async def _resolve_stream_inputs(inputs: Input) -> dict:
        """Convert any AsyncGenerator values in inputs to their full collected values."""
        if not isinstance(inputs, dict):
            return inputs
        resolved: dict[str, Any] = {}
        inner = inputs.get(USER_FIELDS)
        if isinstance(inner, dict):
            resolved_inner: dict[str, Any] = {}
            for key, value in inner.items():
                if isinstance(value, AsyncGenerator):
                    chunks: list[str] = []
                    async for chunk in value:
                        if chunk is not None:
                            chunks.append(str(chunk))
                    resolved_inner[key] = "".join(chunks) if chunks else None
                else:
                    resolved_inner[key] = value
            resolved[USER_FIELDS] = resolved_inner
        else:
            for key, value in inputs.items():
                if isinstance(value, AsyncGenerator):
                    chunks: list[str] = []
                    async for chunk in value:
                        if chunk is not None:
                            chunks.append(str(chunk))
                    resolved[key] = "".join(chunks) if chunks else None
                else:
                    resolved[key] = value
        return resolved
