import base64
from dataclasses import replace
import hashlib
import hmac
import json
import time
from types import SimpleNamespace
from unittest.mock import AsyncMock, MagicMock
from uuid import uuid4

import httpx
import pytest
import pytest_asyncio

from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
    reset_conversation_execution_context,
    set_conversation_execution_context,
)
from agent_runtime.conversation.image_reference import (
    PrivateImageError,
    current_image_catalog,
    open_image_catalog,
    verify_image_grant,
)
from agent_runtime.conversation import private_vision
from jiuwen.extension.workflow_node.llm_chain import LLMChain, USER_FIELDS


TOKEN = "unit-test-key"
WORKFLOW = "vision-workflow"
IDENTITY = ConversationIdentity("project", "workspace", "owner", "conversation", "execution")
APP = "vision-app"
IMAGE = b"\x89PNG\r\n\x1a\nunit-test-image-payload"
ENCODED = base64.b64encode(IMAGE).decode()
URL = "http://minio.example.test/private/object?X-Amz-Signature=test-only-secret"
BINDING = {"id": WORKFLOW, "ir_path": f"workflow/ir/{WORKFLOW}/{WORKFLOW}_1.json", "arguments": [
    {"name": "query", "type": "string"}, {"name": "image_source", "type": "string"},
]}
WORKFLOW_IR = {"workflowId": WORKFLOW, "configs": {"conversation_image_attachment": True},
               "components": [{"type": "jiuwen.LLMComponent", "configs": {
                   "model": {"extension": {"vl_enable": True}}}}]}


def build_grant(*, current=True, **overrides):
    name = "截图.png"
    scope = [IDENTITY.project_id, IDENTITY.workspace_id, IDENTITY.user_id]
    key = "/".join(["conversation-inputs", *(hashlib.sha256(v.encode()).hexdigest() for v in scope),
                    str(uuid4()), name])
    payload = {"projectId": scope[0], "workspaceId": scope[1], "userId": scope[2],
               "conversationId": IDENTITY.conversation_id, "executionId": IDENTITY.execution_id,
               "appId": APP, "expires": int(time.time()) + 100, "agentReleaseId": "1", "omittedCurrent": 0,
               "images": [{"objectKey": key, "fileName": name, "size": len(IMAGE),
                           "checksum": hashlib.sha256(IMAGE).hexdigest(), "mediaType": "image/png",
                           "current": current, "ordinal": 1}]}
    payload.update(overrides)
    data = base64.urlsafe_b64encode(json.dumps(payload).encode()).rstrip(b"=")
    signature = hmac.digest(TOKEN.encode(), b"conversation-image-grant:v2:" + data, "sha256")
    return data.decode() + "." + base64.urlsafe_b64encode(signature).rstrip(b"=").decode()


async def catalog(grant=None, binding=BINDING, workflow_ir=WORKFLOW_IR):
    verified = verify_image_grant(grant or build_grant(), TOKEN, IDENTITY, APP)
    return await open_image_catalog(verified, IDENTITY, APP,
        {"agentId": APP, "configs": {"workflows": [binding]}}, AsyncMock(return_value=workflow_ir))


@pytest.mark.parametrize("field,value", [
    ("projectId", "other"), ("workspaceId", "other"), ("userId", "other"),
    ("conversationId", "other"), ("executionId", "other"), ("appId", "other"), ("expires", 0),
])
def test_grant_cannot_be_used_outside_its_scope(field, value):
    with pytest.raises(PrivateImageError, match="授权无效"):
        verify_image_grant(build_grant(**{field: value}), TOKEN, IDENTITY, APP)


@pytest.mark.asyncio
async def test_valid_grant_requires_a_versioned_binding_and_emits_only_opaque_metadata():
    grant = build_grant()
    first, second = await catalog(grant), await catalog(grant)
    assert set(first.references).isdisjoint(second.references)
    assert next(iter(first.references.values())) == next(iter(second.references.values()))
    prompt = first.prompt()
    assert "截图.png" in prompt and next(iter(first.references)) in prompt
    assert "conversation-inputs" not in prompt and "http" not in prompt
    assert await catalog(grant, binding={**BINDING, "id": "other"}) is None
    assert await catalog(grant, binding={}) is None
    assert await catalog(grant, binding={**BINDING, "ir_path": f"workflow/ir/{WORKFLOW}/{WORKFLOW}.json"}) is None
    assert await catalog(grant, workflow_ir={**WORKFLOW_IR, "configs": {}}) is None
    assert await catalog(grant, workflow_ir={**WORKFLOW_IR, "components": []}) is None
    signed = grant.rsplit(".", 1)[0] + ".AAAA"
    with pytest.raises(PrivateImageError, match="授权无效"):
        verify_image_grant(signed, TOKEN, IDENTITY, APP)


@pytest.mark.asyncio
async def test_two_opted_in_bindings_do_not_select_a_private_reader():
    second = {**BINDING, "ir_path": f"workflow/ir/{WORKFLOW}/{WORKFLOW}_2.json"}
    grant = verify_image_grant(build_grant(), TOKEN, IDENTITY, APP)
    assert await open_image_catalog(grant, IDENTITY, APP,
        {"agentId": APP, "configs": {"workflows": [BINDING, second]}}, AsyncMock(return_value=WORKFLOW_IR)) is None


@pytest.mark.asyncio
@pytest.mark.parametrize("mode", ["ReAct", "PlanExecute", "Controller"])
async def test_only_react_app_receives_image_catalog(mode, monkeypatch):
    from agent_runtime.serve.apis import conversation_team_app

    ordinary_ir = {"agentId": APP, "configs": {"mode": mode}}
    release_ir = {"agentId": APP, "configs": {"mode": "ReAct", "workflows": [BINDING]}}
    loader = AsyncMock(side_effect=[ordinary_ir, release_ir, WORKFLOW_IR])
    monkeypatch.setattr(conversation_team_app, "async_ir_load", loader)
    monkeypatch.setattr(conversation_team_app, "prepare_params", lambda request: request.params)
    monkeypatch.setattr(conversation_team_app, "settings", SimpleNamespace(security_sandbox=SimpleNamespace(
        image_internal_token=TOKEN)))
    seen = []

    async def run(request, execution_id):
        seen.append(current_image_catalog.get())
        seen.append(request.params.global_variables["conversationInputFiles"])
        seen.append(request.params.ir_cache)
        for frame in ():
            yield frame

    monkeypatch.setattr(conversation_team_app, "_conversation_runner_factory", {
        mode: SimpleNamespace(run_streaming=run)})
    execution = set_conversation_execution_context(ConversationExecutionContext.create(IDENTITY, "/workspace"))
    try:
        request = SimpleNamespace(app_id=APP, image_grant=build_grant(current=False), query="之前的图", conversation_history=[])
        assert [event async for event in conversation_team_app.stream_application(request, IDENTITY.execution_id)] == []
    finally:
        reset_conversation_execution_context(execution)

    assert (seen[0] is not None) == (mode == "ReAct")
    assert seen[1] == []  # A history-only image goes through the catalog, not AIO input staging.
    if mode == "ReAct":
        assert seen[2] is release_ir and not seen[0].current_refs
        assert loader.await_args_list[1].args == (f"agent/ir/{APP}/{APP}_1.json",)
    else:
        assert seen[2] is None and loader.await_count == 1
    assert current_image_catalog.get() is None


@pytest_asyncio.fixture
async def selected_catalog():
    image_catalog = await catalog()
    return image_catalog


@pytest.fixture
def image_scope(selected_catalog):
    execution = set_conversation_execution_context(ConversationExecutionContext.create(IDENTITY, "/workspace"))
    marker = current_image_catalog.set(selected_catalog)
    try:
        yield
    finally:
        current_image_catalog.reset(marker)
        reset_conversation_execution_context(execution)


@pytest.mark.asyncio
async def test_native_wrapper_rejects_invented_cross_conversation_and_no_scope_before_build(selected_catalog, image_scope):
    image = next(iter(selected_catalog.references.values()))
    relative = hashlib.sha256(image.object_key.encode()).hexdigest()[:16]
    paths = selected_catalog.sandbox_input_paths()
    assert len(paths) == 1 and next(iter(paths)).endswith(f"/input/{relative}/{image.file_name}")
    workflow = private_vision.ConversationImageWorkflow({"workflowId": WORKFLOW})
    workflow.instantiate = AsyncMock()
    with pytest.raises(PrivateImageError, match="清单"):
        await workflow.invoke({"image_source": "conversation-image:invented"}, None)
    other = set_conversation_execution_context(ConversationExecutionContext.create(
        replace(IDENTITY, conversation_id="other"), "/workspace"))
    try:
        with pytest.raises(PrivateImageError, match="不属于当前会话"):
            await workflow.invoke({"image_source": next(iter(selected_catalog.references))}, None)
    finally:
        reset_conversation_execution_context(other)
    workflow.instantiate.assert_not_awaited()
    with pytest.raises(PrivateImageError, match="范围"):
        await private_vision.resolve_private_image(next(iter(selected_catalog.references)))
    assert await private_vision.resolve_private_image("https://example.test/standalone.png") is None


@pytest.mark.asyncio
async def test_vision_is_a_lazy_native_ability_guided_by_system_prompt(selected_catalog, image_scope, monkeypatch):
    from agent_runtime.conversation.runner.conversation_react_runner import ConversationReActRunner
    from jiuwen.serve.controllers.execution.ir_converter import IRConverter
    from openjiuwen.core.runner import Runner
    from openjiuwen.core.workflow import WorkflowCard

    runner = ConversationReActRunner()
    resources = MagicMock()
    sign = AsyncMock()
    monkeypatch.setattr(Runner, "resource_mgr", resources)
    monkeypatch.setattr("storage.object_storage.get_storage_provider", lambda: SimpleNamespace(get_presigned_url=sign))
    monkeypatch.setattr("agent_runtime.runner.react_agent_runner.async_ir_load", AsyncMock(return_value=WORKFLOW_IR))
    monkeypatch.setattr(IRConverter, "async_ir_to_workflow", AsyncMock(return_value=SimpleNamespace(
        card=WorkflowCard(id=WORKFLOW, name="ordinary_vision"))))
    ir = {"configs": {"workflows": [BINDING]}}
    agent = MagicMock()
    await runner._register_workflows(ir, agent, APP)
    card = resources.add_workflow.call_args.kwargs["card"]
    provider = resources.add_workflow.call_args.kwargs["workflow"]
    agent.ability_manager.add.assert_called_once_with(card)
    assert isinstance(card, WorkflowCard) and card.name == selected_catalog.tool_name
    assert "优先用这个工具" in card.description

    messages = runner._parse_prompt_template(ir, global_variables={"conversationTeam": {"type": "APP"}})
    system_prompt = messages[0]["content"]
    assert card.name in system_prompt and "先看看前文能不能回答" in system_prompt
    assert selected_catalog.current_refs[0] not in system_prompt
    assert selected_catalog.current_refs[0] in selected_catalog.prompt()
    workflow = await provider()
    assert isinstance(workflow, private_vision.ConversationImageWorkflow)
    assert workflow._workflow is None
    sign.assert_not_awaited()


@pytest.mark.asyncio
async def test_private_workflow_cannot_claim_success_without_reading_image(selected_catalog, image_scope):
    workflow = private_vision.ConversationImageWorkflow({"workflowId": WORKFLOW})
    workflow.instantiate = AsyncMock(return_value=SimpleNamespace(invoke=AsyncMock(return_value={"answer": "not read"})))
    with pytest.raises(PrivateImageError, match="未读取"):
        await workflow.invoke({"image_source": selected_catalog.current_refs[0]}, None)


@pytest.mark.asyncio
@pytest.mark.parametrize("fetch_status,fetch_body,model_failure", [
    (200, IMAGE, False), (200, IMAGE, True), (403, b"denied", False), (307, b"redirect", False),
    (200, IMAGE + b"oversize", False), (200, IMAGE[:-1] + b"!", False),
])
async def test_native_vision_node_keeps_workflow_state_and_observations_safe(
    selected_catalog, image_scope, monkeypatch, caplog, fetch_status, fetch_body, model_failure,
):
    from openjiuwen.core.common.logging import llm_logger

    ref = next(iter(selected_catalog.references))
    sign = AsyncMock(return_value=URL)
    monkeypatch.setattr("storage.object_storage.get_storage_provider", lambda: SimpleNamespace(get_presigned_url=sign))
    http_calls = []

    def respond(request):
        http_calls.append(request)
        assert request.method == "GET" and str(request.url) == URL
        return httpx.Response(fetch_status, content=fetch_body, headers={"Location": "http://other.test/"})

    client_type = httpx.AsyncClient
    monkeypatch.setattr(private_vision.httpx, "AsyncClient", lambda **kw: client_type(
        transport=httpx.MockTransport(respond), **kw))
    conf = {"model": {"extension": {"vl_enable": True}}, "responseFormat": {"type": "text"}}
    node = LLMChain(conf)
    node._initialize_if_needed = AsyncMock()
    node._process_inputs = lambda _: None
    node._get_model_input = lambda inputs, *, vision_inputs=None: node._get_vision(
        [{"role": "user", "content": inputs["query"]}], vision_inputs)
    observed, stream_frames = [], []
    data_uri = f"data:image/png;base64,{ENCODED}"

    async def model(**kwargs):
        assert kwargs["messages"][0]["content"][1]["image_url"]["url"] == data_uri
        llm_logger.error(f"must not log {URL} or {ENCODED}")
        if model_failure:
            raise RuntimeError(f"provider echoed {URL} and {data_uri}")
        return SimpleNamespace(content=f"识别成功 {URL} {ENCODED}", reasoning_content=data_uri)

    node._llm = SimpleNamespace(invoke=AsyncMock(side_effect=model))
    node_inputs = {USER_FIELDS: {"query": "看图", "_image_vision_1": [ref]}}

    async def run_graph(inputs, session, **kwargs):
        observed.append(repr(inputs))  # Mirrors the graph's input/checkpoint boundary.
        if fetch_status == 200 and fetch_body == IMAGE and not model_failure:
            session = SimpleNamespace(get_component_id=lambda: "vision", write_custom_stream=AsyncMock())
            stream_frames.extend([part async for part in node.stream(node_inputs, session, None)])
            stream_frames.extend(session.write_custom_stream.await_args_list)
            output = node.get_stream_output()
        else:
            output = await node.invoke(node_inputs, None, None)
        observed.append(repr(output))
        return output

    workflow = private_vision.ConversationImageWorkflow({"workflowId": WORKFLOW})
    workflow.instantiate = AsyncMock(return_value=SimpleNamespace(invoke=run_graph))
    inputs = {"query": "看图", "image_source": ref}
    if fetch_status == 200 and fetch_body == IMAGE and not model_failure:
        assert "识别成功" in repr(await workflow.invoke(inputs, None))
    else:
        with pytest.raises(Exception) as error:
            await workflow.invoke(inputs, None)
        observed.append(str(error.value))
        assert ("图片理解暂时失败" if model_failure else "私有图片读取失败") in str(error.value)
        if not model_failure:
            node._llm.invoke.assert_not_awaited()
    assert node_inputs[USER_FIELDS]["_image_vision_1"] == [ref]
    assert inputs["image_source"] == ref
    sign.assert_awaited_once_with(next(iter(selected_catalog.references.values())).object_key, 120)
    assert len(http_calls) == 1
    observation = "\n".join(observed) + repr(stream_frames) + caplog.text
    assert "test-only-secret" not in observation and ENCODED not in observation
    assert not private_vision.is_private_image_call()
