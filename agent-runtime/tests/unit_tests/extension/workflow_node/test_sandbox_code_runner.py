#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""SandboxCodeRunner unit tests."""

from unittest.mock import AsyncMock, patch, MagicMock

import pytest
from agent_runtime.extension.workflow_node.code_runner.sandbox_code_runner import (
    SandboxCodeRunner,
)


class TestSandboxCodeRunner:
    """Tests for SandboxCodeRunner."""

    @pytest.fixture
    def mock_httpx_client(self):
        """Mock httpx AsyncClient."""
        with patch("httpx.AsyncClient") as mock_client:
            yield mock_client

    def test_init_with_server_and_ssl_verify(self):
        """Test initialization with server URL and SSL verify setting."""
        runner = SandboxCodeRunner(
            sandbox_server="https://sandbox.example.com/api",
            ssl_verify=True,
        )
        assert runner.sandbox_server == "https://sandbox.example.com/api"
        assert runner.ssl_verify is True

    def test_init_with_default_ssl_verify(self):
        """Test initialization with default SSL verify (False)."""
        runner = SandboxCodeRunner(sandbox_server="https://sandbox.example.com/api")
        assert runner.ssl_verify is False

    @pytest.mark.asyncio
    async def test_run_success(self, mock_httpx_client):
        """Test successful code execution."""
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "output": {"return": {"result": 42}, "error": None}
        }
        mock_response.raise_for_status = MagicMock()

        mock_client_instance = AsyncMock()
        mock_client_instance.post = AsyncMock(return_value=mock_response)
        mock_httpx_client.return_value.__aenter__ = AsyncMock(
            return_value=mock_client_instance
        )
        mock_httpx_client.return_value.__aexit__ = AsyncMock()

        runner = SandboxCodeRunner(sandbox_server="https://sandbox.example.com/api")

        result = await runner.run(
            user_code="def main(args):\n    return {'result': 42}",
            inputs={"x": 1},
            timeout=300,
        )

        assert result == {"result": 42}
        mock_client_instance.post.assert_called_once()

    @pytest.mark.asyncio
    async def test_run_with_error(self, mock_httpx_client):
        """Test execution with sandbox returning an error."""
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "output": {"return": None, "error": "Syntax error in code"}
        }

        mock_client_instance = AsyncMock()
        mock_client_instance.post = AsyncMock(return_value=mock_response)
        mock_httpx_client.return_value.__aenter__ = AsyncMock(
            return_value=mock_client_instance
        )
        mock_httpx_client.return_value.__aexit__ = AsyncMock()

        runner = SandboxCodeRunner(sandbox_server="https://sandbox.example.com/api")

        with pytest.raises(
            RuntimeError, match="Sandbox run error: Syntax error in code"
        ):
            await runner.run(
                user_code="invalid code",
                inputs={},
                timeout=300,
            )
