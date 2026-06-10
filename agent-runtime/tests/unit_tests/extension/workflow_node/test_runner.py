#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""Runner pattern unit tests — LocalRunner, SandboxRunner, CodeRunnerFactory."""

from unittest.mock import patch, MagicMock

import pytest
from agent_runtime.extension.workflow_node.code_runner.runner import (
    LocalRunner,
    SandboxRunner,
    CodeRunnerFactory,
)
from agent_runtime.extension.workflow_node.code_runner.local_code_runner import (
    LocalCodeRunner,
)
from agent_runtime.extension.workflow_node.code_runner.sandbox_code_runner import (
    SandboxCodeRunner,
)


class TestLocalRunner:
    @staticmethod
    def test_name_returns_local():
        assert LocalRunner().name() == "local"

    @staticmethod
    def test_create_returns_local_code_runner():
        runner = LocalRunner()
        mock_code_op = MagicMock()
        created = runner.create(mock_code_op)
        assert isinstance(created, LocalCodeRunner)


class TestSandboxRunner:
    @staticmethod
    def test_name_returns_sandbox():
        assert SandboxRunner().name() == "sandbox"

    @staticmethod
    def test_create_returns_sandbox_code_runner_when_registered():
        runner = SandboxRunner()
        mock_sys_op = MagicMock()

        with patch(
            "openjiuwen.core.runner.Runner.resource_mgr"
        ) as mock_resource_mgr:
            mock_resource_mgr.get_sys_operation.return_value = mock_sys_op
            created = runner.create(MagicMock())
            assert isinstance(created, SandboxCodeRunner)

    @staticmethod
    def test_create_raises_when_not_registered():
        runner = SandboxRunner()

        with patch(
            "openjiuwen.core.runner.Runner.resource_mgr"
        ) as mock_resource_mgr:
            mock_resource_mgr.get_sys_operation.return_value = None
            with pytest.raises(RuntimeError, match="not registered"):
                runner.create(MagicMock())

    @staticmethod
    def test_create_uses_sandbox_sys_op_id():
        runner = SandboxRunner()
        mock_sys_op = MagicMock()

        with patch(
            "openjiuwen.core.runner.Runner.resource_mgr"
        ) as mock_resource_mgr:
            mock_resource_mgr.get_sys_operation.return_value = mock_sys_op
            runner.create(MagicMock())
            mock_resource_mgr.get_sys_operation.assert_called_once_with(
                "flow_code_sandbox_sys_op"
            )


class TestCodeRunnerFactory:
    @staticmethod
    def test_get_runner_local():
        runner = CodeRunnerFactory.get_runner("local")
        assert isinstance(runner, LocalRunner)

    @staticmethod
    def test_get_runner_sandbox():
        runner = CodeRunnerFactory.get_runner("sandbox")
        assert isinstance(runner, SandboxRunner)

    @staticmethod
    def test_get_runner_unknown_falls_back_to_local():
        runner = CodeRunnerFactory.get_runner("docker")
        assert isinstance(runner, LocalRunner)

    @staticmethod
    def test_init_default_runners():
        setattr(CodeRunnerFactory, "_runners", {})
        CodeRunnerFactory.init_default_runners()
        assert CodeRunnerFactory.get_runner("local") is not None
        assert CodeRunnerFactory.get_runner("sandbox") is not None
        assert isinstance(CodeRunnerFactory.get_runner("local"), LocalRunner)
        assert isinstance(CodeRunnerFactory.get_runner("sandbox"), SandboxRunner)

    @staticmethod
    def test_register_custom_runner():
        class CustomRunner(LocalRunner):
            def name(self):
                return "custom"

        custom = CustomRunner()
        CodeRunnerFactory.register(custom)
        assert CodeRunnerFactory.get_runner("custom") is custom

    @staticmethod
    def test_get_runner_returns_none_for_empty_runners():
        setattr(CodeRunnerFactory, "_runners", {})
        result = CodeRunnerFactory.get_runner("local")
        assert result is None
        CodeRunnerFactory.init_default_runners()
