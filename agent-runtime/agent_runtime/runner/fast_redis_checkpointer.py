# -*- coding: UTF-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2026. All rights reserved.
"""
FastRedisCheckpointer — decorator that replaces O(N) scan_iter operations
in RedisCheckpointer with O(1)/O(K) alternatives.

Eliminates two full-keyspace Redis scans per workflow request:
1. session_exists() → sentinel key EXISTS (O(1) instead of scan_iter)
2. post_workflow_execute() → precise key deletion (O(K) instead of scan_iter)
"""

from typing import Optional

from openjiuwen.core.common.logging import workflow_logger
from openjiuwen.core.session.checkpointer import Checkpointer
from openjiuwen.core.session.checkpointer.base import (
    WORKFLOW_NAMESPACE_GRAPH,
    build_key_with_namespace,
)
from redis.asyncio import Redis


# GraphStore key suffixes — mirrors GraphStore._DATA_TYPE / _DATA_VALUE
# in openjiuwen/extensions/checkpointer/redis/storage.py
_GRAPH_DATA_TYPE = "checkpoint_data_type"
_GRAPH_DATA_VALUE = "checkpoint_data_value"

# Sentinel key prefix — isolated namespace, no collision with agent-core keys
_SENTINEL_PREFIX = "agentBuilder:session_exists:"


def _sentinel_key(session_id: str) -> str:
    return f"{_SENTINEL_PREFIX}{session_id}"


class FastRedisCheckpointer(Checkpointer):
    """Decorator around RedisCheckpointer that replaces scan_iter with O(1)/O(K) operations.

    Overrides:
      - session_exists: O(1) sentinel key check
      - pre_workflow_execute: delegate + write sentinel key
      - post_workflow_execute: precise GraphStore key delete + sentinel delete

    All other methods delegate to the original RedisCheckpointer unchanged.
    """

    def __init__(
        self,
        delegate: Checkpointer,
        redis_client: Redis,
        ttl_seconds: int = 86400,  # 24h — sentinel key 过期时间，兜底清理
    ):
        self._delegate = delegate
        self._redis = redis_client
        self._ttl_seconds = ttl_seconds

    # ── Overridden methods ──────────────────────────────────────

    async def session_exists(self, session_id: str) -> bool:
        """O(1) sentinel key check — replaces O(N) scan_iter in delegate.

        Returns False if Redis is unreachable (safe default: treat as new session).
        This is a deliberate tradeoff — returning True would be more conservative
        but would require falling back to the delegate's scan_iter, which is the
        exact operation we're optimizing away.
        """
        try:
            exists = await self._redis.exists(_sentinel_key(session_id))
            return bool(exists)
        except Exception as e:
            workflow_logger.warning(
                f"FastRedisCheckpointer: sentinel EXISTS failed for session "
                f"{session_id}, returning False: {e}"
            )
            return False

    async def pre_workflow_execute(self, session, inputs):
        """Delegate + write sentinel key unconditionally (idempotent SET).

        Sentinel is written AFTER delegate succeeds. If delegate raises (e.g.,
        CHECKPOINTER_PRE_WORKFLOW_EXECUTION_ERROR), the sentinel is NOT written,
        which is correct — the session never started, so session_exists() should
        return False.
        """
        await self._delegate.pre_workflow_execute(session, inputs)
        session_id = session.session_id()
        try:
            await self._redis.set(
                _sentinel_key(session_id), "1", ex=self._ttl_seconds
            )
        except Exception as e:
            workflow_logger.warning(
                f"FastRedisCheckpointer: sentinel SET failed for session "
                f"{session_id}: {e}"
            )

    async def post_workflow_execute(self, session, result, exception):
        """Replace GraphStore.delete() scan_iter with precise key deletion.

        On normal completion:
          1. Construct exact GraphStore keys and batch_delete them (O(2))
          2. Delete sentinel key
          3. Delegate workflow_storage.clear()
        On WorkflowAbortException (异常结束节点):
          The workflow terminated via an exception node — this is a normal
          completion path, not an interrupt. Clear checkpoint so the next
          run starts fresh instead of erroneously resuming.
        On other exception/interrupt: delegate unchanged (saves checkpoint).
        """
        session_id = session.session_id()
        workflow_id = session.workflow_id()

        if exception is not None:
            from jiuwen.extension.workflow_node.utils import WorkflowAbortException
            if isinstance(exception, WorkflowAbortException):
                # 异常结束节点终止 → 视为正常完成，清除 checkpoint
                workflow_logger.info(
                    f"FastRedisCheckpointer: WorkflowAbortException detected, "
                    f"clearing checkpoint for session {session_id}, "
                    f"workflow {workflow_id}"
                )
                # 不调用 delegate.post_workflow_execute（它会保存 checkpoint 并 re-raise），
                # 而是直接执行清除逻辑（与正常完成路径一致）
                await self._clear_checkpoint_and_sentinel(session_id, workflow_id, session)
                return

            # Delegate saves checkpoint and re-raises the exception.
            # The re-raise propagates through our await to the caller automatically.
            await self._delegate.post_workflow_execute(session, result, exception)
            return

        from openjiuwen.core.graph.pregel import TASK_STATUS_INTERRUPT

        if result.get(TASK_STATUS_INTERRUPT) is None:
            # Normal completion — clear checkpoint
            await self._clear_checkpoint_and_sentinel(session_id, workflow_id, session)
        else:
            # Interrupt — delegate saves checkpoint
            await self._delegate.post_workflow_execute(session, result, exception)

    # ── Internal helpers ────────────────────────────────────────

    async def _clear_checkpoint_and_sentinel(
        self, session_id: str, workflow_id: str, session
    ):
        """Clear GraphStore keys, sentinel key, and workflow storage.

        Used for both normal completion and WorkflowAbortException (异常结束节点),
        which should both result in a clean slate for the next run.
        """
        # Precise GraphStore key deletion instead of scan_iter
        try:
            key_type = build_key_with_namespace(
                session_id, WORKFLOW_NAMESPACE_GRAPH, workflow_id, _GRAPH_DATA_TYPE
            )
            key_value = build_key_with_namespace(
                session_id, WORKFLOW_NAMESPACE_GRAPH, workflow_id, _GRAPH_DATA_VALUE
            )
            await self._redis.delete(key_type, key_value)
            workflow_logger.info(
                f"FastRedisCheckpointer: precise GraphStore delete for "
                f"session {session_id}, workflow {workflow_id}"
            )
        except Exception as e:
            workflow_logger.warning(
                f"FastRedisCheckpointer: precise GraphStore delete failed, "
                f"falling back to delegate: {e}"
            )
            # Fallback: let delegate do the scan_iter cleanup (slow but correct)
            await self._delegate.post_workflow_execute(session, {}, None)
            return

        # Delete sentinel key
        try:
            await self._redis.delete(_sentinel_key(session_id))
        except Exception as e:
            workflow_logger.warning(
                f"FastRedisCheckpointer: sentinel DELETE failed for session "
                f"{session_id}: {e}"
            )

        # Delegate workflow_storage.clear() (this is O(K), not scan_iter)
        # NOTE: Uses getattr to avoid protected-access lint warning.
        # The delegate (RedisCheckpointer) has a _workflow_storage attribute
        # with a clear() method. If it's not accessible or fails, fall back
        # to the full delegate post_workflow_execute (includes scan_iter).
        workflow_storage = getattr(self._delegate, "_workflow_storage", None)
        if workflow_storage is not None and hasattr(workflow_storage, "clear"):
            try:
                await workflow_storage.clear(workflow_id, session_id)
            except Exception as e:
                workflow_logger.warning(
                    f"FastRedisCheckpointer: workflow_storage.clear() failed, "
                    f"falling back to full delegate post_workflow_execute for session "
                    f"{session_id}, workflow {workflow_id}: {e}"
                )
                await self._delegate.post_workflow_execute(session, {}, None)
        else:
            workflow_logger.warning(
                f"FastRedisCheckpointer: workflow_storage not accessible on delegate, "
                f"falling back to full delegate post_workflow_execute for session "
                f"{session_id}, workflow {workflow_id}"
            )
            await self._delegate.post_workflow_execute(session, {}, None)

    # ── Delegate pass-through methods ───────────────────────────

    async def pre_agent_execute(self, session, inputs):
        await self._delegate.pre_agent_execute(session, inputs)

    async def pre_agent_team_execute(self, session, inputs):
        await self._delegate.pre_agent_team_execute(session, inputs)

    async def interrupt_agent_execute(self, session):
        await self._delegate.interrupt_agent_execute(session)

    async def post_agent_execute(self, session):
        await self._delegate.post_agent_execute(session)

    async def post_agent_team_execute(self, session):
        await self._delegate.post_agent_team_execute(session)

    async def release(self, session_id: str, agent_id: Optional[str] = None):
        """Delegate release. Sentinel key is NOT deleted — it will expire via TTL.

        Intentionally keeping the sentinel key after release avoids accidental
        deletion that could cause loss of interrupt state if release is called
        prematurely. The sentinel TTL (default 24h, configurable via
        FAST_CHECKPOINTER_SENTINEL_TTL_SECONDS) ensures eventual cleanup.
        """
        await self._delegate.release(session_id, agent_id)

    def graph_store(self):
        return self._delegate.graph_store()
