# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
"""Tests for conversation tool describe capability (方案 A2) and usage event extraction."""

import json
from unittest.mock import AsyncMock, patch

import pytest

from agent_runtime.conversation.usage import (
    ConversationUsageObserver,
    ConversationUsageRail,
)
from jiuwen.common.llm_service.messages import UsageMetadata
from openjiuwen.core.single_agent.rail.base import AgentRail
from agent_runtime.conversation.tool.describe import collect_tool_specs


class TestCollectToolSpecs:
    """collect_tool_specs —— 按请求收集本请求所需工具描述。"""

    @staticmethod
    @pytest.mark.asyncio
    async def test_supervisor_handoff_spec():
        """SUPERVISOR：每个子 Agent 一个 handoff 工具，type=inner，source=supervisor_builtin。"""
        with patch("agent_runtime.conversation.tool.describe.async_ir_load",
                   new=AsyncMock(return_value={"agentName": "资料分析", "description": "负责资料检索与汇总"})), \
             patch("agent_runtime.supervisor.builder.async_ir_load",
                   new=AsyncMock(return_value={"agentName": "资料分析", "description": "负责资料检索与汇总"})):
            specs = await collect_tool_specs("SUPERVISOR", ["agent-123"])
        assert len(specs) == 1
        spec = specs[0]
        assert spec["tool_id"] == "handoff_agent-123"
        assert spec["tool_display_name"] == "资料分析"
        assert spec["tool_desc"] == "负责资料检索与汇总"
        assert spec["type"] == "inner"
        assert spec["source"] == "supervisor_builtin"
        metadata = json.loads(spec["metadata"])
        assert metadata == {"tool_type": "handoff", "target_agent_id": "agent-123"}
        assert "query" in json.loads(spec["input_schema"])["properties"]

    @staticmethod
    @pytest.mark.asyncio
    async def test_supervisor_handoff_fallback_when_ir_unavailable():
        """子 Agent IR 加载失败时回退默认展示名/描述，不抛错。"""
        with patch("agent_runtime.conversation.tool.describe.async_ir_load",
                   new=AsyncMock(side_effect=RuntimeError("obs down"))), \
             patch("agent_runtime.supervisor.builder.async_ir_load",
                   new=AsyncMock(side_effect=RuntimeError("obs down"))):
            specs = await collect_tool_specs("SUPERVISOR", ["agent-456"])
        assert len(specs) == 1
        assert specs[0]["tool_id"] == "handoff_agent-456"
        assert specs[0]["tool_display_name"]  # 回退名非空
        assert specs[0]["tool_desc"]  # 回退描述非空

    @staticmethod
    @pytest.mark.asyncio
    async def test_app_returns_empty_for_now():
        """APP 路径暂不产出工具（通用内置清单为空），返回空列表。"""
        specs = await collect_tool_specs("APP", [])
        assert specs == []


class TestUsageObserver:
    """ReAct Rail 与 Controller Trace Handler 共用的业务观察器。"""

    @staticmethod
    def test_deduplicates_and_emits_canonical_event():
        observer = ConversationUsageObserver("exec-1")
        usage = UsageMetadata(input_tokens=10, output_tokens=20, total_tokens=30)
        first = observer.record(invocation_id="llm-1", run_id="run-1", usage=usage)
        duplicate = observer.record(invocation_id="llm-1", run_id="run-1", usage=usage)
        assert first == duplicate
        events = observer.drain_events("conv-1")
        assert len(events) == 1
        assert events[0]["data"]["invocationId"] == "llm-1"
        assert events[0]["data"]["usage"]["total_tokens"] == 30
        assert observer.drain_events("conv-1") == []

    @staticmethod
    def test_react_rail_implements_official_agent_rail_contract():
        observer = ConversationUsageObserver("exec-1")
        rail = ConversationUsageRail(observer, "run-1")
        assert isinstance(rail, AgentRail)
        rail.init(object())
        rail.uninit(object())

    @staticmethod
    @pytest.mark.asyncio
    async def test_react_rail_reads_response_usage():
        observer = ConversationUsageObserver("exec-1")
        rail = ConversationUsageRail(observer, "run-1")
        response = type("Response", (), {"usage_metadata": UsageMetadata(input_tokens=1, output_tokens=2, total_tokens=3)})()
        ctx = type("Context", (), {"extra": {}, "inputs": type("Inputs", (), {"response": response})()})()
        await rail.before_model_call(ctx)
        await rail.after_model_call(ctx)
        assert observer.drain_events("conv-1")[0]["data"]["usage"]["total_tokens"] == 3
