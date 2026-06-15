# pylint: disable=protected-access,no-self-use,invalid-name,unsubscriptable-object
"""Unit tests for AgentBuilderMemoryClient LTM operations."""

import builtins
import importlib
import sys
import types
from pathlib import Path
from unittest.mock import AsyncMock, MagicMock, patch

import pytest

from openjiuwen.core.memory.config.config import MemoryScopeConfig

# ProcessMemoryMode may not exist in agent-core; stub it
try:
    from openjiuwen.core.memory.config.config import ProcessMemoryMode
except ImportError:
    ProcessMemoryMode = MagicMock()
    ProcessMemoryMode.BATCH_MODE = "batch"

# Inject BaseChatModel stub into builtins so the module's class annotations resolve
if not hasattr(builtins, "BaseChatModel"):
    builtins.BaseChatModel = type("BaseChatModel", (), {})

# Pre-mock heavy jiuwen modules that have deep import chains
_jiuwen_mocks = [
    "jiuwen.context",
    "jiuwen.context.memory_engine",
    "jiuwen.context.memory_engine.client",
    "jiuwen.context.memory_engine.client.local_memory_client",
    "jiuwen.context.memory_engine.common",
    "jiuwen.context.memory_engine.common.base",
    "jiuwen.context.memory_engine.config",
    "jiuwen.context.memory_engine.config.config",
    "jiuwen.context.memory_engine.engine",
    "jiuwen.context.memory_engine.engine.memory_engine",
    "jiuwen.context.memory_engine.store",
    "jiuwen.common.llm_service",
    "jiuwen.common.llm_service.messages",
    "jiuwen.common.llm_service.language_model",
    "jiuwen.common.llm_service.language_model.base",
    "jiuwen.common.llm_service.model_util",
    "jiuwen.plugin",
    "jiuwen.plugin.models",
    "jiuwen.plugin.models.function",
    "jiuwen.plugin.decorator",
    "jiuwen.plugin.decorator.plugin",
    "jiuwen.orchestration",
    "jiuwen.orchestration.utils",
    "jiuwen.orchestration.callbacks",
    "jiuwen.orchestration.callbacks.base",
    "jiuwen.orchestration.flow",
    "jiuwen.orchestration.flow.bpmn_workflow",
    "jiwen.context",
    "jiwen.context.memory_engine",
    "jiwen.context.memory_engine.client",
    "jiwen.context.memory_engine.client.local_memory_client",
    "jiwen.context.memory_engine.common",
    "jiwen.context.memory_engine.common.base",
    "jiwen.context.memory_engine.config",
    "jiwen.context.memory_engine.config.config",
    "jiwen.context.memory_engine.engine",
    "jiwen.context.memory_engine.engine.memory_engine",
    "jiwen.context.memory_engine.store",
    "memory",
    "memory.kv_store_redis_impl",
]
for _mod in _jiuwen_mocks:
    if _mod not in sys.modules:
        sys.modules[_mod] = MagicMock()

# Create a real base class for MemoryClient so inheritance works
_mc_mod = types.ModuleType("jiuwen.context.memory_engine.client.memory_client")


class _MemoryClientStub:
    pass
_mc_mod.MemoryClient = _MemoryClientStub
sys.modules["jiuwen.context.memory_engine.client.memory_client"] = _mc_mod

# Also create stub for ProcessedMemResult (return type)
_base_mod = sys.modules.get("jiuwen.context.memory_engine.common.base")
if _base_mod is None or isinstance(_base_mod, MagicMock):
    _base_mod = types.ModuleType("jiuwen.context.memory_engine.common.base")
    _base_mod.ProcessedMemResult = type("ProcessedMemResult", (), {})
    sys.modules["jiuwen.context.memory_engine.common.base"] = _base_mod

# Patch ProcessMemoryMode if not available in agent-core
from openjiuwen.core.memory.config import config as _os_config
if not hasattr(_os_config, "ProcessMemoryMode"):
    _os_config.ProcessMemoryMode = MagicMock()
    _os_config.ProcessMemoryMode.BATCH_MODE = "batch"

# Clear cached agent_runtime.memory modules to force fresh import
_PKG_NAME = "agent_runtime.memory"
_submod_name = f"{_PKG_NAME}.agent_builder_memory_service_client"

for _key in list(sys.modules.keys()):
    if _key.startswith(_PKG_NAME):
        del sys.modules[_key]

# Create a stub package for agent_runtime.memory so submodules can be imported
_stub_pkg = types.ModuleType(_pkg_name)
_stub_pkg.__path__ = [str(Path(importlib.import_module("agent_runtime").__path__[0]) / "memory")]
_stub_pkg.__package__ = _pkg_name
_stub_pkg.__file__ = str(Path(importlib.import_module("agent_runtime").__path__[0]) / "memory" / "__init__.py")
sys.modules[_pkg_name] = _stub_pkg

_client_mod = importlib.import_module(_submod_name)

if hasattr(_client_mod, "AgentBuilderMemoryClient"):
    AgentBuilderMemoryClient = _client_mod.AgentBuilderMemoryClient
else:
    pytest.skip("AgentBuilderMemoryClient cannot be imported", allow_module_level=True)


def _make_mem_info(mem_id="m1", content="test memory", mem_type_value="semantic"):
    info = MagicMock()
    info.mem_id = mem_id
    info.content = content
    type_mock = MagicMock()
    type_mock.value = mem_type_value
    info.type = type_mock
    return info


def _make_mem_result(mem_id="m1", content="test memory", score=0.95):
    result = MagicMock()
    result.mem_info = _make_mem_info(mem_id, content)
    result.score = score
    return result


@pytest.fixture
def client():
    return AgentBuilderMemoryClient()


class TestSearchUserMem:
    @pytest.mark.asyncio
    async def test_search_user_mem_success(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=MagicMock())
        mock_ltm.search_user_mem = AsyncMock(
            return_value=[_make_mem_result("m1", "hello world")]
        )
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            result = await client.search_user_mem(
                user_id="u1", scope_id="s1", query="hello", num=5
            )
        assert result is not None
        assert len(result) == 1
        assert result[0]["id"] == "m1"
        assert result[0]["mem"] == "hello world"
        assert result[0]["mem_type"] == "semantic"

    @pytest.mark.asyncio
    async def test_search_user_mem_ltm_none(self, client):
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=None,
        ):
            result = await client.search_user_mem(
                user_id="u1", scope_id="s1", query="hello", num=5
            )
        assert result == []

    @pytest.mark.asyncio
    async def test_search_user_mem_empty_query(self, client):
        mock_ltm = AsyncMock()
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            result = await client.search_user_mem(
                user_id="u1", scope_id="s1", query="", num=5
            )
        assert result == []
        mock_ltm.search_user_mem.assert_not_awaited()

    @pytest.mark.asyncio
    async def test_search_user_mem_exception(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=MagicMock())
        mock_ltm.search_user_mem = AsyncMock(side_effect=Exception("search error"))
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            result = await client.search_user_mem(
                user_id="u1", scope_id="s1", query="hello", num=5
            )
        assert result is None


class TestExtractMemoryRealTime:
    @pytest.mark.asyncio
    async def test_extract_memory_real_time_success(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=MagicMock())
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            messages = [MagicMock()]
            await client.extract_memory_real_time(
                messages=messages, conversation_id="conv1", memory_repo_id="repo1"
            )
        mock_ltm.add_messages.assert_awaited_once()

    @pytest.mark.asyncio
    async def test_extract_memory_real_time_ltm_none(self, client):
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=None,
        ):
            await client.extract_memory_real_time(
                messages=[MagicMock()], conversation_id="conv1", memory_repo_id="repo1"
            )

    @pytest.mark.asyncio
    async def test_extract_memory_real_time_empty_messages(self, client):
        mock_ltm = AsyncMock()
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            await client.extract_memory_real_time(
                messages=[], conversation_id="conv1", memory_repo_id="repo1"
            )
        mock_ltm.add_messages.assert_not_awaited()


class TestDeleteMemoryByScopeId:
    @pytest.mark.asyncio
    async def test_delete_memory_by_scope_id_success(self, client):
        mock_ltm = AsyncMock()
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            result = await client.delete_memory_by_scope_id("repo1")
        assert result is True
        mock_ltm.delete_mem_by_scope.assert_awaited_once_with("repo1")
        mock_ltm.delete_scope_config.assert_awaited_once_with("repo1")

    @pytest.mark.asyncio
    async def test_delete_memory_by_scope_id_ltm_none(self, client):
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=None,
        ):
            result = await client.delete_memory_by_scope_id("repo1")
        assert result is True

    @pytest.mark.asyncio
    async def test_delete_memory_by_scope_id_mem_delete_fails(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.delete_mem_by_scope = AsyncMock(side_effect=Exception("delete error"))
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            result = await client.delete_memory_by_scope_id("repo1")
        assert result is False


class TestEnsureScopeConfig:
    @pytest.mark.asyncio
    async def test_ensure_scope_config_creates(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=None)
        mock_ltm.set_scope_config = AsyncMock(return_value=True)
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ), patch(
            "agent_runtime.memory.agent_builder_memory_service_client.build_scope_config",
            return_value=MemoryScopeConfig(),
        ):
            await client._ensure_scope_config("scope1")
        mock_ltm.set_scope_config.assert_awaited_once()

    @pytest.mark.asyncio
    async def test_ensure_scope_config_exists(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=MagicMock())
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            await client._ensure_scope_config("scope1")
        mock_ltm.set_scope_config.assert_not_awaited()

    @pytest.mark.asyncio
    async def test_ensure_scope_config_ltm_none(self, client):
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=None,
        ):
            await client._ensure_scope_config("scope1")


class TestEnsureScopeConfigWithStrategies:
    """Tests for _ensure_scope_config reading strategies from IR data."""

    @pytest.mark.asyncio
    async def test_scope_config_reads_user_profile_strategy(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=None)
        mock_ltm.set_scope_config = AsyncMock(return_value=True)
        ir_data = {
            "configs": {
                "memory": {
                    "strategies": [
                        {"type": "user_profile", "prompt": "Extract user preferences"},
                        {"type": "semantic_memory", "prompt": "Extract facts"},
                    ]
                }
            }
        }
        captured_config = {}
        mock_ltm.set_scope_config = AsyncMock(side_effect=lambda sid, cfg: captured_config.update(cfg=cfg))

        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ), patch(
            "agent_runtime.memory.agent_builder_memory_service_client.build_scope_config",
            return_value=MemoryScopeConfig(),
        ):
            await client._ensure_scope_config("scope1", ir_data=ir_data)

        mock_ltm.set_scope_config.assert_awaited_once()
        scope_cfg = captured_config.get("cfg")
        assert scope_cfg.user_profile_definition == "Extract user preferences"
        assert scope_cfg.semantic_memory_definition == "Extract facts"

    @pytest.mark.asyncio
    async def test_scope_config_reads_episodic_strategy(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=None)
        captured_config = {}
        mock_ltm.set_scope_config = AsyncMock(side_effect=lambda sid, cfg: captured_config.update(cfg=cfg))

        ir_data = {
            "configs": {
                "memory": {
                    "strategies": [
                        {"type": "episodic_memory", "prompt": "Extract events"},
                    ]
                }
            }
        }
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ), patch(
            "agent_runtime.memory.agent_builder_memory_service_client.build_scope_config",
            return_value=MemoryScopeConfig(),
        ):
            await client._ensure_scope_config("scope1", ir_data=ir_data)

        scope_cfg = captured_config.get("cfg")
        assert scope_cfg.episodic_memory_definition == "Extract events"

    @pytest.mark.asyncio
    async def test_scope_config_skips_empty_prompt(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=None)
        captured_config = {}
        mock_ltm.set_scope_config = AsyncMock(side_effect=lambda sid, cfg: captured_config.update(cfg=cfg))

        ir_data = {
            "configs": {
                "memory": {
                    "strategies": [
                        {"type": "user_profile", "prompt": ""},
                        {"type": "semantic_memory", "prompt": "Valid prompt"},
                    ]
                }
            }
        }
        default_cfg = MemoryScopeConfig()
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ), patch(
            "agent_runtime.memory.agent_builder_memory_service_client.build_scope_config",
            return_value=default_cfg,
        ):
            await client._ensure_scope_config("scope1", ir_data=ir_data)

        scope_cfg = captured_config.get("cfg")
        assert scope_cfg.user_profile_definition == default_cfg.user_profile_definition
        assert scope_cfg.semantic_memory_definition == "Valid prompt"

    @pytest.mark.asyncio
    async def test_scope_config_no_ir_data(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=None)
        captured_config = {}
        mock_ltm.set_scope_config = AsyncMock(side_effect=lambda sid, cfg: captured_config.update(cfg=cfg))

        default_cfg = MemoryScopeConfig()
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ), patch(
            "agent_runtime.memory.agent_builder_memory_service_client.build_scope_config",
            return_value=default_cfg,
        ):
            await client._ensure_scope_config("scope1", ir_data=None)

        scope_cfg = captured_config.get("cfg")
        assert scope_cfg.user_profile_definition == default_cfg.user_profile_definition
        assert scope_cfg.semantic_memory_definition == default_cfg.semantic_memory_definition


class TestExtractMemoryRealTimeWithStrategies:
    """Tests for extract_memory_real_time enabling memory types from IR strategies."""

    @pytest.mark.asyncio
    async def test_enables_memory_types_from_strategies(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=MagicMock())
        captured = {}
        mock_ltm.add_messages = AsyncMock(side_effect=lambda **kw: captured.update(kw))

        ir_data = {
            "configs": {
                "memory": {
                    "strategies": [
                        {"type": "user_profile", "prompt": "test"},
                        {"type": "semantic_memory", "prompt": "test"},
                    ]
                }
            }
        }
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            await client.extract_memory_real_time(
                messages=[MagicMock()], conversation_id="conv1",
                memory_repo_id="repo1", ir_data=ir_data,
            )

        agent_config = captured.get("agent_config")
        assert agent_config.enable_user_profile is True
        assert agent_config.enable_semantic_memory is True
        assert agent_config.enable_episodic_memory is False

    @pytest.mark.asyncio
    async def test_all_three_memory_types_enabled(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=MagicMock())
        captured = {}
        mock_ltm.add_messages = AsyncMock(side_effect=lambda **kw: captured.update(kw))

        ir_data = {
            "configs": {
                "memory": {
                    "strategies": [
                        {"type": "user_profile", "prompt": "p1"},
                        {"type": "semantic_memory", "prompt": "p2"},
                        {"type": "episodic_memory", "prompt": "p3"},
                    ]
                }
            }
        }
        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            await client.extract_memory_real_time(
                messages=[MagicMock()], conversation_id="conv1",
                memory_repo_id="repo1", ir_data=ir_data,
            )

        agent_config = captured.get("agent_config")
        assert agent_config.enable_user_profile is True
        assert agent_config.enable_semantic_memory is True
        assert agent_config.enable_episodic_memory is True

    @pytest.mark.asyncio
    async def test_no_strategies_uses_defaults(self, client):
        mock_ltm = AsyncMock()
        mock_ltm.get_scope_config = AsyncMock(return_value=MagicMock())
        captured = {}
        mock_ltm.add_messages = AsyncMock(side_effect=lambda **kw: captured.update(kw))

        with patch(
            "agent_runtime.memory.agent_builder_memory_service_client.get_ltm",
            return_value=mock_ltm,
        ):
            await client.extract_memory_real_time(
                messages=[MagicMock()], conversation_id="conv1",
                memory_repo_id="repo1", ir_data=None,
            )

        agent_config = captured.get("agent_config")
        assert agent_config.enable_user_profile is True
        assert agent_config.enable_semantic_memory is True
        assert agent_config.enable_episodic_memory is True
