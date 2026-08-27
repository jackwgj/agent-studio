import json
import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock

import pytest
from pydantic import ValidationError

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.output_artifact_publisher import (
    OutputArtifactPublishError,
    PublishedConversationArtifact,
)
from agent_runtime.serve.apis import conversation_team as conversation_team_module
from agent_runtime.serve.apis import conversation_team_app as conversation_team_app_module
from agent_runtime.serve.apis.conversation_team import ConversationTeamReq, team_sse_stream
from agent_runtime.supervisor import runner as runner_module
from agent_runtime.supervisor.event.channel import (
    EventChannel,
    get_channel,
    reset_channel,
    set_channel,
)
from agent_runtime.supervisor.skill_context import (
    attach_agent_context,
    bind_agent_skill_context,
    get_skill_context,
    reset_skill_context,
)
from agent_runtime.supervisor.skill_model import SkillDescriptor
from agent_runtime.supervisor.tool.activate_skill_tool import ActivateSkillTool


def manager_skill_catalog():
    return [{
        "skillId": "s1",
        "versionId": "v1",
        "name": "会议纪要",
        "description": "整理会议",
        "objectKey": "u/skills/s1/v1/a.zip",
    }]


def request_payload(**overrides):
    payload = {
        "conversationId": "c1",
        "projectId": "p1",
        "workspaceId": "w1",
        "userId": "u1",
        "query": "整理会议",
        "subAgentIds": ["a1"],
        "modelDeploymentId": "m1",
        "skillCatalog": manager_skill_catalog(),
        "recommendedSkillIds": ["s1"],
    }
    payload.update(overrides)
    return payload


def app_request():
    return ConversationTeamReq.model_validate(request_payload(
        selectType="APP",
        appId="app-1",
        subAgentIds=[],
        modelDeploymentId=None,
        skillCatalog=[],
        recommendedSkillIds=[],
    ))


class BlockingAppRunner:
    def __init__(self, cleanup_order):
        self.cleanup_order = cleanup_order
        self.started = asyncio.Event()
        self.closed = asyncio.Event()
        self.contexts = []
        self.close_contexts = []

    async def run_streaming(self, _request, _execution_id):
        self.contexts.append(
            execution_context_module.get_conversation_execution_context()
        )
        self.started.set()
        try:
            yield {"event": "message", "data": {"answer": "app-first"}}
            await asyncio.Event().wait()
        finally:
            self.close_contexts.append(
                execution_context_module.get_conversation_execution_context()
            )
            self.cleanup_order.append("raw_close")
            self.closed.set()


def configure_blocking_app(monkeypatch, cleanup_order):
    runner = BlockingAppRunner(cleanup_order)

    async def load_ir(_path):
        return {"configs": {"mode": "ReAct"}}

    monkeypatch.setattr(conversation_team_app_module, "async_ir_load", load_ir)
    monkeypatch.setattr(
        conversation_team_app_module,
        "prepare_params",
        lambda request: request.params,
    )
    monkeypatch.setattr(
        conversation_team_app_module,
        "_conversation_runner_factory",
        SimpleNamespace(get=lambda _mode: runner),
    )
    return runner


def test_request_requires_trusted_execution_identity():
    req = ConversationTeamReq.model_validate(request_payload())

    assert req.project_id == "p1"
    assert req.workspace_id == "w1"
    assert req.user_id == "u1"

    for field_name in ("projectId", "workspaceId", "userId"):
        missing = request_payload()
        missing.pop(field_name)
        with pytest.raises(ValidationError):
            ConversationTeamReq.model_validate(missing)

        with pytest.raises(ValidationError):
            ConversationTeamReq.model_validate(request_payload(**{field_name: "  "}))


@pytest.mark.parametrize("field_name", ["userId", "conversationId"])
@pytest.mark.parametrize("value", ["contains/slash", "中文标识", "x" * 65, ".", ".."])
def test_request_rejects_non_path_safe_visible_identity(field_name, value):
    with pytest.raises(ValidationError, match="path-safe platform identifier"):
        ConversationTeamReq.model_validate(request_payload(**{field_name: value}))


def test_request_accepts_only_durable_input_artifact_references():
    req = ConversationTeamReq.model_validate(request_payload(fileIds=[{
        "objectKey": "conversation-inputs/project/workspace/user/00000000-0000-0000-0000-000000000001/report.pdf",
        "fileName": "report.pdf",
        "size": 4,
        "checksum": "3a6eb0790f39ac87c94f3856b2dd2c5d110e6811602261a9a923d3bb23adc8b7",
    }]))

    assert req.file_ids[0].object_key == "conversation-inputs/project/workspace/user/00000000-0000-0000-0000-000000000001/report.pdf"
    assert req.file_ids[0].file_name == "report.pdf"
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(fileIds=[{
            "url": "https://files.test/report.pdf", "fileName": "report.pdf",
        }]))


@pytest.mark.asyncio
async def test_team_stream_prepares_inputs_before_building_agent_and_only_passes_sandbox_paths(monkeypatch):
    prepared_path = "/workspace/project/workspace/user/conversation/input/1234-report.pdf"
    prepare = AsyncMock(return_value=[prepared_path])
    captured = {}

    async def build_config(**kwargs):
        captured.update(kwargs)
        return SimpleNamespace()

    async def run_in_context(*_args):
        yield {"event": "message", "data": {"delta": "ok"}}

    monkeypatch.setattr(conversation_team_module, "prepare_conversation_inputs", prepare)
    monkeypatch.setattr(conversation_team_module, "build_conversation_supervisor_config", build_config)
    monkeypatch.setattr(conversation_team_module, "run_conversation_supervisor", run_in_context)
    req = ConversationTeamReq.model_validate(request_payload(fileIds=[{
        "objectKey": "conversation-inputs/project/workspace/user/00000000-0000-0000-0000-000000000001/report.pdf",
        "fileName": "report.pdf",
        "size": 4,
        "checksum": "3a6eb0790f39ac87c94f3856b2dd2c5d110e6811602261a9a923d3bb23adc8b7",
    }]))

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(req, "input-execution")]

    prepare.assert_awaited_once_with(req.file_ids)
    assert captured["file_references"] == [{"fileName": "report.pdf", "path": prepared_path}]
    assert "url" not in captured["file_references"][0]
    assert events[-1]["event"] == "message"


@pytest.mark.asyncio
async def test_team_stream_returns_standard_error_when_input_preparation_fails(monkeypatch):
    async def fail_prepare(_artifacts):
        raise RuntimeError("input preparation rejected")

    monkeypatch.setattr(conversation_team_module, "prepare_conversation_inputs", fail_prepare)
    req = ConversationTeamReq.model_validate(request_payload(fileIds=[{
        "objectKey": "conversation-inputs/project/workspace/user/00000000-0000-0000-0000-000000000001/report.pdf",
        "fileName": "report.pdf",
        "size": 4,
        "checksum": "3a6eb0790f39ac87c94f3856b2dd2c5d110e6811602261a9a923d3bb23adc8b7",
    }]))

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(req, "input-execution")]

    assert len(events) == 1
    assert events[0]["event"] == "error"
    assert events[0]["executionId"] == "input-execution"
    assert events[0]["index"] == 0
    assert "input preparation rejected" in events[0]["data"]["message"]


@pytest.mark.asyncio
async def test_team_stream_emits_artifact_after_upload_and_before_terminal_run_done(monkeypatch):
    async def build_config(**_kwargs):
        return SimpleNamespace()

    async def completed_runner(*_args):
        yield {"event": "message", "data": {"delta": "done"}}
        yield {"event": "run_done", "data": {"text": "done"}}

    publish = AsyncMock(return_value=[PublishedConversationArtifact(
        object_key="conversation-artifacts/trusted/report.pdf",
        file_name="report.pdf",
        size=4,
        media_type="application/pdf",
        checksum="0" * 64,
        execution_id="artifact-execution",
    )])
    monkeypatch.setattr(
        conversation_team_module, "build_conversation_supervisor_config", build_config
    )
    monkeypatch.setattr(
        conversation_team_module, "run_conversation_supervisor", completed_runner
    )
    monkeypatch.setattr(
        conversation_team_module, "publish_conversation_outputs", publish
    )

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "artifact-execution"
    )]

    assert [event["event"] for event in events[-3:]] == [
        "message", "artifact", "run_done"
    ]
    assert events[-2]["data"] == {
        "objectKey": "conversation-artifacts/trusted/report.pdf",
        "fileName": "report.pdf",
        "size": 4,
        "mediaType": "application/pdf",
        "checksum": "0" * 64,
    }
    publish.assert_awaited_once_with({})


@pytest.mark.asyncio
async def test_team_stream_does_not_emit_artifact_or_run_done_when_upload_fails(monkeypatch):
    async def build_config(**_kwargs):
        return SimpleNamespace()

    async def completed_runner(*_args):
        yield {"event": "run_done", "data": {"text": "done"}}

    monkeypatch.setattr(
        conversation_team_module, "build_conversation_supervisor_config", build_config
    )
    monkeypatch.setattr(
        conversation_team_module, "run_conversation_supervisor", completed_runner
    )
    monkeypatch.setattr(
        conversation_team_module,
        "publish_conversation_outputs",
        AsyncMock(side_effect=OutputArtifactPublishError("minio unavailable")),
    )

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "artifact-failure"
    )]

    assert events[-1]["event"] == "error"
    assert "minio unavailable" in events[-1]["data"]["message"]
    assert all(event["event"] not in {"artifact", "run_done"} for event in events)


@pytest.mark.asyncio
@pytest.mark.parametrize("terminal", [False, True])
async def test_runner_error_skips_output_publication_and_retains_failure_cleanup(monkeypatch, terminal):
    """An emitted error must not become a second output error or successful run_done."""
    closed = []

    async def failed_runner(*_args):
        try:
            yield {"event": "error", "data": {"code": "103104", "message": "sandbox registration failed"}}
            if terminal:
                yield {"event": "run_done", "data": {"text": ""}}
        finally:
            closed.append(True)

    publish = AsyncMock(side_effect=RuntimeError("unexpected output publication"))
    cleanup = AsyncMock()
    monkeypatch.setattr(conversation_team_module, "build_conversation_supervisor_config", AsyncMock(return_value=SimpleNamespace()))
    monkeypatch.setattr(conversation_team_module, "run_conversation_supervisor", failed_runner)
    monkeypatch.setattr(conversation_team_module, "publish_conversation_outputs", publish)
    monkeypatch.setattr(conversation_team_module, "cleanup_execution_directories", cleanup)
    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "registration-failure"
    )]
    assert [e["event"] for e in events] == ["user_message", "run_start", "error"]
    assert events[-1]["data"]["message"] == "sandbox registration failed"
    assert closed == [True]
    publish.assert_not_awaited()
    cleanup.assert_awaited_once()
    assert cleanup.await_args.kwargs["remove_output"] is False
    with pytest.raises(LookupError):
        execution_context_module.get_conversation_execution_context()


def test_request_accepts_manager_skill_contract():
    req = ConversationTeamReq.model_validate(request_payload())

    assert req.skill_catalog[0].skill_id == "s1"
    assert req.skill_catalog[0].version_id == "v1"
    assert req.skill_catalog[0].object_key == "u/skills/s1/v1/a.zip"
    assert req.recommended_skill_ids == ["s1"]


@pytest.mark.asyncio
async def test_workspace_initialization_precedes_inputs_and_agent(monkeypatch):
    order = []

    async def initialize():
        assert execution_context_module.get_conversation_execution_context().identity.execution_id == "init-test"
        order.append("ensure")

    async def prepare(_files):
        order.append("inputs")
        return []

    async def baseline():
        order.append("baseline")
        return {"old.txt": "a" * 64}

    async def runner(*_args):
        order.append("agent")
        yield {"event": "run_done", "data": {"text": "ok"}}

    monkeypatch.setattr(conversation_team_module, "ensure_conversation_workspace", initialize, raising=False)
    monkeypatch.setattr(conversation_team_module, "prepare_conversation_inputs", prepare)
    monkeypatch.setattr(conversation_team_module, "capture_conversation_output_baseline", baseline, raising=False)
    monkeypatch.setattr(conversation_team_module, "build_conversation_supervisor_config", AsyncMock(return_value=SimpleNamespace()))
    monkeypatch.setattr(conversation_team_module, "run_conversation_supervisor", runner)
    publish = AsyncMock(return_value=[])
    monkeypatch.setattr(conversation_team_module, "publish_conversation_outputs", publish)
    monkeypatch.setattr(conversation_team_module, "cleanup_execution_directories", AsyncMock())
    events = [line async for line in team_sse_stream(ConversationTeamReq.model_validate(request_payload()), "init-test")]
    assert order == ["ensure", "inputs", "baseline", "agent"]
    publish.assert_awaited_once_with({"old.txt": "a" * 64})
    assert 'run_done' in events[-1]


@pytest.mark.asyncio
async def test_workspace_initialization_failure_stops_before_input_and_model(monkeypatch):
    initialize = AsyncMock(side_effect=RuntimeError("workspace initialization failed: permission denied"))
    prepare = AsyncMock()
    build = AsyncMock()
    monkeypatch.setattr(conversation_team_module, "ensure_conversation_workspace", initialize, raising=False)
    monkeypatch.setattr(conversation_team_module, "prepare_conversation_inputs", prepare)
    monkeypatch.setattr(conversation_team_module, "build_conversation_supervisor_config", build)
    monkeypatch.setattr(conversation_team_module, "cleanup_execution_directories", AsyncMock())
    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "init-failure"
    )]
    assert len(events) == 1
    assert events[0]["event"] == "error"
    assert "workspace initialization failed" in events[0]["data"]["message"]
    prepare.assert_not_awaited()
    build.assert_not_awaited()


@pytest.mark.parametrize("field_name", ["skillId", "versionId", "name", "description", "objectKey"])
def test_request_rejects_blank_skill_descriptor_fields(field_name):
    catalog = manager_skill_catalog()
    catalog[0][field_name] = " \t "

    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=catalog))


def test_request_rejects_duplicate_skill_ids_and_invalid_object_key():
    duplicate = manager_skill_catalog() + [{
        "skillId": "s1",
        "versionId": "v2",
        "name": "另一个版本",
        "description": "冲突",
        "objectKey": "u/skills/s1/v2/a.zip",
    }]

    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=duplicate))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=[{
            **manager_skill_catalog()[0], "objectKey": "../outside.zip"
        }]))


def test_request_normalizes_duplicate_recommendations_and_explicitly_supports_aliases_and_field_names():
    req = ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["s1", "s1"]))
    snake_case = ConversationTeamReq.model_validate({
        "conversation_id": "c1",
        "project_id": "p1",
        "workspace_id": "w1",
        "user_id": "u1",
        "query": "整理会议",
        "sub_agent_ids": ["a1"],
        "model_deployment_id": "m1",
        "skill_catalog": [{
            "skill_id": "s1",
            "version_id": "v1",
            "name": "会议纪要",
            "description": "整理会议",
            "object_key": "u/skills/s1/v1/a.zip",
        }],
        "recommended_skill_ids": ["s1"],
    })

    assert req.recommended_skill_ids == ["s1"]
    assert snake_case.skill_catalog[0].skill_id == "s1"
    assert snake_case.recommended_skill_ids == ["s1"]


def test_request_rejects_null_and_unknown_recommendation_and_ignores_legacy_top_level_extra():
    for field_name in ("skillCatalog", "recommendedSkillIds"):
        with pytest.raises(ValidationError):
            ConversationTeamReq.model_validate(request_payload(**{field_name: None}))
    legacy = ConversationTeamReq.model_validate(request_payload(
        systemPrompt="旧客户端字段", untrustedBrowserMetadata={"x": 1}
    ))
    assert legacy.query == "整理会议"
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["other"]))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=[" "]))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(
            skill_catalog=[{
                "skill_id": "s2",
                "version_id": "v2",
                "name": "field-name",
                "description": "conflicts with the alias",
                "object_key": "u/skills/s2/v2/a.zip",
            }]
        ))


def test_request_rejects_conflicting_alias_and_field_name_but_keeps_descriptor_extra_forbidden():
    with pytest.raises(ValidationError, match="conflicting request field aliases"):
        ConversationTeamReq.model_validate(request_payload(
            skill_catalog=[{
                "skill_id": "s2",
                "version_id": "v2",
                "name": "field-name",
                "description": "conflicts with the alias",
                "object_key": "u/skills/s2/v2/a.zip",
            }]
        ))
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(skillCatalog=[{
            **manager_skill_catalog()[0], "unexpected": "still forbidden inside descriptors"
        }]))


@pytest.mark.asyncio
async def test_team_stream_converts_manager_catalog_to_runtime_descriptors(monkeypatch):
    build_supervisor = AsyncMock(return_value=SimpleNamespace())

    async def run_supervisor(*_args):
        yield {"event": "message", "data": {"delta": "已整理"}, "executionId": "e1"}

    monkeypatch.setenv("CONVERSATION_TEAM_USE_LEGACY_SUPERVISOR", "true")
    monkeypatch.setattr("agent_runtime.serve.apis.conversation_team.build_supervisor", build_supervisor)
    monkeypatch.setattr("agent_runtime.serve.apis.conversation_team.run_supervisor", run_supervisor)

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "e1"
    )]

    catalog = build_supervisor.await_args.kwargs["skill_catalog"]
    assert catalog == [SkillDescriptor(
        skill_id="s1",
        version_id="v1",
        name="会议纪要",
        description="整理会议",
        object_key="u/skills/s1/v1/a.zip",
    )]
    assert build_supervisor.await_args.kwargs["recommended_skill_ids"] == ["s1"]
    assert events[-1]["event"] == "message"


@pytest.mark.asyncio
async def test_team_stream_rejects_recommended_skill_outside_manager_catalog(monkeypatch):
    with pytest.raises(ValidationError):
        ConversationTeamReq.model_validate(request_payload(recommendedSkillIds=["other"]))


@pytest.mark.asyncio
async def test_team_stream_binds_before_first_event_and_resets_when_closed_immediately(monkeypatch):
    monkeypatch.setenv("CONVERSATION_SANDBOX_WORKSPACE_ROOT", "/ignored-by-stream")
    monkeypatch.setattr(
        conversation_team_module,
        "settings",
        SimpleNamespace(
            security_sandbox=SimpleNamespace(workspace_root="/sandbox/conversations/")
        ),
        raising=False,
    )
    stream = team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()),
        "execution-first",
    )

    first = json.loads((await stream.__anext__()).removeprefix("data: "))
    active = execution_context_module.get_conversation_execution_context()

    assert first["event"] == "user_message"
    assert active.identity.conversation_id == "c1"
    assert active.identity.execution_id == "execution-first"
    assert str(active.workspace.sandbox_root) == "/sandbox/conversations"

    await stream.aclose()
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()


@pytest.mark.asyncio
async def test_team_stream_keeps_context_through_runner_close_and_resets_after_normal_end(monkeypatch):
    observed = []
    closed = []

    async def build_config(**_kwargs):
        return SimpleNamespace()

    async def run_in_context(*_args):
        observed.append(execution_context_module.get_conversation_execution_context())
        try:
            yield {"event": "message", "data": {"delta": "ok"}}
        finally:
            closed.append(execution_context_module.get_conversation_execution_context())

    monkeypatch.delenv("CONVERSATION_SANDBOX_WORKSPACE_ROOT", raising=False)
    monkeypatch.delenv("CONVERSATION_TEAM_USE_LEGACY_SUPERVISOR", raising=False)
    monkeypatch.setattr(conversation_team_module, "build_conversation_supervisor_config", build_config)
    monkeypatch.setattr(conversation_team_module, "run_conversation_supervisor", run_in_context)

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "execution-normal"
    )]

    assert events[-1]["event"] == "message"
    assert observed == closed
    assert observed[0].identity.execution_id == "execution-normal"
    assert str(observed[0].workspace.sandbox_root) == "/workspace"
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()


@pytest.mark.asyncio
async def test_team_stream_resets_context_after_runner_exception(monkeypatch):
    observed = []

    async def build_config(**_kwargs):
        return SimpleNamespace()

    async def failing_runner(*_args):
        observed.append(execution_context_module.get_conversation_execution_context())
        raise RuntimeError("runner exploded")
        yield

    monkeypatch.delenv("CONVERSATION_TEAM_USE_LEGACY_SUPERVISOR", raising=False)
    monkeypatch.setattr(conversation_team_module, "build_conversation_supervisor_config", build_config)
    monkeypatch.setattr(conversation_team_module, "run_conversation_supervisor", failing_runner)

    events = [json.loads(line.removeprefix("data: ")) async for line in team_sse_stream(
        ConversationTeamReq.model_validate(request_payload()), "execution-error"
    )]

    assert observed[0].identity.execution_id == "execution-error"
    assert events[-1]["event"] == "error"
    assert events[-1]["data"]["message"] == "runner exploded"
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()


@pytest.mark.asyncio
async def test_concurrent_team_streams_observe_only_their_own_context(monkeypatch):
    contexts = {}
    both_started = asyncio.Event()

    async def build_config(**_kwargs):
        return SimpleNamespace()

    async def isolated_runner(req, *_args):
        contexts[req.conversation_id] = execution_context_module.get_conversation_execution_context()
        if len(contexts) == 2:
            both_started.set()
        await asyncio.wait_for(both_started.wait(), timeout=1)
        yield {"event": "message", "data": {"delta": req.conversation_id}}

    async def consume(payload, execution_id):
        req = ConversationTeamReq.model_validate(request_payload(**payload))
        return [line async for line in team_sse_stream(req, execution_id)]

    monkeypatch.setenv("CONVERSATION_SANDBOX_WORKSPACE_ROOT", "/shared/root")
    monkeypatch.delenv("CONVERSATION_TEAM_USE_LEGACY_SUPERVISOR", raising=False)
    monkeypatch.setattr(conversation_team_module, "build_conversation_supervisor_config", build_config)
    monkeypatch.setattr(conversation_team_module, "run_conversation_supervisor", isolated_runner)

    await asyncio.gather(
        consume({
            "conversationId": "conversation-a", "projectId": "project-a",
            "workspaceId": "workspace-a", "userId": "user-a",
        }, "execution-a"),
        consume({
            "conversationId": "conversation-b", "projectId": "project-b",
            "workspaceId": "workspace-b", "userId": "user-b",
        }, "execution-b"),
    )

    context_a = contexts["conversation-a"]
    context_b = contexts["conversation-b"]
    assert context_a.identity.project_id == "project-a"
    assert context_a.identity.execution_id == "execution-a"
    assert context_b.identity.project_id == "project-b"
    assert context_b.identity.execution_id == "execution-b"
    assert context_a.workspace.conversation_root != context_b.workspace.conversation_root
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()


@pytest.mark.asyncio
async def test_app_first_event_close_closes_raw_runner_before_context_reset(monkeypatch):
    cleanup_order = []
    runner = configure_blocking_app(monkeypatch, cleanup_order)
    original_reset = conversation_team_module.reset_conversation_execution_context

    def reset_context(token):
        cleanup_order.append("context_reset")
        original_reset(token)

    monkeypatch.setattr(
        conversation_team_module,
        "reset_conversation_execution_context",
        reset_context,
    )
    stream = team_sse_stream(app_request(), "app-close-execution")

    await stream.__anext__()
    await stream.__anext__()
    app_event = json.loads((await stream.__anext__()).removeprefix("data: "))
    await stream.aclose()

    assert app_event["event"] == "message"
    assert runner.started.is_set()
    assert runner.closed.is_set()
    assert runner.contexts == runner.close_contexts
    assert cleanup_order == ["raw_close", "context_reset"]
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()


@pytest.mark.asyncio
async def test_app_send_cancellation_closes_raw_runner_before_context_reset_in_consumer(monkeypatch):
    cleanup_order = []
    runner = configure_blocking_app(monkeypatch, cleanup_order)
    original_reset = conversation_team_module.reset_conversation_execution_context

    def reset_context(token):
        cleanup_order.append("context_reset")
        original_reset(token)

    monkeypatch.setattr(
        conversation_team_module,
        "reset_conversation_execution_context",
        reset_context,
    )
    response = await conversation_team_module.conversation_team(
        app_request(), SimpleNamespace(headers={"x-execution-id": "app-cancel-execution"})
    )
    app_body_started = asyncio.Event()
    never = asyncio.Event()
    body_count = 0

    async def send(message):
        nonlocal body_count
        if message["type"] != "http.response.body" or not message.get("more_body"):
            return
        body_count += 1
        if body_count == 3:
            app_body_started.set()
            await never.wait()

    response_task = asyncio.create_task(response.stream_response(send))
    await asyncio.wait_for(app_body_started.wait(), timeout=1)
    response_task.cancel()
    with pytest.raises(asyncio.CancelledError):
        await response_task

    assert runner.started.is_set()
    assert runner.closed.is_set()
    assert runner.contexts == runner.close_contexts
    assert cleanup_order == ["raw_close", "context_reset"]


@pytest.mark.asyncio
async def test_concurrent_skill_context_and_event_channel_are_isolated():
    def agent_with_catalog(skill_id):
        agent = SimpleNamespace()
        cache = AsyncMock()
        cache.load_instructions.return_value = f"instructions-{skill_id}"
        attach_agent_context(
            agent,
            [SkillDescriptor(
                skill_id=skill_id,
                version_id="v1",
                name=f"name-{skill_id}",
                description=f"description-{skill_id}",
                object_key=f"user/skills/{skill_id}/v1/{skill_id}.zip",
            )],
            [],
            cache,
        )
        return agent

    async def invoke_in_context(agent, skill_id, execution_id):
        skill_token = bind_agent_skill_context(agent)
        channel = EventChannel(execution_id)
        event_token = set_channel(channel)
        try:
            result = await ActivateSkillTool().invoke({"skill_id": skill_id})
            event = await channel.get()
            return result, event
        finally:
            reset_channel(event_token)
            reset_skill_context(skill_token)

    (result_a, event_a), (result_b, event_b) = await asyncio.gather(
        invoke_in_context(agent_with_catalog("s1"), "s1", "exec-1"),
        invoke_in_context(agent_with_catalog("s2"), "s2", "exec-2"),
    )

    assert result_a["skillId"] == event_a["data"]["skillId"] == "s1"
    assert result_b["skillId"] == event_b["data"]["skillId"] == "s2"
    assert event_a["executionId"] == "exec-1"
    assert event_b["executionId"] == "exec-2"
    assert get_channel() is None
    assert get_skill_context() is None


@pytest.mark.asyncio
async def test_outer_sse_close_waits_for_runner_cleanup_in_the_consuming_context(monkeypatch):
    class BlockingAgent:
        card = object()

        async def stream(self, _inputs, _session):
            observed_context.append(
                execution_context_module.get_conversation_execution_context()
            )
            yield object()
            stream_started.set()
            try:
                await release.wait()
            finally:
                closed_context.append(
                    execution_context_module.get_conversation_execution_context()
                )
                child_finished.set()

    stream_started = asyncio.Event()
    child_finished = asyncio.Event()
    release = asyncio.Event()
    observed_context = []
    closed_context = []
    agent = BlockingAgent()
    attach_agent_context(agent, [SkillDescriptor(
        skill_id="s1", version_id="v1", name="会议纪要", description="整理会议",
        object_key="u/skills/s1/v1/a.zip",
    )], [], SimpleNamespace())
    monkeypatch.setenv("CONVERSATION_TEAM_USE_LEGACY_SUPERVISOR", "true")
    monkeypatch.setattr(conversation_team_module, "build_supervisor", AsyncMock(return_value=agent))
    monkeypatch.setattr(runner_module, "create_agent_session", lambda **_kwargs: object())
    monkeypatch.setattr(
        runner_module,
        "adapt_stream_chunk",
        lambda _chunk, _ctx: [{"event": "message", "data": {"delta": "x"}}],
    )
    stream = team_sse_stream(ConversationTeamReq.model_validate(request_payload()), "e1")

    await stream.__anext__()
    await stream.__anext__()
    event = await stream.__anext__()
    await stream_started.wait()
    await stream.aclose()

    assert json.loads(event.removeprefix("data: "))["event"] == "message"
    assert child_finished.is_set()
    assert observed_context == closed_context
    assert observed_context[0].identity.execution_id == "e1"
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()
    assert get_channel() is None
    assert get_skill_context() is None


@pytest.mark.asyncio
@pytest.mark.parametrize("disconnect", ["cancel", "error"])
async def test_streaming_response_disconnect_closes_body_in_its_consuming_context(monkeypatch, disconnect):
    class BlockingAgent:
        card = object()

        async def stream(self, _inputs, _session):
            observed_context.append(
                execution_context_module.get_conversation_execution_context()
            )
            child_started.set()
            yield object()
            try:
                await release.wait()
            finally:
                closed_context.append(
                    execution_context_module.get_conversation_execution_context()
                )
                child_finished.set()

    child_started = asyncio.Event()
    child_finished = asyncio.Event()
    release = asyncio.Event()
    observed_context = []
    closed_context = []
    agent = BlockingAgent()
    attach_agent_context(agent, [SkillDescriptor(
        skill_id="s1", version_id="v1", name="会议纪要", description="整理会议",
        object_key="u/skills/s1/v1/a.zip",
    )], [], SimpleNamespace())
    monkeypatch.setenv("CONVERSATION_TEAM_USE_LEGACY_SUPERVISOR", "true")
    monkeypatch.setattr(conversation_team_module, "build_supervisor", AsyncMock(return_value=agent))
    monkeypatch.setattr(runner_module, "create_agent_session", lambda **_kwargs: object())
    monkeypatch.setattr(
        runner_module,
        "adapt_stream_chunk",
        lambda _chunk, _ctx: [{"event": "message", "data": {"delta": "x"}}],
    )
    reset_order = []
    reset_errors = []
    original_reset_channel = runner_module.reset_channel
    original_reset_skill = runner_module.reset_skill_context

    def reset_channel(token):
        try:
            original_reset_channel(token)
        except ValueError as error:
            reset_errors.append(error)
            raise
        reset_order.append("channel")

    def reset_skill(token):
        try:
            original_reset_skill(token)
        except ValueError as error:
            reset_errors.append(error)
            raise
        reset_order.append("skill")

    monkeypatch.setattr(runner_module, "reset_channel", reset_channel)
    monkeypatch.setattr(runner_module, "reset_skill_context", reset_skill)
    response = await conversation_team_module.conversation_team(
        ConversationTeamReq.model_validate(request_payload()), SimpleNamespace(headers={})
    )
    runner_entered_send = asyncio.Event()
    never = asyncio.Event()
    body_count = 0

    async def send(message):
        nonlocal body_count
        if message["type"] != "http.response.body" or not message.get("more_body"):
            return
        body_count += 1
        if body_count == 3:
            runner_entered_send.set()
            if disconnect == "error":
                raise OSError("client disconnected")
            await never.wait()

    if disconnect == "cancel":
        response_task = asyncio.create_task(response.stream_response(send))
        await runner_entered_send.wait()
        response_task.cancel()
        with pytest.raises(asyncio.CancelledError):
            await response_task
    else:
        with pytest.raises(OSError, match="client disconnected"):
            await response.stream_response(send)

    assert child_started.is_set()
    assert child_finished.is_set()
    assert observed_context == closed_context
    with pytest.raises(LookupError, match="no conversation execution context is active"):
        execution_context_module.get_conversation_execution_context()
    assert reset_order == ["channel", "skill"]
    assert reset_errors == []
    assert get_channel() is None
    assert get_skill_context() is None
