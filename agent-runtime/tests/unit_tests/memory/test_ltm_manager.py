# pylint: disable=protected-access,no-self-use,redefined-outer-name
"""Unit tests for LTM manager module."""

from unittest.mock import AsyncMock, MagicMock, patch

import pytest

from agent_runtime.memory.adapter.ltm_manager import (
    init_ltm,
    get_ltm,
    build_scope_config,
)
from agent_runtime.common.config import MemorySettings, OpenSearchSettings


class TestInitLtm:
    @pytest.mark.asyncio
    async def test_init_ltm_disabled(self, mock_redis):
        with patch("agent_runtime.memory.adapter.ltm_manager.settings") as mock_settings:
            mock_settings.memory = MemorySettings(enabled=False)
            result = await init_ltm(mock_redis)
        assert result is False

    @pytest.mark.asyncio
    async def test_init_ltm_opensearch_unreachable(self, mock_redis):
        with patch("agent_runtime.memory.adapter.ltm_manager.settings") as mock_settings:
            mock_settings.memory = MemorySettings(
                enabled=True,
                embedding_base_url="http://localhost:11434",
            )
            # pydantic-settings ignores the ``enabled=True`` init kwarg because
            # the field uses ``validation_alias="MEMORY_ENABLED"``; set it
            # explicitly so the test's intent holds regardless of env.
            mock_settings.memory.enabled = True
            mock_settings.opensearch = OpenSearchSettings(hosts="localhost:9200")

            with patch(
                "agent_runtime.memory.adapter.ltm_manager.OpenSearchVectorStore"
            ) as mock_vs_cls:
                mock_vs = AsyncMock()
                mock_vs.ping = AsyncMock(return_value=False)
                mock_vs_cls.return_value = mock_vs

                result = await init_ltm(mock_redis)

        assert result is False

    @pytest.mark.asyncio
    async def test_init_ltm_success(self, mock_redis):
        with patch("agent_runtime.memory.adapter.ltm_manager.settings") as mock_settings:
            mock_settings.memory = MemorySettings(
                enabled=True,
                embedding_base_url="http://localhost:11434",
                embedding_model="bge-m3",
                llm_model="test-model",
                llm_base_url="http://localhost:11434/v1",
                llm_api_key="sk-test",
            )
            # pydantic-settings ignores the ``enabled=True`` init kwarg because
            # the field uses ``validation_alias="MEMORY_ENABLED"``; set it
            # explicitly so the test's intent holds regardless of env.
            mock_settings.memory.enabled = True
            mock_settings.opensearch = OpenSearchSettings(hosts="localhost:9200")

            with patch(
                "agent_runtime.memory.adapter.ltm_manager.OpenSearchVectorStore"
            ) as mock_vs_cls:
                mock_vs = AsyncMock()
                mock_vs.ping = AsyncMock(return_value=True)
                mock_vs_cls.return_value = mock_vs

                with patch(
                    "agent_runtime.memory.adapter.ltm_manager.APIEmbedding"
                ) as mock_emb_cls:
                    mock_emb_cls.return_value = MagicMock()
                    with patch(
                        "agent_runtime.memory.adapter.ltm_manager.LongTermMemory"
                    ) as mock_ltm_cls:
                        mock_ltm = MagicMock()
                        mock_ltm.register_store = AsyncMock()
                        mock_ltm.set_config = MagicMock()
                        mock_ltm_cls.return_value = mock_ltm

                        result = await init_ltm(mock_redis)

        assert result is True
        assert get_ltm() is mock_ltm

    @pytest.mark.asyncio
    async def test_init_ltm_no_embedding_url(self, mock_redis):
        with patch("agent_runtime.memory.adapter.ltm_manager.settings") as mock_settings:
            mock_settings.memory = MemorySettings(enabled=True)
            # pydantic-settings ignores the ``enabled=True`` init kwarg because
            # the field uses ``validation_alias="MEMORY_ENABLED"``; set it
            # explicitly so the test's intent holds regardless of env.
            mock_settings.memory.enabled = True
            mock_settings.opensearch = OpenSearchSettings(hosts="localhost:9200")

            with patch(
                "agent_runtime.memory.adapter.ltm_manager.OpenSearchVectorStore"
            ) as mock_vs_cls:
                mock_vs = AsyncMock()
                mock_vs.ping = AsyncMock(return_value=True)
                mock_vs_cls.return_value = mock_vs

                with patch(
                    "agent_runtime.memory.adapter.ltm_manager.APIEmbedding"
                ) as mock_emb_cls:
                    mock_emb_cls.return_value = MagicMock()
                    with patch(
                        "agent_runtime.memory.adapter.ltm_manager.LongTermMemory"
                    ) as mock_ltm_cls:
                        mock_ltm = MagicMock()
                        mock_ltm.register_store = AsyncMock()
                        mock_ltm.set_config = MagicMock()
                        mock_ltm_cls.return_value = mock_ltm

                        result = await init_ltm(mock_redis)

        assert result is True
        call_kwargs = mock_ltm.register_store.call_args.kwargs
        assert call_kwargs["embedding_model"] is None


class TestGetLtm:
    @staticmethod
    def test_get_ltm_none():
        # Access get_ltm through the module object so the function's
        # __globals__ is guaranteed to be the same dict we reset — the
        # top-imported `get_ltm` binding can drift to a different module
        # instance under CI's import layout, leaking a stale mock.
        import agent_runtime.memory.adapter.ltm_manager as mgr
        mgr._ltm_instance = None
        assert mgr.get_ltm() is None

    @staticmethod
    def test_get_ltm_after_init():
        import agent_runtime.memory.adapter.ltm_manager as mgr
        mock = MagicMock()
        mgr._ltm_instance = mock
        assert mgr.get_ltm() is mock


class TestBuildScopeConfig:
    @staticmethod
    def test_build_scope_config_full():
        with patch.dict("os.environ", {
            "MEMORY_LLM_MODEL": "test-model",
            "MEMORY_LLM_BASE_URL": "http://localhost:11434/v1",
            "MEMORY_LLM_API_KEY": "sk-test",
            "MEMORY_EMBEDDING_MODEL": "bge-m3",
            "MEMORY_EMBEDDING_BASE_URL": "http://localhost:11434",
            "MEMORY_EMBEDDING_API_KEY": "sk-emb",
        }):
            from agent_runtime.common.config import Settings, MemorySettings as _MS
            s = Settings()
            s.memory = _MS()
            with patch("agent_runtime.memory.adapter.ltm_manager.settings", s):
                config = build_scope_config()
        assert config.model_cfg is not None
        assert config.model_cfg.model_name == "test-model"
        assert config.model_client_cfg is not None
        assert config.model_client_cfg.api_base == "http://localhost:11434/v1"
        assert config.embedding_cfg is not None
        assert config.embedding_cfg.model_name == "bge-m3"

    @staticmethod
    def test_build_scope_config_minimal():
        with patch("agent_runtime.memory.adapter.ltm_manager.settings") as mock_settings:
            mock_settings.memory = MemorySettings()
            config = build_scope_config()
