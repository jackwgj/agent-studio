# coding: utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025. All rights reserved.
"""LazyWorkflow — deferred-build shell around openjiuwen Workflow.

Background
----------
``IRConverter.build_openjiuwen_workflow_from_ir`` eagerly builds the entire
workflow graph (all nodes, connections, branch finalization) at registration
time. When the IR contains many SubWorkflow nodes, every child workflow is
recursively built at parent-build time, even though only a subset of branches
will actually execute per request. This wastes build cost (1 + M builds for M
children, vs the optimal 1 + K where K is the executed-branch count).

Design
------
``LazyWorkflow`` subclasses ``Workflow`` but only initializes a lightweight
``WorkflowCard`` in its constructor — no nodes/connections are built. The full
graph is materialized on first ``invoke``/``stream`` by calling
``IRConverter.build_openjiuwen_workflow_from_ir`` and caching the result in
``self._workflow``. Subsequent calls reuse the cached executable instance.

The shell transparently delegates ``invoke``/``stream`` (and any ``is_sub`` /
``reset_sub_outputs`` / ``skip_inputs_validate`` kwargs) to the cached
``_workflow``. Because ``SubWorkflow.__init__`` stores the ``sub_workflow``
argument directly in ``self._workflow_instance`` and
``_get_workflow_instance`` returns it unchanged, injecting a ``LazyWorkflow``
as the ``sub_workflow`` argument makes child-workflow construction lazy without
any change to ``SubWorkflow``.

Concurrency
-----------
``instantiate()`` uses an ``asyncio.Lock`` with double-checked locking so
concurrent first-time invocations only build once. Since
``build_openjiuwen_workflow_from_ir`` is now pure-functional (the previous
class-level ``_STREAM_SOURCE_IDS`` was removed by commit 216a5323), multiple
``LazyWorkflow`` instances can safely build in parallel.
"""

from __future__ import annotations

import asyncio
from typing import Any, AsyncIterator, Optional

from openjiuwen.core.graph.executable import Input, Output
from openjiuwen.core.workflow import Workflow, WorkflowCard


class LazyWorkflow(Workflow):
    """Workflow shell that defers IR -> graph construction to first use.

    Construction is essentially free: only the ``WorkflowCard`` is created.
    The full graph (nodes, connections, branch finalization) is built lazily
    inside :meth:`instantiate`, which is triggered automatically by the first
    ``invoke``/``stream`` call.

    Attributes:
        _ir_data: IR dict captured at construction time.
        _ir_path: Optional IR file path (used by child workflows loaded via
            ``_try_load_sub_workflow`` for diagnostics).
        _build_kwargs: Extra kwargs forwarded to
            ``IRConverter.build_openjiuwen_workflow_from_ir`` (e.g. parent
            context).
        _workflow: Cached executable Workflow, ``None`` until first
            instantiation.
        _instantiate_lock: Serializes concurrent first-time instantiations.
    """

    def __init__(
        self,
        ir_data: dict,
        *,
        ir_path: str = "",
        build_kwargs: Optional[dict] = None,
    ) -> None:
        # Capture IR + build kwargs for deferred instantiation.
        self._ir_data = ir_data
        self._ir_path = ir_path
        self._build_kwargs = dict(build_kwargs) if build_kwargs else {}
        self._workflow: Optional[Workflow] = None
        self._instantiate_lock = asyncio.Lock()

        # Lightweight card init only — no nodes/connections are built here.
        card = WorkflowCard(
            id=ir_data.get("workflowId") or ir_data.get("agentId") or "",
            name=(
                ir_data.get("workflowName")
                or ir_data.get("workflowId")
                or ir_data.get("agentName")
                or ir_data.get("agentId")
                or ""
            ),
            description=ir_data.get("description") or "",
        )
        super().__init__(card=card)

    async def instantiate(self) -> Workflow:
        """Idempotently materialize the underlying executable Workflow.

        First call builds the graph via ``build_openjiuwen_workflow_from_ir``
        and caches it. Subsequent calls return the cached instance directly.
        Concurrent first-time calls are serialized by ``_instantiate_lock``
        with double-checked locking, so only one build happens.

        Returns:
            The cached executable Workflow instance.
        """
        if self._workflow is not None:
            return self._workflow
        async with self._instantiate_lock:
            if self._workflow is not None:
                return self._workflow
            # Deferred import to avoid circular dependency: ir_converter.py
            # imports lazy_workflow at module load time.
            from jiuwen.serve.controllers.execution.ir_converter import IRConverter

            self._workflow = await IRConverter.build_openjiuwen_workflow_from_ir(
                self._ir_data, **self._build_kwargs
            )
        return self._workflow

    async def invoke(
        self,
        inputs: Input,
        session: Any,
        context: Any = None,
        **kwargs: Any,
    ) -> Output:
        """Delegate to the cached workflow's ``invoke``.

        All kwargs (including ``is_sub``, ``reset_sub_outputs``,
        ``skip_inputs_validate``, ``CONFIG_KEY``) are forwarded verbatim so
        that callers (notably ``SubWorkflow`` which passes ``is_sub=True``)
        behave identically to a directly-built Workflow.
        """
        wf = await self.instantiate()
        return await wf.invoke(inputs, session, context=context, **kwargs)

    async def stream(
        self,
        inputs: Input,
        session: Any,
        context: Any = None,
        stream_modes: Any = None,
        **kwargs: Any,
    ) -> AsyncIterator[Any]:
        """Delegate to the cached workflow's ``stream``.

        ``stream_modes`` is kept as an explicit parameter (matching
        ``Workflow.stream``'s signature) and defaulted to ``None`` so missing
        values from callers (e.g. SubWorkflow does not pass ``stream_modes``)
        behave identically to calling ``Workflow.stream`` directly.
        """
        wf = await self.instantiate()
        async for chunk in wf.stream(
            inputs,
            session,
            context=context,
            stream_modes=stream_modes,
            **kwargs,
        ):
            yield chunk
