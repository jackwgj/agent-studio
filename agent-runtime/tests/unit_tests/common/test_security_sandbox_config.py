#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""SecuritySandboxSettings unit tests."""

import os
import subprocess
import sys
from pathlib import Path
from unittest.mock import patch

import pytest
from pydantic import ValidationError
from agent_runtime.common.config import SecuritySandboxSettings, Settings


REPOSITORY_ROOT = Path(__file__).resolve().parents[4]
MONOREPO_PYTHONPATH = os.pathsep.join(
    str(REPOSITORY_ROOT / path)
    for path in (
        "agent-runtime",
        "packages/common_utils",
        "packages/storage",
        "packages/model_service",
    )
)


def import_runtime_settings(workspace_root: str) -> subprocess.CompletedProcess[str]:
    env = os.environ.copy()
    env["PYTHONPATH"] = MONOREPO_PYTHONPATH
    env["CONVERSATION_SANDBOX_WORKSPACE_ROOT"] = workspace_root
    return subprocess.run(
        [
            sys.executable,
            "-c",
            (
                "from agent_runtime.common.config import settings; "
                "print(f'workspace_root={settings.security_sandbox.workspace_root}')"
            ),
        ],
        cwd=REPOSITORY_ROOT,
        env=env,
        capture_output=True,
        text=True,
        check=False,
    )


class TestSecuritySandboxSettingsDefaults:
    @staticmethod
    def test_default_values():
        env_vars_to_clear = [
            "CONVERSATION_SANDBOX_WORKSPACE_ROOT",
            "SECURITY_SANDBOX_SERVER",
            "SECURITY_SANDBOX_SSL_VERIFY",
            "SECURITY_SANDBOX_IDLE_TTL",
            "SECURITY_SANDBOX_TIMEOUT",
            "SECURITY_SANDBOX_SCOPE",
        ]
        saved = {k: os.environ.pop(k, None) for k in env_vars_to_clear}
        try:
            s = SecuritySandboxSettings(_env_file=None)
            assert s.server == ""
            assert s.ssl_verify is False
            assert s.idle_ttl_seconds == 600
            assert s.timeout_seconds == 300
            assert s.scope == "system"
            assert s.workspace_root == "/workspace"
        finally:
            for k, v in saved.items():
                if v is not None:
                    os.environ[k] = v

    @staticmethod
    def test_env_override_server():
        with patch.dict(os.environ, {"SECURITY_SANDBOX_SERVER": "http://xxx:9090"}):
            s = SecuritySandboxSettings()
            assert s.server == "http://xxx:9090"

    @staticmethod
    def test_env_override_ssl_verify():
        with patch.dict(os.environ, {"SECURITY_SANDBOX_SSL_VERIFY": "true"}):
            s = SecuritySandboxSettings()
            assert s.ssl_verify is True

    @staticmethod
    def test_env_override_idle_ttl():
        with patch.dict(os.environ, {"SECURITY_SANDBOX_IDLE_TTL": "1200"}):
            s = SecuritySandboxSettings()
            assert s.idle_ttl_seconds == 1200

    @staticmethod
    def test_env_override_timeout():
        with patch.dict(os.environ, {"SECURITY_SANDBOX_TIMEOUT": "600"}):
            s = SecuritySandboxSettings()
            assert s.timeout_seconds == 600

    @staticmethod
    def test_invalid_timeout_raises():
        with patch.dict(os.environ, {"SECURITY_SANDBOX_TIMEOUT": "abc"}):
            with pytest.raises(ValidationError):
                SecuritySandboxSettings()

    @staticmethod
    def test_scope_session():
        with patch.dict(os.environ, {"SECURITY_SANDBOX_SCOPE": "session"}):
            s = SecuritySandboxSettings()
            assert s.scope == "session"

    @staticmethod
    def test_scope_system():
        with patch.dict(os.environ, {"SECURITY_SANDBOX_SCOPE": "system"}):
            s = SecuritySandboxSettings()
            assert s.scope == "system"

    @staticmethod
    def test_workspace_root_normalizes_trailing_slash():
        with patch.dict(os.environ, {"CONVERSATION_SANDBOX_WORKSPACE_ROOT": "/sandbox/conversations/"}):
            s = SecuritySandboxSettings()
            assert s.workspace_root == "/sandbox/conversations"

    @staticmethod
    @pytest.mark.parametrize(
        "workspace_root",
        [
            "",
            "   ",
            "workspace",
            "../workspace",
            "/workspace/../other",
            "C:/workspace",
            r"C:\\workspace",
            r"\\workspace",
        ],
    )
    def test_workspace_root_rejects_non_posix_absolute_paths(workspace_root):
        with patch.dict(os.environ, {"CONVERSATION_SANDBOX_WORKSPACE_ROOT": workspace_root}):
            with pytest.raises(ValidationError, match="CONVERSATION_SANDBOX_WORKSPACE_ROOT"):
                SecuritySandboxSettings()


class TestSettingsIntegration:
    @staticmethod
    def test_settings_has_security_sandbox():
        s = Settings()
        assert isinstance(s.security_sandbox, SecuritySandboxSettings)

    @staticmethod
    def test_settings_security_sandbox_defaults():
        env_vars_to_clear = [
            "CONVERSATION_SANDBOX_WORKSPACE_ROOT",
            "SECURITY_SANDBOX_SERVER",
            "SECURITY_SANDBOX_SSL_VERIFY",
            "SECURITY_SANDBOX_IDLE_TTL",
            "SECURITY_SANDBOX_TIMEOUT",
            "SECURITY_SANDBOX_SCOPE",
        ]
        saved = {k: os.environ.pop(k, None) for k in env_vars_to_clear}
        try:
            s = SecuritySandboxSettings(_env_file=None)
            assert s.server == ""
            assert s.ssl_verify is False
            assert s.idle_ttl_seconds == 600
            assert s.timeout_seconds == 300
            assert s.scope == "system"
            assert s.workspace_root == "/workspace"
        finally:
            for k, v in saved.items():
                if v is not None:
                    os.environ[k] = v

    @staticmethod
    def test_import_time_settings_uses_workspace_root_from_environment():
        result = import_runtime_settings("/sandbox/conversations/")

        assert result.returncode == 0, result.stderr
        assert "workspace_root=/sandbox/conversations" in result.stdout

    @staticmethod
    @pytest.mark.parametrize(
        "workspace_root",
        [
            "",
            "   ",
            "relative/workspace",
            "../workspace",
            "/workspace/../other",
            "C:/workspace",
            r"C:\\workspace",
            r"\\workspace",
        ],
    )
    def test_import_time_settings_rejects_invalid_workspace_root(workspace_root):
        result = import_runtime_settings(workspace_root)

        assert result.returncode != 0
        assert "CONVERSATION_SANDBOX_WORKSPACE_ROOT" in result.stderr
