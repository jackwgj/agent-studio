#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""Server lifespan unit tests — LOCAL and SANDBOX SysOperation registration."""

import sys
from unittest.mock import MagicMock, patch, AsyncMock

# agent_builder.app triggers SSL context creation at import time (reads YAML
# config and tries to create an SSLContext with None certs). Pre-populate
# sys.modules so the real module is never imported during these tests.
#
# The real agent_builder.app exposes a Flask app, which server.instance_app()
# mounts via WSGIMiddleware (not app.include_router). The stub must preserve
# that contract: if `app` is left as a MagicMock, instance_app() falls into the
# app.include_router() branch and FastAPI's `assert not router._contains_router`
# guard trips because `mock._contains_router(...)` returns a truthy MagicMock
# ("Cannot include an APIRouter instance that already includes this router").
if "agent_builder.app" not in sys.modules:
    from flask import Flask as _Flask
    _stub_mod = MagicMock()
    _stub_mod.app = _Flask(__name__)
    sys.modules["agent_builder.app"] = _stub_mod

import pytest
from openjiuwen.core.sys_operation import OperationMode


class TestServerLifespanLocalSysOp:
    @staticmethod
    def test_registers_local_sys_op_when_not_exists():
        from agent_runtime.serve.server import lifespan
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()
        mock_resource_mgr.get_sys_operation.return_value = None
        mock_add_result = MagicMock()
        mock_add_result.is_ok.return_value = True
        mock_resource_mgr.add_sys_operation.return_value = mock_add_result

        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = ""
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"):

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            asyncio.run(run_lifespan())

            mock_resource_mgr.get_sys_operation.assert_any_call("flow_code_sys_op")
            mock_resource_mgr.add_sys_operation.assert_called_once()
            card = mock_resource_mgr.add_sys_operation.call_args[0][0]
            assert card.id == "flow_code_sys_op"
            assert card.mode == OperationMode.LOCAL

    @staticmethod
    def test_skips_local_sys_op_when_already_registered():
        from agent_runtime.serve.server import lifespan
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()
        mock_resource_mgr.get_sys_operation.return_value = MagicMock()
        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = ""
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"):

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            asyncio.run(run_lifespan())

            mock_resource_mgr.add_sys_operation.assert_not_called()


class TestServerLifespanSandboxSysOp:
    @staticmethod
    def test_no_sandbox_when_server_empty():
        from agent_runtime.serve.server import lifespan
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()

        def get_sys_op_side_effect(op_id):
            if op_id == "flow_code_sys_op":
                return MagicMock()
            return None

        mock_resource_mgr.get_sys_operation.side_effect = get_sys_op_side_effect
        mock_add_result = MagicMock()
        mock_add_result.is_ok.return_value = True
        mock_resource_mgr.add_sys_operation.return_value = mock_add_result

        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = ""
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"):

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            asyncio.run(run_lifespan())

            sandbox_calls = [
                call for call in mock_resource_mgr.add_sys_operation.call_args_list
                if "sandbox" in str(call)
            ]
            assert len(sandbox_calls) == 0

    @staticmethod
    def test_registers_sandbox_when_server_configured():
        from agent_runtime.serve.server import lifespan
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()

        def get_sys_op_side_effect(op_id):
            return None

        mock_resource_mgr.get_sys_operation.side_effect = get_sys_op_side_effect
        mock_add_result = MagicMock()
        mock_add_result.is_ok.return_value = True
        mock_resource_mgr.add_sys_operation.return_value = mock_add_result

        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = "http://172.23.10.84:9090"
        mock_settings.security_sandbox.sandbox_type = "aio"
        mock_settings.security_sandbox.scope = "system"
        mock_settings.security_sandbox.idle_ttl_seconds = 600
        mock_settings.security_sandbox.timeout_seconds = 300
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"):

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            asyncio.run(run_lifespan())

            assert mock_resource_mgr.add_sys_operation.call_count == 2
            sandbox_card = mock_resource_mgr.add_sys_operation.call_args_list[1][0][0]
            assert sandbox_card.id == "flow_code_sandbox_sys_op"
            assert sandbox_card.mode == OperationMode.SANDBOX

    @staticmethod
    def test_sandbox_scope_session():
        from agent_runtime.serve.server import lifespan
        from openjiuwen.core.sys_operation.config import ContainerScope
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()
        mock_resource_mgr.get_sys_operation.return_value = None
        mock_add_result = MagicMock()
        mock_add_result.is_ok.return_value = True
        mock_resource_mgr.add_sys_operation.return_value = mock_add_result

        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = "http://172.23.10.84:9090"
        mock_settings.security_sandbox.sandbox_type = "aio"
        mock_settings.security_sandbox.scope = "session"
        mock_settings.security_sandbox.idle_ttl_seconds = 600
        mock_settings.security_sandbox.timeout_seconds = 300
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"):

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            asyncio.run(run_lifespan())

            sandbox_card = mock_resource_mgr.add_sys_operation.call_args_list[1][0][0]
            assert sandbox_card.gateway_config.isolation.container_scope == ContainerScope.SESSION

    @staticmethod
    def test_sandbox_registration_failure_logs_error():
        from agent_runtime.serve.server import lifespan
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()
        mock_resource_mgr.get_sys_operation.return_value = None

        def add_sys_op_side_effect(card):
            if card.mode == OperationMode.SANDBOX:
                result = MagicMock()
                result.is_ok.return_value = False
                return result
            result = MagicMock()
            result.is_ok.return_value = True
            return result

        mock_resource_mgr.add_sys_operation.side_effect = add_sys_op_side_effect

        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = "http://172.23.10.84:9090"
        mock_settings.security_sandbox.sandbox_type = "aio"
        mock_settings.security_sandbox.scope = "system"
        mock_settings.security_sandbox.idle_ttl_seconds = 600
        mock_settings.security_sandbox.timeout_seconds = 300
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"), \
             patch("agent_runtime.serve.server.logger") as mock_logger:

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            asyncio.run(run_lifespan())

            error_calls = [
                call for call in mock_logger.error.call_args_list
                if "sandbox" in str(call).lower()
            ]
            assert len(error_calls) > 0


class TestServerLifespanS3Init:
    @staticmethod
    def test_initializes_s3_storage_provider():
        """lifespan calls S3StorageProvider.initialize() on startup"""
        from agent_runtime.serve.server import lifespan
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()
        mock_resource_mgr.get_sys_operation.return_value = MagicMock()
        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = ""
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        mock_s3_provider = MagicMock()
        mock_s3_provider.initialize = AsyncMock()
        mock_s3_provider.close = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"), \
             patch("agent_runtime.serve.server.S3StorageProvider") as mock_s3_cls:

            mock_s3_cls.instance.return_value = mock_s3_provider

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            asyncio.run(run_lifespan())

            mock_s3_cls.instance.assert_called()
            mock_s3_provider.initialize.assert_awaited_once()
            mock_s3_provider.close.assert_awaited_once()

    @staticmethod
    def test_s3_init_failure_does_not_crash_lifespan():
        """If S3 initialize() fails, lifespan still completes"""
        from agent_runtime.serve.server import lifespan
        from fastapi import FastAPI

        mock_app = MagicMock(spec=FastAPI)

        mock_resource_mgr = MagicMock()
        mock_resource_mgr.get_sys_operation.return_value = MagicMock()
        mock_settings = MagicMock()
        mock_settings.security_sandbox.server = ""
        mock_settings.workflow_log.level = "INFO"

        mock_redis_mgr = MagicMock()
        mock_redis_mgr.close = AsyncMock()
        mock_redis_mgr.is_initialized = True
        mock_redis_client = AsyncMock()
        mock_redis_client.ping = AsyncMock(return_value=True)
        mock_redis_mgr.get_client.return_value = mock_redis_client

        mock_checkpointer = AsyncMock()

        mock_s3_provider = MagicMock()
        mock_s3_provider.initialize = AsyncMock(side_effect=Exception("S3 unavailable"))
        mock_s3_provider.close = AsyncMock()

        with patch("agent_runtime.serve.server.settings", mock_settings), \
             patch("agent_runtime.serve.server.Runner.resource_mgr", mock_resource_mgr), \
             patch("agent_runtime.serve.server.RedisClientManager.get_instance", return_value=mock_redis_mgr), \
             patch("agent_runtime.serve.server.configure_log_config"), \
             patch("agent_runtime.serve.server.CheckpointerFactory.create", return_value=mock_checkpointer), \
             patch("agent_runtime.serve.server.CheckpointerFactory.set_default_checkpointer"), \
             patch("agent_runtime.serve.server.build_redis_checkpointer_config"), \
             patch("agent_runtime.serve.server.S3StorageProvider") as mock_s3_cls:

            mock_s3_cls.instance.return_value = mock_s3_provider

            import asyncio

            async def run_lifespan():
                async with lifespan(mock_app):
                    pass

            # Should not raise
            asyncio.run(run_lifespan())

            mock_s3_provider.initialize.assert_awaited_once()
            # close() should still be called in finally block
            mock_s3_provider.close.assert_awaited_once()
