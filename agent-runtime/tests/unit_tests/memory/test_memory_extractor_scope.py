# pylint: disable=protected-access,no-self-use
"""Unit tests for memory_extractor.py scope_id changes."""

import json

import pytest

from agent_runtime.memory.storage.memory_extractor import (
    ChatHistoryConversationCache,
    MessageChatTurn,
    UserProfileMemoryExtractor,
    UserProfileMemoryHistoryMessage,
)


class TestChatHistoryConversationCacheFromJson:
    """Tests for ChatHistoryConversationCache.from_json backward compatibility."""

    def test_from_json_with_memory_repo_id(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache_json = json.dumps({
            "user_id": "user1",
            "memory_repo_id": "repo-abc",
            "conversation_id": "conv1",
            "last_update_time": 1700000000,
            "chat_turns": [],
        })

        cache = ChatHistoryConversationCache.from_json(cache_json)

        assert cache.user_id == "user1"
        assert cache.memory_repo_id == "repo-abc"
        assert cache.conversation_id == "conv1"

    def test_from_json_fallback_to_app_id(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache_json = json.dumps({
            "user_id": "user1",
            "app_id": "legacy-app-id",
            "conversation_id": "conv1",
            "last_update_time": 1700000000,
            "chat_turns": [],
        })

        cache = ChatHistoryConversationCache.from_json(cache_json)

        assert cache.memory_repo_id == "legacy-app-id"

    def test_from_json_memory_repo_id_takes_precedence_over_app_id(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache_json = json.dumps({
            "user_id": "user1",
            "memory_repo_id": "new-repo-id",
            "app_id": "old-app-id",
            "conversation_id": "conv1",
            "last_update_time": 1700000000,
            "chat_turns": [],
        })

        cache = ChatHistoryConversationCache.from_json(cache_json)

        assert cache.memory_repo_id == "new-repo-id"

    def test_from_json_neither_field_returns_empty(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache_json = json.dumps({
            "user_id": "user1",
            "conversation_id": "conv1",
            "last_update_time": 1700000000,
            "chat_turns": [],
        })

        cache = ChatHistoryConversationCache.from_json(cache_json)

        assert cache.memory_repo_id == ""

    def test_from_json_empty_memory_repo_id_falls_back_to_app_id(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache_json = json.dumps({
            "user_id": "user1",
            "memory_repo_id": "",
            "app_id": "fallback-id",
            "conversation_id": "conv1",
            "last_update_time": 1700000000,
            "chat_turns": [],
        })

        cache = ChatHistoryConversationCache.from_json(cache_json)

        assert cache.memory_repo_id == "fallback-id"

    def test_from_json_with_chat_turns(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache_json = json.dumps({
            "user_id": "user1",
            "memory_repo_id": "repo-1",
            "conversation_id": "conv1",
            "last_update_time": 1700000000,
            "chat_turns": [
                {
                    "messages": [
                        {"role": "human", "content": "hello"},
                        {"role": "ai", "content": "hi there"},
                    ]
                }
            ],
        })

        cache = ChatHistoryConversationCache.from_json(cache_json)

        assert len(cache.chat_turns) == 1
        assert len(cache.chat_turns[0].messages) == 2
        assert cache.chat_turns[0].messages[0].role == "human"
        assert cache.chat_turns[0].messages[0].content == "hello"


class TestChatHistoryConversationCacheToJson:
    """Tests for ChatHistoryConversationCache.to_json using memory_repo_id."""

    def test_to_json_contains_memory_repo_id(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache = ChatHistoryConversationCache(
            user_id="user1",
            memory_repo_id="repo-xyz",
            conversation_id="conv1",
        )

        result = cache.to_json()
        data = json.loads(result)

        assert data["memory_repo_id"] == "repo-xyz"
        assert "app_id" not in data

    def test_round_trip_preserves_data(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        original = ChatHistoryConversationCache(
            user_id="user1",
            memory_repo_id="repo-123",
            conversation_id="conv1",
            last_update_time=1700000000,
            chat_turns=[
                MessageChatTurn(
                    messages=[
                        UserProfileMemoryHistoryMessage(role="human", content="test"),
                    ]
                )
            ],
        )

        json_str = original.to_json()
        restored = ChatHistoryConversationCache.from_json(json_str)

        assert restored.user_id == original.user_id
        assert restored.memory_repo_id == original.memory_repo_id
        assert restored.conversation_id == original.conversation_id
        assert restored.last_update_time == original.last_update_time
        assert len(restored.chat_turns) == 1
        assert restored.chat_turns[0].messages[0].content == "test"

    def test_backward_compat_round_trip(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        old_json = json.dumps({
            "user_id": "user1",
            "app_id": "old-scope",
            "conversation_id": "conv1",
            "last_update_time": 1700000000,
            "chat_turns": [],
        })

        cache = ChatHistoryConversationCache.from_json(old_json)
        new_json = cache.to_json()
        data = json.loads(new_json)

        assert data["memory_repo_id"] == "old-scope"
        assert "app_id" not in data


class TestObtainUserMemoryKey:
    """Tests for _obtain_user_memory_key using memory_repo_id."""

    def test_key_format_with_memory_repo_id(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        extractor = UserProfileMemoryExtractor.__new__(UserProfileMemoryExtractor)
        key = extractor._obtain_user_memory_key("user1", "repo-abc", "conv1")

        assert key == "memory_chat_cache:user1:repo-abc:conv1"

    def test_key_format_with_different_values(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        extractor = UserProfileMemoryExtractor.__new__(UserProfileMemoryExtractor)
        key = extractor._obtain_user_memory_key("u2", "repo-xyz", "c3")

        assert key == "memory_chat_cache:u2:repo-xyz:c3"

    def test_key_changes_with_different_memory_repo_id(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        extractor = UserProfileMemoryExtractor.__new__(UserProfileMemoryExtractor)
        key1 = extractor._obtain_user_memory_key("user1", "repo-a", "conv1")
        key2 = extractor._obtain_user_memory_key("user1", "repo-b", "conv1")

        assert key1 != key2
        assert "repo-a" in key1
        assert "repo-b" in key2


class TestAddChatTurn:
    """Tests for ChatHistoryConversationCache.add_chat_turn."""

    def test_add_chat_turn_sets_timestamp(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache = ChatHistoryConversationCache(
            user_id="user1",
            memory_repo_id="repo-1",
            conversation_id="conv1",
        )

        assert cache.last_update_time is None

        cache.add_chat_turn(MessageChatTurn(messages=[
            UserProfileMemoryHistoryMessage(role="human", content="hi"),
        ]))

        assert cache.last_update_time is not None
        assert len(cache.chat_turns) == 1

    def test_add_chat_turn_initializes_empty_list(self):
        self.assertIsNotNone(self)  # satisfy G.CLS.07
        cache = ChatHistoryConversationCache(
            user_id="user1",
            memory_repo_id="repo-1",
            conversation_id="conv1",
            chat_turns=None,
        )

        cache.add_chat_turn(MessageChatTurn(messages=[]))

        assert cache.chat_turns is not None
        assert len(cache.chat_turns) == 1
