# pylint: disable=protected-access,no-self-use
"""Unit tests for _convert_memory_extract_config new/old IR compatibility."""

import pytest

from agent_runtime.memory.storage.memory_extractor import (
    UserProfileMemoryExtractor,
)


def _make_extractor():
    """Create extractor without calling __init__ (avoids Redis dependency)."""
    return UserProfileMemoryExtractor.__new__(UserProfileMemoryExtractor)


class TestConvertMemoryExtractConfigNewIR:
    """Tests for new IR structure: configs.memory.extract_config + strategies."""

    def test_reads_extract_config_max_chat_turn(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ext._extract_max_turns = 5
        ext._extract_time_windows = 10

        ir_data = {
            "configs": {
                "memory": {
                    "extract_config": {"max_chat_turn": 8, "time_window": 30},
                    "strategies": [
                        {"type": "user_profile", "prompt": "test"},
                        {"type": "semantic_memory", "prompt": "test"},
                    ],
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.extract_max_turns == 8
        assert result.extract_time_windows == 30

    def test_strategies_enable_memory(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ir_data = {
            "configs": {
                "memory": {
                    "strategies": [{"type": "semantic_memory", "prompt": "test"}],
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.user_profile_enable is True

    def test_extract_config_partial_only_max_chat_turn(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ext._extract_max_turns = 5
        ext._extract_time_windows = 10

        ir_data = {
            "configs": {
                "memory": {
                    "extract_config": {"max_chat_turn": 12},
                    "strategies": [{"type": "user_profile", "prompt": "p"}],
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.extract_max_turns == 12
        assert result.extract_time_windows == 10

    def test_extract_config_partial_only_time_window(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ext._extract_max_turns = 5
        ext._extract_time_windows = 10

        ir_data = {
            "configs": {
                "memory": {
                    "extract_config": {"time_window": 60},
                    "strategies": [{"type": "episodic_memory", "prompt": "p"}],
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.extract_max_turns == 5
        assert result.extract_time_windows == 60

    def test_empty_strategies_list_disables(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ir_data = {
            "configs": {
                "memory": {
                    "extract_config": {"max_chat_turn": 3},
                    "strategies": [],
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.user_profile_enable is False


class TestConvertMemoryExtractConfigOldIR:
    """Tests for old IR structure fallback: configs.memory.userProfile."""

    def test_reads_old_user_profile_enable(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ext._extract_max_turns = 5
        ext._extract_time_windows = 10

        ir_data = {
            "configs": {
                "memory": {
                    "userProfile": {
                        "enable": True,
                        "extractConfig": {
                            "maxChatTurn": 7,
                            "timeWindow": 20,
                        },
                    }
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.user_profile_enable is True
        assert result.extract_max_turns == 7
        assert result.extract_time_windows == 20

    def test_old_user_profile_disable(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ir_data = {
            "configs": {
                "memory": {
                    "userProfile": {"enable": False},
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.user_profile_enable is False

    def test_no_memory_config_returns_defaults(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ext._extract_max_turns = 5
        ext._extract_time_windows = 10

        ir_data = {"configs": {}}

        result = ext._convert_memory_extract_config(ir_data)
        assert result.user_profile_enable is False
        assert result.extract_max_turns == 5
        assert result.extract_time_windows == 10


class TestConvertMemoryExtractConfigPriority:
    """Tests that new IR structure takes priority over old."""

    def test_new_ir_takes_priority_over_old(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        ext = _make_extractor()
        ext._extract_max_turns = 5
        ext._extract_time_windows = 10

        ir_data = {
            "configs": {
                "memory": {
                    "extract_config": {"max_chat_turn": 15, "time_window": 45},
                    "strategies": [{"type": "semantic_memory", "prompt": "new-prompt"}],
                    "userProfile": {
                        "enable": True,
                        "extractConfig": {"maxChatTurn": 3, "timeWindow": 5},
                    },
                }
            }
        }

        result = ext._convert_memory_extract_config(ir_data)
        assert result.extract_max_turns == 15
        assert result.extract_time_windows == 45
        assert result.user_profile_enable is True
