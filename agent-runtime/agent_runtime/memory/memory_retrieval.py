"""Shared long-term memory retrieval helper.

Used by both the direct-workflow runner (``WorkflowRunner._retrieve_memory``)
and the LLM component (``LLMChain``) so that memory retrieval works
identically whether a workflow runs directly or is invoked by a
multi-agent (controller) — the multi-agent path executes sub-workflows
through the agent-core engine and otherwise bypasses the jiuwen
``Workflow._update_runtime_context`` retrieval hook.

The function performs a vector search over the LTM index scoped by
``user_id`` + ``scope_id`` (the memory repo id), formats the hits with the
shared ``MEMORY_USAGE_PROMPT``, and returns the prompt string (or ``None``
when no memory is found / retrieval is disabled).
"""

from __future__ import annotations

import logging
from typing import Optional

logger = logging.getLogger(__name__)


async def retrieve_memory_prompt(
    user_id: str,
    scope_id: str,
    query: str,
    *,
    mem_num: int = 20,
    summary_num: int = 5,
) -> Optional[str]:
    """Retrieve relevant memories and return a formatted prompt string.

    Args:
        user_id: User identifier (case-insensitive; lower-cased internally
            to match the index convention).
        scope_id: Memory scope identifier (the memory repo id).
        query: The user query to retrieve memories for.
        mem_num: Max number of semantic/episodic memories to retrieve.
        summary_num: Max number of history-summary memories to retrieve.

    Returns:
        The formatted memory-usage prompt string, or ``None`` if no
        memories were found, retrieval is disabled, or retrieval failed.
    """
    if not query or not user_id or not scope_id:
        return None
    try:
        from agent_runtime.memory.adapter.ltm_manager import get_ltm
        from jiuwen.context.memory_engine.prompt.memory_usage import (
            MEMORY_USAGE_PROMPT,
        )

        ltm = get_ltm()
        if ltm is None:
            logger.debug("LTM not initialized, skipping memory retrieval")
            return None

        uid = user_id.lower()
        memory_content = ""
        has_mem = False

        search_mems = await ltm.search_user_mem(
            query=query, num=mem_num, user_id=uid, scope_id=scope_id
        )
        for mem in search_mems or []:
            if mem is None:
                continue
            mem_content = (
                mem.mem_info.content
                if hasattr(mem, "mem_info")
                else mem.get("mem", "")
            )
            if mem_content:
                memory_content += f"<mem>{mem_content}</mem>\n"
                has_mem = True

        search_summary_mems = await ltm.search_user_history_summary(
            query=query, num=summary_num, user_id=uid, scope_id=scope_id
        )
        for mem in search_summary_mems or []:
            if mem is None:
                continue
            mem_content = (
                mem.mem_info.content
                if hasattr(mem, "mem_info")
                else mem.get("mem", "")
            )
            if mem_content:
                memory_content += f"<history_summary>{mem_content}</history_summary>\n"
                has_mem = True

        if not has_mem:
            logger.debug(
                "No memory found for user=%s, scope=%s, query=%s",
                user_id, scope_id, query[:50],
            )
            return None

        return MEMORY_USAGE_PROMPT.replace("MEMORY_CONTENT", memory_content)
    except Exception as e:
        logger.warning("Failed to retrieve memory: %s", e, exc_info=True)
        return None
