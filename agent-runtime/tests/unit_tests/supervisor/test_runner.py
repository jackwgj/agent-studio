import asyncio
from types import SimpleNamespace
from unittest.mock import Mock

import pytest

from agent_runtime.supervisor import runner as runner_module
from agent_runtime.supervisor.skill_context import (
    attach_agent_context,
    get_skill_context,
    reset_skill_context as real_reset_skill_context,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor


def descriptor(skill_id, version_id):
    return SkillDescriptor(
        skill_id=skill_id,
        version_id=version_id,
        name=f"name-{skill_id}",
        description=f"description-{skill_id}",
        object_key=f"user/skills/{skill_id}/{version_id}/{skill_id}.zip",
    )


class EmptyAgent:
    card = object()

    async def stream(self, _inputs, _session):
        if False:
            yield None


class FailingAgent:
    card = object()

    async def stream(self, _inputs, _session):
        raise RuntimeError("stream exploded")
        yield None


@pytest.mark.asyncio
@pytest.mark.parametrize("agent", [EmptyAgent(), FailingAgent()])
async def test_runner_resets_skill_context_after_normal_and_failing_stream(monkeypatch, agent):
    monkeypatch.setattr(runner_module, "create_agent_session", lambda **_kwargs: object())
    reset = Mock(wraps=real_reset_skill_context)
    monkeypatch.setattr(runner_module, "reset_skill_context", reset)
    attach_agent_context(agent, [descriptor("s1", "v1")], [], SimpleNamespace())

    events = [event async for event in runner_module.run_supervisor(agent, "q", "c1", "e1")]

    assert reset.call_count == 1
    assert get_skill_context() is None
    if isinstance(agent, FailingAgent):
        assert events[0]["event"] == "error"
    else:
        assert events[0]["event"] == "run_done"


@pytest.mark.asyncio
async def test_early_close_cancels_stream_task_before_context_reset_and_isolates_concurrent_nested_agent(monkeypatch):
    monkeypatch.setattr(runner_module, "create_agent_session", lambda **_kwargs: object())
    monkeypatch.setattr(
        runner_module,
        "adapt_stream_chunk",
        lambda _chunk, ctx: [{"event": "message", "data": {"skill": ctx.execution_id}}],
    )
    first_started = asyncio.Event()
    first_finished = asyncio.Event()
    release_first = asyncio.Event()
    observed = []

    class NestedAgent:
        async def stream(self):
            observed.append(("nested", get_skill_context().catalog_by_id["s1"].version_id))
            yield object()

    class BlockingAgent:
        card = object()

        async def stream(self, _inputs, _session):
            observed.append(("top", get_skill_context().catalog_by_id["s1"].version_id))
            async for chunk in NestedAgent().stream():
                yield chunk
            first_started.set()
            try:
                await release_first.wait()
            finally:
                first_finished.set()

    class CompleteAgent:
        card = object()

        async def stream(self, _inputs, _session):
            observed.append(("concurrent", get_skill_context().catalog_by_id["s1"].version_id))
            yield object()

    first_agent = BlockingAgent()
    second_agent = CompleteAgent()
    attach_agent_context(first_agent, [descriptor("s1", "v1")], [], SimpleNamespace())
    attach_agent_context(second_agent, [descriptor("s1", "v2")], [], SimpleNamespace())
    first_stream = runner_module.run_supervisor(first_agent, "q", "c1", "e1")

    first_event = await first_stream.__anext__()
    await first_started.wait()
    second_events = [event async for event in runner_module.run_supervisor(second_agent, "q", "c2", "e2")]
    await first_stream.aclose()

    assert first_event["event"] == "message"
    assert second_events[-1]["event"] == "run_done"
    assert first_finished.is_set()
    assert observed == [("top", "v1"), ("nested", "v1"), ("concurrent", "v2")]
    assert get_skill_context() is None
