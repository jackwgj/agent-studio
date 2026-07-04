#!/usr/bin/env python
# -*- coding: UTF-8 -*-
# pylint: disable=protected-access  # 单测需直接调用内部函数 _log_ir_content
"""UT for the DEBUG-log guard optimization in the IR loading path.

Covers the committed change:
  - ``open_utils._log_ir_content``: guard via
    ``workflow_logger.logger().isEnabledFor(logging.DEBUG)`` + %-style lazy formatting.
  - ``ir_converter`` debug sites: guard via ``logger.isEnabledFor(logging.DEBUG)``
    on the stdlib common logger.

Why these tests look the way they do
------------------------------------
``workflow_logger`` is a ``LazyLogger`` wrapping openjiuwen's ``DefaultLogger``,
which is *not* a stdlib ``logging.Logger`` subclass and does not expose
``isEnabledFor`` directly. A guard that calls ``workflow_logger.isEnabledFor``
raises ``AttributeError`` on the real chain — and an earlier mock-based test hid
that by substituting a fake logger that *did* expose it. These tests therefore
exercise the **real** chain (no mocking of ``workflow_logger``) so the
regression is pinned: the guard must not raise, must skip ``json.dumps`` when
DEBUG is off, and must emit byte-identical output when DEBUG is on.
"""
import json
import logging
from unittest.mock import patch

import pytest

import jiuwen.serve.controllers.execution.open_utils as ou
from jiuwen.common.log.base import logger as common_logger

# Captured before any test patches json.dumps, so baseline computations always
# use the real encoder.
_REAL_DUMPS = json.dumps


class _RecordingHandler(logging.Handler):
    """Captures log records into a list for assertions."""

    def __init__(self):
        super().__init__(level=logging.DEBUG)
        self.records = []

    def emit(self, record):  # noqa: D401
        self.records.append(record)


@pytest.fixture
def workflow_stdlib_logger():
    """The real stdlib logger underlying the LazyLogger ``workflow_logger``."""
    return ou.workflow_logger.logger()


@pytest.fixture
def capture_workflow_debug(workflow_stdlib_logger):
    """Attach a DEBUG-level recording handler to the workflow stdlib logger."""
    handler = _RecordingHandler()
    workflow_stdlib_logger.addHandler(handler)
    yield handler
    workflow_stdlib_logger.removeHandler(handler)


@pytest.fixture
def restore_workflow_level(workflow_stdlib_logger):
    """Save/restore the workflow logger level around each test."""
    original = workflow_stdlib_logger.level
    yield
    workflow_stdlib_logger.setLevel(original)


def _baseline_message(source: str, path: str, ir_data) -> str:
    """Reproduce the pre-optimization f-string output for byte-identical compare."""
    ir_json = _REAL_DUMPS(ir_data, ensure_ascii=False, default=str)
    return f"IR content from {source}: path={path}, size={len(ir_json)} bytes, content={ir_json}"


class TestLogIrContentGuard:
    """_log_ir_content 守卫：DEBUG 关闭跳过 json.dumps，开启时输出与原实现一致，且真实链路上不抛异常。"""

    @staticmethod
    def test_guard_does_not_raise_on_real_chain():
        # Regression for the AttributeError bug: calling the guard on the real
        # LazyLogger -> DefaultLogger -> stdlib chain must succeed.
        stdlib_logger = ou.workflow_logger.logger()
        original = stdlib_logger.level
        try:
            stdlib_logger.setLevel(logging.INFO)
            ou._log_ir_content("obs", "p/ath", {"k": "v"})  # must not raise
        finally:
            stdlib_logger.setLevel(original)

    @pytest.mark.usefixtures("restore_workflow_level")
    def test_debug_disabled_skips_serialization(
        self, workflow_stdlib_logger, capture_workflow_debug
    ):
        workflow_stdlib_logger.setLevel(logging.INFO)
        with patch.object(ou.json, "dumps", wraps=_REAL_DUMPS) as spy:
            ou._log_ir_content("obs", "p/ath", {"k": "v", "nested": {"a": [1, 2]}})

        assert spy.call_count == 0, "DEBUG 关闭时不应触发 json.dumps（守卫未生效）"
        assert capture_workflow_debug.records == [], "DEBUG 关闭时不应发出 DEBUG 记录"

    @pytest.mark.usefixtures("restore_workflow_level")
    def test_debug_enabled_emits_identical_output(
        self, workflow_stdlib_logger, capture_workflow_debug
    ):
        workflow_stdlib_logger.setLevel(logging.DEBUG)
        ir_data = {"k": "v", "nested": {"a": [1, 2, 3]}, "中文": "ok"}

        with patch.object(ou.json, "dumps", wraps=_REAL_DUMPS):
            ou._log_ir_content("obs", "p/ath", ir_data)

        assert len(capture_workflow_debug.records) == 1
        rec = capture_workflow_debug.records[0]
        assert rec.levelno == logging.DEBUG
        assert rec.getMessage() == _baseline_message("obs", "p/ath", ir_data)

    @pytest.mark.usefixtures("restore_workflow_level")
    @pytest.mark.parametrize(
        "source,path,ir_data",
        [
            ("memory", "a/b", {"x": 1}),
            ("redis", "c/d", {"list": [1, {"y": "z"}]}),
            ("cache", "e/f", {"obj": object()}),  # exercises default=str
        ],
    )
    def test_debug_enabled_matches_baseline_across_inputs(
        self,
        workflow_stdlib_logger,
        capture_workflow_debug,
        source,
        path,
        ir_data,
    ):
        workflow_stdlib_logger.setLevel(logging.DEBUG)
        with patch.object(ou.json, "dumps", wraps=_REAL_DUMPS):
            ou._log_ir_content(source, path, ir_data)

        assert capture_workflow_debug.records[-1].getMessage() == _baseline_message(
            source, path, ir_data
        )

    @pytest.mark.usefixtures("restore_workflow_level")
    def test_serialization_error_falls_back_to_warning(
        self, workflow_stdlib_logger, capture_workflow_debug
    ):
        # Even with DEBUG on, a json.dumps failure must be caught and routed to
        # logger.warning; it must not propagate out of _log_ir_content.
        workflow_stdlib_logger.setLevel(logging.DEBUG)
        with patch.object(ou.json, "dumps", side_effect=TypeError("boom")), \
             patch.object(ou, "logger") as common_mock:
            ou._log_ir_content("obs", "p", {"x": 1})

        common_mock.warning.assert_called_once()
        assert capture_workflow_debug.records == [], "dumps 失败时不应再发出 DEBUG 记录"


class TestIrConverterGuardSafeOnRealLogger:
    """ir_converter 守卫所用 stdlib common logger 的 isEnabledFor 可用性与 DEBUG 关闭短路校验。"""

    @staticmethod
    def test_common_logger_supports_is_enabled_for():
        assert isinstance(common_logger, logging.Logger)
        assert hasattr(common_logger, "isEnabledFor")
        # must not raise on the real logger:
        _ = common_logger.isEnabledFor(logging.DEBUG)

    @staticmethod
    def test_debug_disabled_returns_false():
        original = common_logger.level
        try:
            common_logger.setLevel(logging.INFO)
            assert common_logger.isEnabledFor(logging.DEBUG) is False
            common_logger.setLevel(logging.DEBUG)
            assert common_logger.isEnabledFor(logging.DEBUG) is True
        finally:
            common_logger.setLevel(original)
