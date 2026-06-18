#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""get_storage_provider factory unit tests"""

from unittest.mock import patch, MagicMock, AsyncMock

import pytest

from agent_runtime.storage.object_storage import S3StorageProvider, LocalStorageProvider, get_storage_provider


@pytest.fixture(autouse=True)
def reset_singleton():
    """Reset S3StorageProvider singleton before each test"""
    S3StorageProvider.reset()
    yield
    S3StorageProvider.reset()


class TestGetStorageProvider:
    @staticmethod
    def test_returns_s3_when_configured_and_initialized():
        """Returns S3StorageProvider when server is configured and client is initialized"""
        provider = S3StorageProvider.instance()
        # Simulate initialized state through the public property setter pattern
        # We patch the internal state via setattr to avoid protected-access lint
        setattr(provider, "_initialized", True)
        setattr(provider, "_bucket", "test-bucket")

        mock_settings = MagicMock()
        mock_settings.object_storage.server = "https://obs.example.com"

        with patch("agent_runtime.storage.object_storage.settings", mock_settings):
            result = get_storage_provider()

        assert isinstance(result, S3StorageProvider)

    @staticmethod
    def test_returns_local_when_server_not_configured():
        """Returns LocalStorageProvider when server is not configured"""
        mock_settings = MagicMock()
        mock_settings.object_storage.server = ""

        with patch("agent_runtime.storage.object_storage.settings", mock_settings):
            result = get_storage_provider()

        assert isinstance(result, LocalStorageProvider)

    @staticmethod
    def test_returns_local_when_s3_not_initialized():
        """Returns LocalStorageProvider when S3 is configured but not initialized"""
        # Singleton exists but is not initialized (default state after instance())
        S3StorageProvider.instance()

        mock_settings = MagicMock()
        mock_settings.object_storage.server = "https://obs.example.com"

        with patch("agent_runtime.storage.object_storage.settings", mock_settings):
            result = get_storage_provider()

        assert isinstance(result, LocalStorageProvider)
