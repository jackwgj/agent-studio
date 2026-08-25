"""Supervisor-only remote sandbox tool injection tests."""

from __future__ import annotations

import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock

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
        self.tools = {}
        self.removed_tools = []
        self.removed_operations = []

    def add_sys_operation(self, card, *, tag):
        self.sys_operation_cards.append((card, tag))
        return _Result()

    def get_sys_operation(self, _operation_id, *, tag):
        return self.operation

    def add_tool(self, tool, *, tag):
        self.tools[tool.card.id] = (tool, tag)
        return _Result()

    def remove_tool(self, tool_id, *, tag):
        self.removed_tools.append((tool_id, tag))
        self.tools.pop(tool_id, None)
        return _Result()

    def remove_sys_operation(self, operation_id, *, tag):
        self.removed_operations.append((operation_id, tag))
        return _Result()


class _Factory:
    def __init__(self, card):
        self.card = card

    def create(self):
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
        assert all(card.id.startswith(f"{registered_card.id}.") for card in agent.ability_manager.cards)

        tools = {tool.card.name: tool for tool, _tag in manager.tools.values()}
        await tools["read_file"].invoke({"path": "notes/../plan.md"})
        await tools["read_file"].invoke({"path": str(context.workspace.input_dir / "source.md")})
        await tools["execute_code"].invoke({"code": "print(1)", "cwd": "scratch"})
        await tools["execute_cmd"].invoke({"command": "pwd"})

        assert remote.calls[0] == ("read_file", f"{context.workspace.work_dir}/plan.md", {})
        assert remote.calls[1] == ("read_file", str(context.workspace.input_dir / "source.md"), {})
        assert remote.calls[2][0] == "execute_code"
        assert "os.chdir" in remote.calls[2][1]
        assert remote.calls[2][2] == {"cwd": f"{context.workspace.work_dir}/scratch"}
        assert remote.calls[3] == ("execute_cmd", "pwd", {"cwd": str(context.workspace.work_dir)})
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
    binder = ConversationSandboxToolBinder(_Factory(_sandbox_card()))
    try:
        agent = _Agent()
        binder.register(agent)

        operation = Runner.resource_mgr.get_sys_operation(
            binder.operation_id, tag=binder.operation_id
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

    assert Runner.resource_mgr.get_sys_operation(
        binder.operation_id, tag=binder.operation_id
    ) is None
    assert Runner.resource_mgr.get_tool(
        f"{binder.operation_id}.fs.read_file", tag=binder.operation_id
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
        assert len(manager.removed_operations) == 1
        assert manager.removed_operations[0][0] == manager.sys_operation_cards[0][0].id
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


def _streaming_request(team_type: str = "SUPERVISOR"):
    return SimpleNamespace(
        conversation_id="conversation-a",
        user_id="user-a",
        query="hello",
        resume_input=None,
        ir_path="ignored",
        params=SimpleNamespace(
            conversation_history=[],
            global_variables={"conversationTeam": {"type": team_type}},
            ir_cache={"agentName": "Supervisor", "configs": {}},
        ),
    )


def _configure_runner_for_stream(monkeypatch, runner, agent):
    runner._create_llm = AsyncMock(return_value=object())
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
async def test_runner_never_builds_a_sandbox_binding_for_an_app(monkeypatch):
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        runner = ConversationReActRunner()
        agent = _StreamingAgent()
        _configure_runner_for_stream(monkeypatch, runner, agent)

        def fail_if_called():
            raise AssertionError("APP must not receive conversation sandbox tools")

        monkeypatch.setattr(
            conversation_react_runner,
            "ConversationSandboxToolBinder",
            SimpleNamespace(from_runtime_settings=fail_if_called),
            raising=False,
        )

        _ = [event async for event in runner.run_streaming(_streaming_request("APP"))]
    finally:
        execution_context_module.reset_conversation_execution_context(token)
