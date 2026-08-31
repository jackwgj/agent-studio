"""Supervisor-only remote sandbox tool injection tests."""

from __future__ import annotations

import asyncio
import sys
from types import SimpleNamespace
from unittest.mock import AsyncMock, MagicMock

import pytest

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
)
from agent_runtime.conversation.sandbox import (
    ConversationSandboxConfig,
    ConversationSandboxMode,
    ConversationSandboxToolBinder,
    ConversationSysOperationFactory,
    CONVERSATION_SYS_OPERATION_ID,
)
from agent_runtime.conversation.runner import conversation_react_runner
from agent_runtime.conversation.runner.conversation_react_runner import ConversationReActRunner
from openjiuwen.core.sys_operation import OperationMode
from openjiuwen.core.runner import Runner
from openjiuwen.core.runner.resources_manager.resource_manager import ResourceMgr
from agent_runtime.conversation.sandbox.registration import SHARED_OPERATION_ID


class _Result:
    def __init__(self, *, added: bool = True, error: Exception | None = None):
        self.added = added
        self._error = error

    def is_ok(self) -> bool:
        return self._error is None

    def error(self) -> Exception | None:
        return self._error


class _AbilityManager:
    def __init__(self):
        self.cards = []

    def add(self, card):
        self.cards.append(card)
        return _Result()


class _Agent:
    def __init__(self):
        self.ability_manager = _AbilityManager()


class _RemoteOperation:
    def __init__(self):
        self.calls = []
        self.command_error: Exception | None = None

    def fs(self):
        return self

    def code(self):
        return self

    def shell(self):
        return self

    async def read_file(self, path, **kwargs):
        self.calls.append(("read_file", path, kwargs))
        return {"path": path}

    async def execute_code(self, code, **kwargs):
        self.calls.append(("execute_code", code, kwargs))
        return {"code": code, "cwd": kwargs["cwd"]}

    async def execute_cmd(self, command, **kwargs):
        if self.command_error is not None:
            raise self.command_error
        self.calls.append(("execute_cmd", command, kwargs))
        return {"command": command, "cwd": kwargs["cwd"]}


class _ResourceManager:
    def __init__(self, operation: _RemoteOperation):
        self.operation = operation
        self.sys_operation_cards = []
        self.operations = {}
        self.tools = {}
        self.added_tools = {}
        self.removed_tools = []
        self.removed_operations = []

    def add_sys_operation(self, card, *, tag):
        self.sys_operation_cards.append((card, tag))
        self.operations[(card.id, tag)] = self.operation
        return _Result()

    def get_sys_operation(self, operation_id, *, tag):
        return self.operations.get((operation_id, tag))

    def add_tool(self, tool, *, tag):
        self.tools[tool.card.id] = (tool, tag)
        self.added_tools[tool.card.id] = tool
        return _Result()

    def remove_tool(self, tool_id, *, tag):
        self.removed_tools.append((tool_id, tag))
        self.tools.pop(tool_id, None)
        return _Result()

    def remove_sys_operation(self, operation_id, *, tag):
        self.removed_operations.append((operation_id, tag))
        self.operations.pop((operation_id, tag), None)
        return _Result()


class _Factory:
    def __init__(self, card):
        self.card = card
        self.create_count = 0

    def create(self):
        self.create_count += 1
        return self.card


def _context(execution_id: str = "execution-a") -> ConversationExecutionContext:
    return ConversationExecutionContext.create(
        ConversationIdentity(
            project_id="project-a",
            workspace_id="workspace-a",
            user_id="user-a",
            conversation_id="conversation-a",
            execution_id=execution_id,
        ),
        "/workspace",
    )


def _sandbox_card():
    return ConversationSysOperationFactory(
        ConversationSandboxConfig(
            mode=ConversationSandboxMode.SANDBOX,
            server="https://sandbox.example",
            ssl_verify=True,
            sandbox_type="aio",
            idle_ttl_seconds=600,
            timeout_seconds=300,
            scope="system",
        )
    ).create()


def _workspace_environment(context: ConversationExecutionContext) -> dict[str, str]:
    return {
        "CONVERSATION_ROOT": str(context.workspace.conversation_root),
        "CONVERSATION_INPUT_DIR": str(context.workspace.input_dir),
        "CONVERSATION_SKILLS_DIR": str(context.workspace.skills_dir),
        "CONVERSATION_WORK_DIR": str(context.workspace.work_dir),
        "CONVERSATION_OUTPUT_DIR": str(context.workspace.output_dir),
        "CONVERSATION_TMP_DIR": str(context.workspace.tmp_dir),
    }


def test_conversation_prompt_describes_the_workspace_directory_protocol():
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        prompt = ConversationReActRunner()._parse_prompt_template(
            {
                "configs": {
                    "sysPromptTemplate": "你是对话工作台顶层 Agent。",
                    "skills": {},
                }
            }
        )[0]["content"]

        assert "## 当前会话沙箱目录协议" in prompt
        assert f"`{context.workspace.conversation_root}`" in prompt
        assert f"`{context.workspace.input_dir}`：用户上传的原始输入文件" in prompt
        assert f"`{context.workspace.skills_dir}`：已激活 Skill 的完整制品和配套资源" in prompt
        assert f"`{context.workspace.work_dir}`：默认工作目录" in prompt
        assert f"`{context.workspace.output_dir}`：正式成果目录" in prompt
        assert f"`{context.workspace.tmp_dir}`：临时目录" in prompt
        assert "只有写入 output 目录的文件才会作为正式成果被采集和发布" in prompt
        assert "不得访问或写入当前会话根目录之外" in prompt
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_configured_sandbox_binds_exactly_three_remote_tools_and_anchors_paths():
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        remote = _RemoteOperation()
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        agent = _Agent()

        binder.register(agent)

        assert [card.name for card in agent.ability_manager.cards] == [
            "read_file",
            "execute_code",
            "execute_cmd",
        ]
        registered_card, tag = manager.sys_operation_cards[0]
        assert registered_card.mode is OperationMode.SANDBOX
        assert registered_card.id != CONVERSATION_SYS_OPERATION_ID
        assert tag == registered_card.id
        assert all(card.id.startswith(f"{binder.operation_id}.") for card in agent.ability_manager.cards)

        tools = {tool.card.name: tool for tool, _tag in manager.tools.values()}
        await tools["read_file"].invoke({"path": "notes/../plan.md"})
        await tools["read_file"].invoke({"path": str(context.workspace.input_dir / "source.md")})
        await tools["execute_code"].invoke({"code": "print(1)", "cwd": "scratch"})
        await tools["execute_cmd"].invoke({"command": "pwd"})

        assert remote.calls[0] == ("read_file", f"{context.workspace.work_dir}/plan.md", {})
        assert remote.calls[1] == ("read_file", str(context.workspace.input_dir / "source.md"), {})
        assert remote.calls[2][0] == "execute_code"
        assert "os.chdir" in remote.calls[2][1]
        assert remote.calls[2][2] == {
            "cwd": f"{context.workspace.work_dir}/scratch",
            "environment": _workspace_environment(context),
        }
        assert remote.calls[3] == (
            "execute_cmd",
            "pwd",
            {
                "cwd": str(context.workspace.work_dir),
                "environment": _workspace_environment(context),
            },
        )
    finally:
        execution_context_module.reset_conversation_execution_context(token)


def test_no_factory_card_attaches_no_conversation_sandbox_tools():
    manager = _ResourceManager(_RemoteOperation())
    binder = ConversationSandboxToolBinder(_Factory(None), manager)
    agent = _Agent()

    binder.register(agent)

    assert agent.ability_manager.cards == []
    assert manager.sys_operation_cards == []
    assert manager.tools == {}


@pytest.mark.asyncio
async def test_remote_error_is_raised_unchanged_without_a_local_fallback():
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        remote = _RemoteOperation()
        remote.command_error = TimeoutError("remote sandbox timed out")
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        binder.register(_Agent())
        command_tool = next(
            tool for tool, _tag in manager.tools.values() if tool.card.name == "execute_cmd"
        )

        with pytest.raises(TimeoutError, match="remote sandbox timed out") as error:
            await command_tool.invoke({"command": "sleep 1"})

        assert error.value is remote.command_error
        assert remote.calls == []
        assert manager.sys_operation_cards[0][0].mode is OperationMode.SANDBOX
    finally:
        execution_context_module.reset_conversation_execution_context(token)


def test_real_resource_manager_registers_only_request_owned_sandbox_resources():
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    manager = ResourceMgr()
    binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
    try:
        agent = _Agent()
        binder.register(agent)

        operation = manager.get_sys_operation(
            SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID
        )
        assert operation is not None
        assert operation.mode is OperationMode.SANDBOX
        assert [card.name for card in agent.ability_manager.cards] == [
            "read_file",
            "execute_code",
            "execute_cmd",
        ]
    finally:
        binder.cleanup()
        execution_context_module.reset_conversation_execution_context(token)

    assert manager.get_sys_operation(
        SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID
    ) is operation
    assert manager.get_tool(
        f"{binder.operation_id}.read_file", tag=binder.operation_id
    ) is None


@pytest.mark.asyncio
async def test_sandbox_tools_reject_paths_outside_the_active_conversation_root():
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        remote = _RemoteOperation()
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        binder.register(_Agent())
        tools = {tool.card.name: tool for tool, _tag in manager.tools.values()}

        with pytest.raises(ValueError, match="conversation workspace"):
            await tools["read_file"].invoke({"path": "../../other-conversation.txt"})
        with pytest.raises(ValueError, match="conversation workspace"):
            await tools["execute_cmd"].invoke({"command": "pwd", "cwd": "/workspace/foreign/work"})
        await tools["read_file"].invoke({"path": str(context.output_dir / "answer.txt")})

        assert remote.calls == [
            ("read_file", str(context.output_dir / "answer.txt"), {}),
        ]
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
@pytest.mark.parametrize(
    "command",
    [
        "mkdir -p /tmp/output",
        "printf result > '/home/gem/result.txt'",
        "cd / && touch result.txt",
        "cd ../../../../tmp && touch result.txt",
    ],
)
async def test_command_tool_rejects_literal_paths_outside_the_conversation_workspace(command):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        remote = _RemoteOperation()
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        binder.register(_Agent())
        command_tool = next(
            tool for tool, _tag in manager.tools.values() if tool.card.name == "execute_cmd"
        )

        with pytest.raises(ValueError, match="outside the active conversation workspace"):
            await command_tool.invoke({"command": command})

        assert remote.calls == []
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_command_tool_allows_paths_that_normalize_inside_the_conversation_workspace():
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        remote = _RemoteOperation()
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        binder.register(_Agent())
        command_tool = next(
            tool for tool, _tag in manager.tools.values() if tool.card.name == "execute_cmd"
        )

        await command_tool.invoke(
            {
                "command": "cp report.md ../output/report.md",
                "environment": {"CONVERSATION_ROOT": "/untrusted", "CUSTOM_FLAG": "on"},
            }
        )

        assert remote.calls == [
            (
                "execute_cmd",
                "cp report.md ../output/report.md",
                {
                    "cwd": str(context.workspace.work_dir),
                    "environment": {
                        "CUSTOM_FLAG": "on",
                        **_workspace_environment(context),
                    },
                },
            )
        ]
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
@pytest.mark.parametrize(
    ("language", "source", "expected_chdir"),
    [
        ("python", "from __future__ import annotations\nprint('ok')", "os.chdir"),
        ("javascript", "'use strict'; console.log('ok')", "process.chdir"),
    ],
)
async def test_code_tool_embeds_the_validated_cwd_when_the_official_provider_ignores_cwd(
    language, source, expected_chdir
):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        remote = _RemoteOperation()
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        binder.register(_Agent())
        code_tool = next(
            tool for tool, _tag in manager.tools.values() if tool.card.name == "execute_code"
        )

        await code_tool.invoke({"code": source, "language": language})

        operation, transmitted_code, kwargs = remote.calls[-1]
        assert operation == "execute_code"
        assert transmitted_code != source
        assert source not in transmitted_code
        assert expected_chdir in transmitted_code
        assert str(context.workspace.work_dir) in transmitted_code
        assert kwargs["cwd"] == str(context.workspace.work_dir)
    finally:
        execution_context_module.reset_conversation_execution_context(token)


def test_cleanup_is_idempotent_and_removes_request_scoped_resources():
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        manager = _ResourceManager(_RemoteOperation())
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        binder.register(_Agent())

        binder.cleanup()
        binder.cleanup()

        assert len(manager.removed_tools) == 3
        assert manager.removed_operations == []
        assert manager.get_sys_operation(SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID) is manager.operation
    finally:
        execution_context_module.reset_conversation_execution_context(token)


def test_parent_and_handoff_child_bindings_cleanup_only_their_own_resources():
    parent_context = _context()
    child_context = parent_context.for_child_call()
    manager = _ResourceManager(_RemoteOperation())
    token = execution_context_module.set_conversation_execution_context(parent_context)
    try:
        parent_binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        parent_binder.register(_Agent())

        child_token = execution_context_module.set_conversation_execution_context(child_context)
        try:
            child_binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
            child_binder.register(_Agent())
        finally:
            execution_context_module.reset_conversation_execution_context(child_token)

        assert child_context.workspace.conversation_root == parent_context.workspace.conversation_root
        assert child_context.workspace.work_dir == parent_context.workspace.work_dir
        assert child_binder.operation_id != parent_binder.operation_id

        child_binder.cleanup()

        assert parent_binder.operation_id not in [
            operation_id for operation_id, _tag in manager.removed_operations
        ]
        assert all(
            tool_id in manager.tools
            for tool_id in (
                f"{parent_binder.operation_id}.read_file",
                f"{parent_binder.operation_id}.execute_code",
                f"{parent_binder.operation_id}.execute_cmd",
            )
        )

        parent_binder.cleanup()

        assert manager.removed_operations == []
        assert manager.tools == {}
        assert len(manager.sys_operation_cards) == 1
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_repeated_bindings_keep_each_conversation_work_directory_isolated():
    first_context = _context("execution-a")
    second_context = ConversationExecutionContext.create(
        ConversationIdentity(
            project_id="project-b",
            workspace_id="workspace-b",
            user_id="user-b",
            conversation_id="conversation-b",
            execution_id="execution-b",
        ),
        "/workspace",
    )
    first_manager = _ResourceManager(_RemoteOperation())
    second_manager = _ResourceManager(_RemoteOperation())

    first_token = execution_context_module.set_conversation_execution_context(first_context)
    try:
        first_binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), first_manager)
        first_binder.register(_Agent())
    finally:
        execution_context_module.reset_conversation_execution_context(first_token)
    second_token = execution_context_module.set_conversation_execution_context(second_context)
    try:
        second_binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), second_manager)
        second_binder.register(_Agent())
        first_tool = next(tool for tool, _tag in first_manager.tools.values() if tool.card.name == "execute_code")
        second_tool = next(tool for tool, _tag in second_manager.tools.values() if tool.card.name == "execute_code")
        await first_tool.invoke({"code": "print('first')"})
        await second_tool.invoke({"code": "print('second')"})

        assert first_manager.operation.calls[-1][2]["cwd"] == str(first_context.workspace.work_dir)
        assert second_manager.operation.calls[-1][2]["cwd"] == str(second_context.workspace.work_dir)
        assert first_binder.operation_id != second_binder.operation_id
    finally:
        execution_context_module.reset_conversation_execution_context(second_token)


class _Session:
    async def pre_run(self, **_kwargs):
        return None

    async def post_run(self):
        return None


class _StreamingAgent(_Agent):
    card = SimpleNamespace(id="supervisor-card")

    def set_llm(self, _llm):
        return None

    async def register_rail(self, _rail):
        return None

    async def stream(self, *_args):
        if False:
            yield None


class _RunnerBinder:
    def __init__(self):
        self.registered_agents = []
        self.cleanup_count = 0

    def register(self, agent):
        self.registered_agents.append(agent)

    def cleanup(self):
        self.cleanup_count += 1


def _streaming_request(
    team_type: str | None = "SUPERVISOR", global_variables: dict | None = None
):
    if global_variables is None:
        global_variables = {}
        if team_type is not None:
            global_variables["conversationTeam"] = {"type": team_type}
    return SimpleNamespace(
        conversation_id="conversation-a",
        user_id="user-a",
        query="hello",
        resume_input=None,
        ir_path="ignored",
        params=SimpleNamespace(
            conversation_history=[],
            global_variables=global_variables,
            ir_cache={"agentName": "Supervisor", "configs": {}},
        ),
    )


def _skill_streaming_request(global_variables: dict | None = None):
    request = _streaming_request(None, global_variables)
    request.params.ir_cache["configs"]["skills"] = {
        "skill_dir": "workspace-skills",
        "skill_info": [{"name": "meeting-minutes", "description": "Summarize meetings"}],
    }
    return request


def _configure_runner_for_stream(monkeypatch, runner, agent):
    runner._create_llm = AsyncMock(return_value=object())
    runner._download_skills = AsyncMock(return_value="/workspace/workspace-skills")
    runner._create_agent = lambda *_args: (agent, "supervisor")
    runner._register_plugins = AsyncMock()
    runner._register_mcp_servers = AsyncMock()
    runner._register_workflows = AsyncMock()
    runner._register_skills = AsyncMock()
    runner._attach_supervisor_skill_context = AsyncMock()
    runner._register_supervisor_handoff_tools = AsyncMock()
    monkeypatch.setattr(conversation_react_runner, "create_agent_session", lambda **_kwargs: _Session())


@pytest.mark.asyncio
async def test_runner_cleans_supervisor_sandbox_binding_after_a_successful_stream(monkeypatch):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        binder = _RunnerBinder()
        _configure_runner_for_stream(monkeypatch, runner, agent)
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        _ = [event async for event in runner.run_streaming(_streaming_request())]

        assert binder.registered_agents == [agent]
        assert binder.cleanup_count == 1
        runner._attach_supervisor_skill_context.assert_awaited_once()
        runner._register_supervisor_handoff_tools.assert_awaited_once()
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_runner_cleans_sandbox_binding_when_the_stream_is_closed_after_start(monkeypatch):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        binder = _RunnerBinder()
        _configure_runner_for_stream(monkeypatch, runner, agent)
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
        )
        stream = runner.run_streaming(_streaming_request())

        assert (await anext(stream))["event"] == "start"
        await stream.aclose()

        assert binder.registered_agents == [agent]
        assert binder.cleanup_count == 1
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_runner_cleans_sandbox_binding_when_rail_registration_is_cancelled(monkeypatch):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        agent.register_rail = AsyncMock(side_effect=asyncio.CancelledError())
        binder = _RunnerBinder()
        _configure_runner_for_stream(monkeypatch, runner, agent)
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
        )
        monkeypatch.setitem(
            sys.modules,
            "openjiuwen.extensions.tracer_otel.otel_rail",
            SimpleNamespace(OtelRail=object),
        )

        with pytest.raises(asyncio.CancelledError):
            await anext(runner.run_streaming(_streaming_request()))

        assert binder.registered_agents == [agent]
        assert binder.cleanup_count == 1
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_runner_cleans_sandbox_binding_when_later_supervisor_registration_fails(monkeypatch):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        binder = _RunnerBinder()
        _configure_runner_for_stream(monkeypatch, runner, agent)
        runner._register_supervisor_handoff_tools = AsyncMock(side_effect=RuntimeError("handoff failed"))
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        events = [event async for event in runner.run_streaming(_streaming_request())]

        assert events[-1]["event"] == "error"
        assert binder.registered_agents == [agent]
        assert binder.cleanup_count == 1
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_runner_binds_sandbox_for_an_app_without_supervisor_context(monkeypatch):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        remote = _RemoteOperation()
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        _configure_runner_for_stream(monkeypatch, runner, agent)
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        _ = [event async for event in runner.run_streaming(_streaming_request(None))]

        assert [card.name for card in agent.ability_manager.cards] == [
            "read_file",
            "execute_code",
            "execute_cmd",
        ]
        command_tool = next(
            tool for tool in manager.added_tools.values() if tool.card.name == "execute_cmd"
        )
        await command_tool.invoke({"command": "pwd"})
        assert remote.calls == [
            (
                "execute_cmd",
                "pwd",
                {
                    "cwd": str(context.workspace.work_dir),
                    "environment": _workspace_environment(context),
                },
            )
        ]
        assert len(manager.removed_tools) == 3
        assert manager.removed_operations == []
        runner._attach_supervisor_skill_context.assert_not_awaited()
        runner._register_supervisor_handoff_tools.assert_not_awaited()
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_runner_binds_sandbox_for_handoff_child_without_supervisor_context(monkeypatch):
    parent_context = _context()
    child_context = parent_context.for_child_call()
    token = execution_context_module.set_conversation_execution_context(child_context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        remote = _RemoteOperation()
        manager = _ResourceManager(remote)
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        _configure_runner_for_stream(monkeypatch, runner, agent)
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        _ = [
            event
            async for event in runner.run_streaming(
                _streaming_request(
                    None,
                    {
                        "trustedConversationIdentity": {"conversationId": "conversation-a"},
                        "subExecutionId": "handoff-child-a",
                    },
                ),
                execution_id="handoff-child-a",
            )
        ]

        assert child_context.workspace.conversation_root == parent_context.workspace.conversation_root
        assert child_context.workspace.work_dir == parent_context.workspace.work_dir
        assert [card.name for card in agent.ability_manager.cards] == [
            "read_file",
            "execute_code",
            "execute_cmd",
        ]
        command_tool = next(
            tool for tool in manager.added_tools.values() if tool.card.name == "execute_cmd"
        )
        await command_tool.invoke({"command": "pwd"})
        assert remote.calls == [
            (
                "execute_cmd",
                "pwd",
                {
                    "cwd": str(parent_context.workspace.work_dir),
                    "environment": _workspace_environment(parent_context),
                },
            )
        ]
        assert len(manager.removed_tools) == 3
        assert manager.removed_operations == []
        runner._attach_supervisor_skill_context.assert_not_awaited()
        runner._register_supervisor_handoff_tools.assert_not_awaited()
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
@pytest.mark.parametrize(
    "global_variables",
    [
        {},
        {
            "trustedConversationIdentity": {"conversationId": "conversation-a"},
            "subExecutionId": "handoff-child-a",
        },
    ],
    ids=["app", "handoff-child"],
)
async def test_runner_does_not_create_sandbox_or_local_tools_when_unconfigured(
    monkeypatch, global_variables
):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        manager = _ResourceManager(_RemoteOperation())
        factory = _Factory(None)
        binder = ConversationSandboxToolBinder(factory, manager)
        _configure_runner_for_stream(monkeypatch, runner, agent)
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        _ = [
            event
            async for event in runner.run_streaming(
                _streaming_request(None, global_variables)
            )
        ]

        assert factory.create_count == 1
        assert agent.ability_manager.cards == []
        assert manager.sys_operation_cards == []
        assert manager.tools == {}
        runner._attach_supervisor_skill_context.assert_not_awaited()
        runner._register_supervisor_handoff_tools.assert_not_awaited()
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
@pytest.mark.parametrize(
    "global_variables",
    [
        {},
        {
            "trustedConversationIdentity": {"conversationId": "conversation-a"},
            "subExecutionId": "handoff-child-a",
        },
    ],
    ids=["app", "handoff-child"],
)
async def test_skill_configured_app_and_child_keep_skill_registration_without_local_tools(
    monkeypatch, global_variables
):
    """A local Skill tool registration would make this request fail its isolation contract."""
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        binder = ConversationSandboxToolBinder(_Factory(None), _ResourceManager(_RemoteOperation()))
        _configure_runner_for_stream(monkeypatch, runner, agent)
        local_skill_registration = MagicMock()
        runner._register_skill_tools = local_skill_registration
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        events = [
            event
            async for event in runner.run_streaming(
                _skill_streaming_request(global_variables)
            )
        ]

        assert all(event["event"] != "error" for event in events)
        runner._download_skills.assert_awaited_once_with(
            "workspace-skills", [{"name": "meeting-minutes", "description": "Summarize meetings"}]
        )
        runner._register_skills.assert_awaited_once_with(
            {
                "agentName": "Supervisor",
                "configs": {
                    "skills": {
                        "skill_dir": "workspace-skills",
                        "skill_info": [
                            {"name": "meeting-minutes", "description": "Summarize meetings"}
                        ],
                    }
                },
            },
            agent,
            "supervisor",
            "/workspace/workspace-skills",
        )
        local_skill_registration.assert_not_called()
        assert agent.ability_manager.cards == []
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_skill_configured_conversation_never_attempts_a_local_sysoperation(monkeypatch):
    """Changing the conversation path to add a LOCAL card must trip this guard."""
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        binder = ConversationSandboxToolBinder(_Factory(None), _ResourceManager(_RemoteOperation()))
        _configure_runner_for_stream(monkeypatch, runner, agent)
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        attempted_local_cards = []

        def reject_local_sysoperation(card, *, tag):
            if card.mode is OperationMode.LOCAL:
                attempted_local_cards.append((card, tag))
            return _Result()

        monkeypatch.setattr(Runner.resource_mgr, "add_sys_operation", reject_local_sysoperation)

        _ = [event async for event in runner.run_streaming(_skill_streaming_request())]

        assert attempted_local_cards == []
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_skill_configured_conversation_binds_one_sandbox_tool_set_without_local_duplicate(
    monkeypatch,
):
    """Replacing the SANDBOX set with LOCAL or adding a second set must fail this check."""
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        manager = _ResourceManager(_RemoteOperation())
        binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()), manager)
        _configure_runner_for_stream(monkeypatch, runner, agent)
        local_skill_registration = MagicMock()
        runner._register_skill_tools = local_skill_registration
        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=lambda: binder),
            raising=False,
        )

        _ = [event async for event in runner.run_streaming(_skill_streaming_request())]

        local_skill_registration.assert_not_called()
        assert [card.name for card in agent.ability_manager.cards] == [
            "read_file",
            "execute_code",
            "execute_cmd",
        ]
        assert len(manager.sys_operation_cards) == 1
        sandbox_card, _tag = manager.sys_operation_cards[0]
        assert sandbox_card.mode is OperationMode.SANDBOX
        assert all(card.mode is not OperationMode.LOCAL for card, _tag in manager.sys_operation_cards)
    finally:
        execution_context_module.reset_conversation_execution_context(token)
