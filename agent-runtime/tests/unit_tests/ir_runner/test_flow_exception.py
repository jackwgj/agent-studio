# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
"""Unit tests for ExceptionInfo component — verify session.trace() writes onInvokeData."""

from unittest.mock import AsyncMock, MagicMock, call

import pytest

from jiuwen.extension.workflow_node.flow_exception import ExceptionInfo


def _make_mock_session():
    """Create a mock Session with async trace/write methods."""
    session = MagicMock()
    session.trace = AsyncMock()
    session.write_custom_stream = AsyncMock()
    session.get_global_state = MagicMock(return_value=None)
    session.update_global_state = MagicMock()
    return session


class TestExceptionInfoTrace:
    """Test that ExceptionInfo writes exception data to tracer onInvokeData via session.trace()."""

    @pytest.mark.asyncio
    async def test_invoke_writes_trace_data(self):
        """invoke() should call session.trace() with error_code and jiuwen_exception_node_id."""
        session = _make_mock_session()
        exc_info = ExceptionInfo(
            node_id="node_exc",
            node_name="异常",
            node_type="jiuwen.exception",
        )
        inputs = {"userFields": {"error_code": "10025"}}
        context = MagicMock()

        with pytest.raises(Exception):
            # WorkflowAbortException is expected
            await exc_info.invoke(inputs, session, context)

        session.trace.assert_awaited_once_with(
            {"error_code": "10025", "jiuwen_exception_node_id": "node_exc"}
        )

    @pytest.mark.asyncio
    async def test_invoke_trace_before_custom_stream(self):
        """session.trace() should be called before session.write_custom_stream()."""
        session = _make_mock_session()
        exc_info = ExceptionInfo(
            node_id="node_exc",
            node_name="异常",
            node_type="jiuwen.exception",
        )
        inputs = {"userFields": {"error_code": "10025"}}
        context = MagicMock()

        # Track call order
        call_order = []
        session.trace = AsyncMock(side_effect=lambda *a, **k: call_order.append("trace"))
        session.write_custom_stream = AsyncMock(
            side_effect=lambda *a, **k: call_order.append("custom_stream")
        )

        with pytest.raises(Exception):
            await exc_info.invoke(inputs, session, context)

        assert call_order == ["trace", "custom_stream"]

    @pytest.mark.asyncio
    async def test_invoke_no_user_fields(self):
        """invoke() with no userFields should still write jiuwen_exception_node_id to trace."""
        session = _make_mock_session()
        exc_info = ExceptionInfo(
            node_id="node_exc",
            node_name="异常",
            node_type="jiuwen.exception",
        )
        inputs = None
        context = MagicMock()

        with pytest.raises(Exception):
            await exc_info.invoke(inputs, session, context)

        session.trace.assert_awaited_once_with(
            {"jiuwen_exception_node_id": "node_exc"}
        )

    @pytest.mark.asyncio
    async def test_stream_writes_trace_data(self):
        """stream() should also call session.trace() with the same data as invoke()."""
        session = _make_mock_session()
        exc_info = ExceptionInfo(
            node_id="node_exc",
            node_name="异常",
            node_type="jiuwen.exception",
        )
        inputs = {"userFields": {"error_code": "10025"}}
        context = MagicMock()

        with pytest.raises(Exception):
            async for _ in exc_info.stream(inputs, session, context):
                pass

        session.trace.assert_awaited_once_with(
            {"error_code": "10025", "jiuwen_exception_node_id": "node_exc"}
        )

    @pytest.mark.asyncio
    async def test_invoke_abort_guard_skips_trace(self):
        """When __abort__ is already set, invoke() should NOT call session.trace()
        (the exception was already handled by another concurrent exception node)."""
        session = _make_mock_session()
        # Simulate another exception node already triggered
        session.get_global_state = MagicMock(return_value=True)

        exc_info = ExceptionInfo(
            node_id="node_exc_2",
            node_name="异常2",
            node_type="jiuwen.exception",
        )
        inputs = {"userFields": {"error_code": "10025"}}
        context = MagicMock()

        with pytest.raises(Exception):
            await exc_info.invoke(inputs, session, context)

        # trace should NOT be called for the second concurrent exception node
        session.trace.assert_not_called()
