#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""Runner pattern unit tests."""

from unittest.mock import patch, MagicMock

import pytest
from agent_runtime.extension.workflow_node.code_runner.runner import (
    LocalRunner,
    SandboxRunner,
    CodeRunnerFactory,
)


class TestLocalRunner:
    """Tests for LocalRunner."""

    def test_name_returns_local(self):
        """Test name returns 'local'."""
        runner = LocalRunner()
        assert runner.name() == "local"

    def test_create_returns_local_code_runner(self):
        """Test create returns LocalCodeRunner instance."""
        runner = LocalRunner()
        mock_code_op = MagicMock()

        created_runner = runner.create(mock_code_op)

        from agent_runtime.extension.workflow_node.code_runner.local_code_runner import (
            LocalCodeRunner,
        )

        assert isinstance(created_runner, LocalCodeRunner)


class TestSandboxRunner:
    """Tests for SandboxRunner."""

    def test_name_returns_sandbox(self):
        """Test name returns 'sandbox'."""
        runner = SandboxRunner()
        assert runner.name() == "sandbox"

    def test_create_raises_when_server_not_configured(self):
        """Test create raises RuntimeError when SECURITY_SANDBOX_SERVER is empty."""
        runner = SandboxRunner()
        mock_code_op = MagicMock()

        with patch(
            "agent_runtime.extension.workflow_node.code_runner.runner.settings"
        ) as mock_settings:
            mock_settings.security_sandbox.server = ""
            with pytest.raises(
                RuntimeError, match="SECURITY_SANDBOX_SERVER not configured"
            ):
                runner.create(mock_code_op)

    def test_create_returns_sandbox_code_runner_when_configured(self):
        """Test create returns SandboxCodeRunner when server is configured."""
        runner = SandboxRunner()
        mock_code_op = MagicMock()

        with patch(
            "agent_runtime.extension.workflow_node.code_runner.runner.settings"
        ) as mock_settings:
            mock_settings.security_sandbox.server = "https://sandbox.example.com/api"
            mock_settings.security_sandbox.ssl_verify = False

            created_runner = runner.create(mock_code_op)

            from agent_runtime.extension.workflow_node.code_runner.sandbox_code_runner import (
                SandboxCodeRunner,
            )

            assert isinstance(created_runner, SandboxCodeRunner)


class TestCodeRunnerFactory:
    """Tests for CodeRunnerFactory."""

    def test_get_runner_returns_local_for_unknown_env(self):
        """Test get_runner returns local runner for unknown environment."""
        runner = CodeRunnerFactory.get_runner("unknown")
        assert isinstance(runner, LocalRunner)

    def test_get_runner_returns_local_for_local_env(self):
        """Test get_runner returns local runner for 'local' environment."""
        runner = CodeRunnerFactory.get_runner("local")
        assert isinstance(runner, LocalRunner)

    def test_get_runner_returns_sandbox_for_sandbox_env(self):
        """Test get_runner returns sandbox runner for 'sandbox' environment."""
        runner = CodeRunnerFactory.get_runner("sandbox")
        assert isinstance(runner, SandboxRunner)

    def test_init_default_runners_registers_all_runners(self):
        """Test init_default_runners registers all built-in runners."""
        # Clear existing runners
        CodeRunnerFactory._runners = {}

        CodeRunnerFactory.init_default_runners()

        assert "local" in CodeRunnerFactory._runners
        assert "sandbox" in CodeRunnerFactory._runners
