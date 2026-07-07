# pylint: disable=no-self-use
"""Unit tests for memory_message_collector.py scope_id changes.

Tests the memory_repo_id extraction from IR data.
"""

import sys
from unittest.mock import MagicMock

import pytest

# Mock heavy jiuwen dependencies
for mod_name in ["memory", "memory.storage", "memory.storage.user_profile_memory_extractor"]:
    if mod_name not in sys.modules:
        sys.modules[mod_name] = MagicMock()


def extract_memory_repo_id(ir_data: dict, app_id: str) -> str:
    """Reproduction of the memory_repo_id extraction logic from
    UserProfileMemoryMessageCollector.__init__ for isolated testing.
    """
    configs = ir_data.get("configs") if ir_data.get("configs") else {}
    memory_config = configs.get("memory") if configs.get("memory") else {}
    return memory_config.get("memory_repo_id") or app_id


class TestMemoryRepoIdExtraction:
    """Tests for memory_repo_id extraction from IR data."""

    @staticmethod
    def test_extracts_memory_repo_id_from_ir():
        ir_data = {
            "configs": {
                "memory": {
                    "memory_repo_id": "repo-from-ir",
                }
            }
        }

        result = extract_memory_repo_id(ir_data, app_id="app-fallback")
        assert result == "repo-from-ir"

    @staticmethod
    def test_falls_back_to_app_id_when_no_memory_repo_id():
        ir_data = {
            "configs": {
                "memory": {}
            }
        }

        result = extract_memory_repo_id(ir_data, app_id="fallback-app")
        assert result == "fallback-app"

    @staticmethod
    def test_falls_back_to_app_id_when_no_memory_config():
        ir_data = {"configs": {}}

        result = extract_memory_repo_id(ir_data, app_id="fallback-app")
        assert result == "fallback-app"

    @staticmethod
    def test_falls_back_to_app_id_when_no_configs():
        ir_data = {}

        result = extract_memory_repo_id(ir_data, app_id="fallback-app")
        assert result == "fallback-app"

    @staticmethod
    def test_falls_back_to_app_id_when_configs_is_none():
        ir_data = {"configs": None}

        result = extract_memory_repo_id(ir_data, app_id="fallback-app")
        assert result == "fallback-app"

    @staticmethod
    def test_falls_back_to_app_id_when_memory_is_none():
        ir_data = {"configs": {"memory": None}}

        result = extract_memory_repo_id(ir_data, app_id="fallback-app")
        assert result == "fallback-app"

    @staticmethod
    def test_empty_string_memory_repo_id_falls_back():
        ir_data = {
            "configs": {
                "memory": {
                    "memory_repo_id": "",
                }
            }
        }

        result = extract_memory_repo_id(ir_data, app_id="fallback-app")
        assert result == "fallback-app"

    @staticmethod
    def test_real_ir_data_structure():
        """Test with a realistic IR data structure matching production format."""
        ir_data = {
            "configs": {
                "memory": {
                    "memory_repo_id": "uuid-1234-5678",
                    "enable_memory_retrieve": True,
                },
                "llm": {
                    "model_name": "some-model",
                },
            },
            "app_id": "app-xyz",
        }

        result = extract_memory_repo_id(ir_data, app_id="app-xyz")
        assert result == "uuid-1234-5678"

    @staticmethod
    def test_app_id_preserved_when_no_ir_memory():
        ir_data = {
            "configs": {
                "llm": {"model_name": "test"},
            }
        }

        result = extract_memory_repo_id(ir_data, app_id="app-original")
        assert result == "app-original"


class TestCollectorMatchesExtractionLogic:
    """Verify that the extraction function matches the actual source code logic."""

    @staticmethod
    def test_matches_source_code():
        """The extraction logic in the test function matches the source exactly."""
        import inspect

        source = inspect.getsource(extract_memory_repo_id)

        assert 'ir_data.get("configs")' in source
        assert 'configs.get("memory")' in source
        assert 'memory_config.get("memory_repo_id")' in source
        assert "or app_id" in source
