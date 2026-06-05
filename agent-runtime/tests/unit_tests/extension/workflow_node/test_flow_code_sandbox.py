#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""FlowCode sandbox execution tests."""

from unittest.mock import patch, MagicMock

from agent_runtime.extension.workflow_node.flow_code import FlowCode, FlowCodeConfig


class TestFlowCodeSandboxFallback:
    """Tests for FlowCode sandbox fallback behavior."""

    def test_ensure_code_runner_fallbacks_to_local_when_sandbox_not_configured(self):
        """Test that FlowCode falls back to local when sandbox server is not configured."""
        config = FlowCodeConfig(
            code="def main(args):\n    return {}", exec_env="sandbox"
        )
        flow_code = FlowCode(config)

        with patch("agent_runtime.common.config.settings") as mock_settings:
            mock_settings.security_sandbox.server = ""

            with patch(
                "openjiuwen.core.runner.Runner.resource_mgr"
            ) as mock_resource_mgr:
                mock_sys_op = MagicMock()
                mock_resource_mgr.get_sys_operation.return_value = mock_sys_op

                mock_code_op = MagicMock()
                mock_sys_op.code.return_value = mock_code_op

                # This should not raise, just fall back to local
                flow_code._ensure_code_runner()

                # Verify a runner was created
                assert flow_code._code_runner is not None

    def test_ensure_code_runner_uses_local_for_local_exec_env(self):
        """Test that FlowCode uses local runner for local exec_env."""
        config = FlowCodeConfig(code="def main(args):\n    return {}", exec_env="local")
        flow_code = FlowCode(config)

        with patch("openjiuwen.core.runner.Runner.resource_mgr") as mock_resource_mgr:
            mock_sys_op = MagicMock()
            mock_resource_mgr.get_sys_operation.return_value = mock_sys_op

            mock_code_op = MagicMock()
            mock_sys_op.code.return_value = mock_code_op

            flow_code._ensure_code_runner()

            from agent_runtime.extension.workflow_node.code_runner.local_code_runner import (
                LocalCodeRunner,
            )

            assert isinstance(flow_code._code_runner, LocalCodeRunner)
