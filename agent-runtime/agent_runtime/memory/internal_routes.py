"""Internal endpoints for memory repo and memory item management.

Called by Java studio-runtime-service via JiuWenClient, these endpoints
operate directly on the LTM (LongTermMemory) singleton.
"""

import logging
import uuid

from fastapi import APIRouter, Query
from fastapi.responses import JSONResponse

from agent_runtime.memory.adapter.ltm_manager import get_ltm

logger = logging.getLogger(__name__)


def _validate_uuid(value: str, field_name: str = "id") -> str | None:
    """Validate UUID format. Returns error message if invalid, None if valid."""
    try:
        uuid.UUID(value)
        return None
    except (ValueError, AttributeError):
        return f"Invalid {field_name} format: {value!r} is not a valid UUID"

memory_internal_router = APIRouter(prefix="/internal/v1/memory-repos", tags=["memory-internal"])


# ── Memory repo lifecycle ──


@memory_internal_router.delete("/{memory_repo_id}")
async def delete_memory_repo(memory_repo_id: str):
    """Delete all memory data for a memory repo.

    Called by Java Manager via Runtime RCE when a memory repo is deleted.
    """
    err = _validate_uuid(memory_repo_id, "memory_repo_id")
    if err:
        return JSONResponse(status_code=400, content={"status": "error", "reason": err})

    ltm = get_ltm()
    if ltm is None:
        return {"status": "skipped", "reason": "memory library not initialized"}

    try:
        await ltm.delete_mem_by_scope(memory_repo_id)
    except Exception as e:
        logger.error("Failed to delete memory data for repo %s: %s", memory_repo_id, e)
        return {"status": "error", "reason": str(e)}

    try:
        await ltm.delete_scope_config(memory_repo_id)
    except Exception as e:
        logger.warning(
            "Failed to delete scope config for repo %s (non-critical): %s",
            memory_repo_id,
            e,
        )

    return {"status": "ok"}


# ── Memory item CRUD ──


@memory_internal_router.delete("/{memory_repo_id}/users/{user_id}/memories")
async def clear_user_memories(memory_repo_id: str, user_id: str):
    """Clear all memories for a user within a memory repo scope."""
    err = _validate_uuid(memory_repo_id, "memory_repo_id")
    if err:
        return JSONResponse(status_code=400, content={"status": "error", "reason": err})

    ltm = get_ltm()
    if ltm is None:
        return {"status": "skipped", "reason": "memory library not initialized"}

    # LTM stores user_id in lowercase (OpenSearch index names must be lowercase)
    user_id = user_id.lower()

    try:
        await ltm.delete_mem_by_user_id(user_id, memory_repo_id)
        return {"status": "ok"}
    except Exception as e:
        logger.error(
            "Failed to clear memories for user %s in repo %s: %s",
            user_id,
            memory_repo_id,
            e,
        )
        return {"status": "error", "reason": str(e)}


@memory_internal_router.post("/{memory_repo_id}/users/{user_id}/memories/batch-delete")
async def batch_delete_memories(memory_repo_id: str, user_id: str, body: dict):
    """Batch-delete memories by ID list.

    Body: {"memory_ids": ["id1", "id2", ...]}
    """
    err = _validate_uuid(memory_repo_id, "memory_repo_id")
    if err:
        return JSONResponse(status_code=400, content={"status": "error", "reason": err})

    ltm = get_ltm()
    if ltm is None:
        return {"status": "skipped", "reason": "memory library not initialized"}

    # LTM stores user_id in lowercase (OpenSearch index names must be lowercase)
    user_id = user_id.lower()

    memory_ids = body.get("memory_ids", [])
    if not memory_ids:
        return JSONResponse(status_code=400, content={"status": "error", "reason": "memory_ids is required"})

    errors = []
    for mem_id in memory_ids:
        try:
            await ltm.delete_mem_by_id(mem_id, user_id, memory_repo_id)
        except Exception as e:
            errors.append({"mem_id": mem_id, "error": str(e)})

    if errors:
        return {"status": "partial", "errors": errors}
    return {"status": "ok"}


@memory_internal_router.get("/{memory_repo_id}/users/{user_id}/memories")
async def list_user_memories(
    memory_repo_id: str,
    user_id: str,
    page_size: int = Query(10, ge=1, le=1000),
    page_num: int = Query(1, ge=1),
    memory_type: str = Query(None),
):
    """Paginated list of memories for a user within a memory repo scope."""
    err = _validate_uuid(memory_repo_id, "memory_repo_id")
    if err:
        return JSONResponse(status_code=400, content={"status": "error", "reason": err})

    ltm = get_ltm()
    if ltm is None:
        return {"total": 0, "memories": []}

    # LTM stores user_id in lowercase (OpenSearch index names must be lowercase)
    user_id = user_id.lower()

    try:
        # Do NOT pass memory_type to LTM — it expects an enum, not a string.
        # Retrieve all and filter in Python.
        results = await ltm.get_user_mem_by_page(
            user_id=user_id,
            scope_id=memory_repo_id,
            page_size=page_size * 2,
            page_idx=page_num,
        )

        memories = []
        for r in (results or []):
            info = r.mem_info if hasattr(r, "mem_info") else r
            mem_type = getattr(info, "type", None)
            if mem_type is not None and hasattr(mem_type, "value"):
                mem_type_str = mem_type.value
            elif mem_type is not None:
                mem_type_str = str(mem_type)
            else:
                mem_type_str = ""
            if memory_type and mem_type_str != memory_type:
                continue
            memories.append({
                "memory_id": getattr(info, "mem_id", ""),
                "content": getattr(info, "content", ""),
                "type": mem_type_str,
                "last_update_time": str(getattr(info, "timestamp", "")),
            })

        return {"total": len(memories), "memories": memories}
    except Exception as e:
        logger.error(
            "Failed to list memories for user %s in repo %s: %s",
            user_id,
            memory_repo_id,
            e,
            exc_info=True,
        )
        return {"total": 0, "memories": [], "error": str(e)}


@memory_internal_router.post("/{memory_repo_id}/users/{user_id}/memories/search")
async def search_memories(
    memory_repo_id: str,
    user_id: str,
    body: dict,
):
    """Semantic search for memories within a memory repo scope.

    Body: {"query": "...", "top_k": 10, "threshold": 0.3}
    """
    err = _validate_uuid(memory_repo_id, "memory_repo_id")
    if err:
        return JSONResponse(status_code=400, content={"status": "error", "reason": err})

    ltm = get_ltm()
    if ltm is None:
        return {"total": 0, "memories": []}

    # LTM stores user_id in lowercase (OpenSearch index names must be lowercase)
    user_id = user_id.lower()

    query = body.get("query", "")
    if not query:
        return JSONResponse(status_code=400, content={"status": "error", "reason": "query is required"})

    top_k = body.get("top_k", 10)
    threshold = body.get("threshold", 0.3)

    try:
        results = await ltm.search_user_mem(
            query=query,
            num=top_k,
            user_id=user_id,
            scope_id=memory_repo_id,
            threshold=threshold,
        )

        memories = []
        for r in (results or []):
            info = r.mem_info if hasattr(r, "mem_info") else r
            mem_type = getattr(info, "type", None)
            if mem_type is not None and hasattr(mem_type, "value"):
                mem_type_str = mem_type.value
            elif mem_type is not None:
                mem_type_str = str(mem_type)
            else:
                mem_type_str = ""

            memories.append({
                "memory_id": getattr(info, "mem_id", ""),
                "content": getattr(info, "content", ""),
                "type": mem_type_str,
                "score": getattr(r, "score", None),
                "last_update_time": str(getattr(info, "timestamp", "")),
            })

        return {"total": len(memories), "memories": memories}
    except Exception as e:
        logger.error(
            "Failed to search memories for user %s in repo %s: %s",
            user_id,
            memory_repo_id,
            e,
            exc_info=True,
        )
        return {"total": 0, "memories": [], "error": str(e)}


@memory_internal_router.put("/{memory_repo_id}/memories/{memory_id}")
async def update_memory(memory_repo_id: str, memory_id: str, body: dict):
    """Update a single memory's content.

    Body: {"user_id": "...", "content": "new content"}
    """
    err = _validate_uuid(memory_repo_id, "memory_repo_id")
    if err:
        return JSONResponse(status_code=400, content={"status": "error", "reason": err})
    if not memory_id or not memory_id.strip():
        return JSONResponse(status_code=400, content={"status": "error", "reason": "memory_id is required"})

    ltm = get_ltm()
    if ltm is None:
        return {"status": "skipped", "reason": "memory library not initialized"}

    user_id = body.get("user_id", "")
    content = body.get("content", "")

    # LTM stores user_id in lowercase (OpenSearch index names must be lowercase)
    user_id = user_id.lower()

    try:
        await ltm.update_mem_by_id(memory_id, content, user_id, memory_repo_id)
        return {"status": "ok"}
    except Exception as e:
        logger.error(
            "Failed to update memory %s in repo %s: %s",
            memory_id,
            memory_repo_id,
            e,
        )
        return {"status": "error", "reason": str(e)}
