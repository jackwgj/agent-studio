"""Unit tests for internal memory routes (DELETE endpoint)."""
from unittest.mock import AsyncMock, patch

import pytest

from httpx import ASGITransport, AsyncClient

from agent_runtime.memory.internal_routes import memory_internal_router
from fastapi import FastAPI

# Use valid UUIDs for path parameters (UUID validation is enforced)
REPO_ID = "11111111-1111-1111-1111-111111111111"
INVALID_ID = "not-a-valid-uuid"


def _make_app():
    app = FastAPI()
    app.include_router(memory_internal_router)
    return app


class TestDeleteMemoryRepo:
    @pytest.mark.asyncio
    async def test_delete_ltm_none(self):
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=None):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.delete(f"/internal/v1/memory-repos/{REPO_ID}")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "skipped"

    @pytest.mark.asyncio
    async def test_delete_success(self):
        mock_ltm = AsyncMock()
        mock_ltm.delete_mem_by_scope = AsyncMock(return_value=True)
        mock_ltm.delete_scope_config = AsyncMock(return_value=True)
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=mock_ltm):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.delete(f"/internal/v1/memory-repos/{REPO_ID}")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "ok"
        mock_ltm.delete_mem_by_scope.assert_awaited_once_with(REPO_ID)
        mock_ltm.delete_scope_config.assert_awaited_once_with(REPO_ID)

    @pytest.mark.asyncio
    async def test_delete_mem_fails(self):
        mock_ltm = AsyncMock()
        mock_ltm.delete_mem_by_scope = AsyncMock(side_effect=Exception("db error"))
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=mock_ltm):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.delete(f"/internal/v1/memory-repos/{REPO_ID}")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "error"
        assert "db error" in data["reason"]

    @pytest.mark.asyncio
    async def test_delete_scope_config_fails_non_critical(self):
        mock_ltm = AsyncMock()
        mock_ltm.delete_mem_by_scope = AsyncMock(return_value=True)
        mock_ltm.delete_scope_config = AsyncMock(side_effect=Exception("config error"))
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=mock_ltm):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.delete(f"/internal/v1/memory-repos/{REPO_ID}")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "ok"

    @pytest.mark.asyncio
    async def test_delete_invalid_uuid_returns_400(self):
        """Invalid UUID format should return HTTP 400."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.delete(f"/internal/v1/memory-repos/{INVALID_ID}")
        assert resp.status_code == 400
        data = resp.json()
        assert data["status"] == "error"
        assert "not a valid UUID" in data["reason"]


class TestBatchDeleteMemories:
    """Tests for POST /{repo}/users/{user}/memories/batch-delete."""

    @pytest.mark.asyncio
    async def test_empty_memory_ids_returns_400(self):
        """Empty memory_ids list should return HTTP 400, not 200."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.post(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories/batch-delete",
                    json={"memory_ids": []},
                )
        assert resp.status_code == 400
        data = resp.json()
        assert data["status"] == "error"

    @pytest.mark.asyncio
    async def test_missing_memory_ids_key_returns_400(self):
        """Body without memory_ids key should return HTTP 400."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.post(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories/batch-delete",
                    json={},
                )
        assert resp.status_code == 400

    @pytest.mark.asyncio
    async def test_batch_delete_success(self):
        """Valid memory_ids should delete and return 200."""
        mock_ltm = AsyncMock()
        mock_ltm.delete_mem_by_id = AsyncMock(return_value=None)
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=mock_ltm):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.post(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories/batch-delete",
                    json={"memory_ids": ["mem-1", "mem-2"]},
                )
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "ok"
        assert mock_ltm.delete_mem_by_id.await_count == 2

    @pytest.mark.asyncio
    async def test_batch_delete_user_id_lowercased(self):
        """user_id should be lowercased before calling LTM."""
        mock_ltm = AsyncMock()
        mock_ltm.delete_mem_by_id = AsyncMock(return_value=None)
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=mock_ltm):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.post(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/TestUser/memories/batch-delete",
                    json={"memory_ids": ["mem-1"]},
                )
        assert resp.status_code == 200
        # Verify LTM was called with lowercased user_id
        mock_ltm.delete_mem_by_id.assert_awaited_once_with("mem-1", "testuser", REPO_ID)

    @pytest.mark.asyncio
    async def test_batch_delete_ltm_none(self):
        """LTM not initialized should return skipped."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=None):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.post(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories/batch-delete",
                    json={"memory_ids": ["mem-1"]},
                )
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "skipped"

    @pytest.mark.asyncio
    async def test_batch_delete_partial_error(self):
        """If some deletes fail, return partial status with errors."""
        mock_ltm = AsyncMock()
        mock_ltm.delete_mem_by_id = AsyncMock(side_effect=[None, Exception("not found")])
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=mock_ltm):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.post(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories/batch-delete",
                    json={"memory_ids": ["mem-1", "mem-2"]},
                )
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "partial"
        assert len(data["errors"]) == 1
        assert data["errors"][0]["mem_id"] == "mem-2"

    @pytest.mark.asyncio
    async def test_batch_delete_invalid_repo_uuid_returns_400(self):
        """Invalid repo UUID format should return HTTP 400."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.post(
                    f"/internal/v1/memory-repos/{INVALID_ID}/users/testuser/memories/batch-delete",
                    json={"memory_ids": ["mem-1"]},
                )
        assert resp.status_code == 400
        data = resp.json()
        assert "not a valid UUID" in data["reason"]


class TestListUserMemories:
    """Tests for GET /{repo}/users/{user}/memories."""

    @pytest.mark.asyncio
    async def test_list_ltm_none(self):
        """LTM not initialized should return empty."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=None):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.get(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories",
                )
        assert resp.status_code == 200
        data = resp.json()
        assert data["total"] == 0
        assert data["memories"] == []

    @pytest.mark.asyncio
    async def test_list_user_id_lowercased(self):
        """user_id should be lowercased before calling LTM."""
        mock_ltm = AsyncMock()
        mock_ltm.get_user_mem_by_page = AsyncMock(return_value=[])
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=mock_ltm):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.get(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/TestUser/memories?page_size=10&page_num=1",
                )
        assert resp.status_code == 200
        mock_ltm.get_user_mem_by_page.assert_awaited_once()
        call_kwargs = mock_ltm.get_user_mem_by_page.call_args.kwargs
        assert call_kwargs["user_id"] == "testuser"

    @pytest.mark.asyncio
    async def test_list_page_size_validation(self):
        """page_size must be between 1 and 1000."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                # page_size=0 should fail
                resp = await client.get(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories?page_size=0",
                )
                assert resp.status_code == 422

                # page_size=1001 should fail
                resp = await client.get(
                    f"/internal/v1/memory-repos/{REPO_ID}/users/testuser/memories?page_size=1001",
                )
                assert resp.status_code == 422

    @pytest.mark.asyncio
    async def test_list_invalid_repo_uuid_returns_400(self):
        """Invalid repo UUID format should return HTTP 400."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.get(
                    f"/internal/v1/memory-repos/{INVALID_ID}/users/testuser/memories",
                )
        assert resp.status_code == 400
        data = resp.json()
        assert "not a valid UUID" in data["reason"]


class TestUpdateMemory:
    """Tests for PUT /{repo}/memories/{memory_id}."""

    @pytest.mark.asyncio
    async def test_update_invalid_repo_uuid_returns_400(self):
        """Invalid repo UUID format should return HTTP 400."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.put(
                    f"/internal/v1/memory-repos/{INVALID_ID}/memories/{REPO_ID}",
                    json={"user_id": "u", "content": "c"},
                )
        assert resp.status_code == 400
        assert "memory_repo_id" in resp.json()["reason"]

    @pytest.mark.asyncio
    async def test_update_invalid_memory_uuid_returns_400(self):
        """Invalid memory_id UUID format should return HTTP 400."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.put(
                    f"/internal/v1/memory-repos/{REPO_ID}/memories/{INVALID_ID}",
                    json={"user_id": "u", "content": "c"},
                )
        assert resp.status_code == 400
        assert "memory_id" in resp.json()["reason"]


class TestClearUserMemories:
    """Tests for DELETE /{repo}/users/{user}/memories."""

    @pytest.mark.asyncio
    async def test_clear_invalid_repo_uuid_returns_400(self):
        """Invalid repo UUID format should return HTTP 400."""
        with patch("agent_runtime.memory.internal_routes.get_ltm", return_value=AsyncMock()):
            app = _make_app()
            async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
                resp = await client.delete(
                    f"/internal/v1/memory-repos/{INVALID_ID}/users/testuser/memories",
                )
        assert resp.status_code == 400
        assert "not a valid UUID" in resp.json()["reason"]
