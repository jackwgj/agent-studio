"""Exercise conversation registration against the SDK's real duplicate-key guard."""

import asyncio
from concurrent.futures import ThreadPoolExecutor
from types import SimpleNamespace

import pytest
from openjiuwen.core.runner.resources_manager.resource_manager import ResourceMgr
from openjiuwen.core.runner import Runner

from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext, ConversationIdentity,
    set_conversation_execution_context, reset_conversation_execution_context,
)
from agent_runtime.conversation.input_artifact_bridge import conversation_sandbox_operation
from agent_runtime.conversation.sandbox import (
    ConversationSandboxConfig, ConversationSandboxMode,
    ConversationSandboxToolBinder, ConversationSysOperationFactory,
)
from agent_runtime.conversation.sandbox.registration import SHARED_OPERATION_ID


def factory(scope="system", server="http://sandbox.test:8082"):
    return ConversationSysOperationFactory(ConversationSandboxConfig(
        mode=ConversationSandboxMode.SANDBOX, server=server, ssl_verify=False,
        sandbox_type="aio", idle_ttl_seconds=600, timeout_seconds=300, scope=scope,
    ))


def context(name):
    return ConversationExecutionContext.create(ConversationIdentity(
        project_id="project", workspace_id="workspace", user_id=name,
        conversation_id=name, execution_id=f"run-{name}",
    ), "/workspace")


class Agent:
    def __init__(self):
        self.cards = []
        self.ability_manager = SimpleNamespace(add=self.cards.append)


def register_flow(manager, scope):
    card = factory(scope).create().model_copy(deep=True)
    card.id = "flow_code_sandbox_sys_op"
    card.gateway_config.isolation.prefix = ""
    result = manager.add_sys_operation(card)
    assert result.is_ok(), result.error()
    return manager.get_sys_operation(card.id)


@pytest.mark.parametrize("scope", ["system", "session"])
@pytest.mark.asyncio
async def test_startup_flow_and_all_conversation_bridges_share_only_conversation_operation(monkeypatch, scope):
    """Unique IDs alone collide; fixed prefix without reuse collides on the next bridge."""
    manager = ResourceMgr()
    flow = register_flow(manager, scope)
    configured_factory = factory(scope)
    configured_card = configured_factory.create()
    monkeypatch.setattr("openjiuwen.core.runner.Runner", SimpleNamespace(
        resource_mgr=manager, callback_framework=Runner.callback_framework,
    ))
    monkeypatch.setattr(ConversationSysOperationFactory, "create", lambda self: configured_card)
    token = set_conversation_execution_context(context("first"))
    binders = []
    try:
        for _ in range(3):
            first, second = Agent(), Agent()
            a = ConversationSandboxToolBinder(configured_factory, manager)
            b = ConversationSandboxToolBinder(configured_factory, manager)
            binders.extend([a, b])
            a.register(first)
            b.register(second)
            assert not ({c.id for c in first.cards} & {c.id for c in second.cards})
            async with conversation_sandbox_operation("conversation_input_bridge") as operation:
                assert operation is not flow
                for stage in ("skill", "output", "execution_cleanup", "workspace_cleanup"):
                    async with conversation_sandbox_operation(stage) as other:
                        assert other is operation
                a.cleanup()
                assert all(manager.get_tool(c.id, tag=b.operation_id) is not None for c in second.cards)
                b.cleanup()
            async with conversation_sandbox_operation("next_round") as next_operation:
                assert next_operation is operation
            assert manager.get_sys_operation("flow_code_sandbox_sys_op") is flow
    finally:
        for binder in binders:
            binder.cleanup()
        reset_conversation_execution_context(token)


def test_parallel_registration_preserves_request_tools_and_existing_flow():
    manager = ResourceMgr()
    flow = register_flow(manager, "system")

    def bind(index):
        token = set_conversation_execution_context(context(str(index)))
        try:
            agent = Agent()
            binder = ConversationSandboxToolBinder(factory(), manager)
            binder.register(agent)
            return binder, agent.cards
        finally:
            reset_conversation_execution_context(token)

    with ThreadPoolExecutor(max_workers=8) as pool:
        bindings = list(pool.map(bind, range(16)))
    assert len({card.id for _, cards in bindings for card in cards}) == 48
    for binder, cards in bindings:
        binder.cleanup()
        assert all(manager.get_tool(c.id, tag=binder.operation_id) is None for c in cards)
    assert manager.get_sys_operation("flow_code_sandbox_sys_op") is flow


def test_changed_configuration_fails_closed_without_replacing_live_operation():
    manager = ResourceMgr()
    token = set_conversation_execution_context(context("first"))
    binder = ConversationSandboxToolBinder(factory(), manager)
    try:
        binder.register(Agent())
        changed = ConversationSandboxToolBinder(factory(server="http://other.test:8082"), manager)
        with pytest.raises(RuntimeError, match="configuration.*changed|configuration.*mismatch"):
            changed.register(Agent())
    finally:
        binder.cleanup()
        reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_shared_operation_keeps_concurrent_cwds_and_survives_cancelled_borrower(monkeypatch):
    manager = ResourceMgr()
    flow = register_flow(manager, "system")
    configured = factory()
    card = configured.create()
    monkeypatch.setattr("openjiuwen.core.runner.Runner", SimpleNamespace(
        resource_mgr=manager, callback_framework=Runner.callback_framework,
    ))
    monkeypatch.setattr(ConversationSysOperationFactory, "create", lambda self: card)
    entered = asyncio.Event()

    async def cancelled_bridge():
        async with conversation_sandbox_operation("input"):
            entered.set()
            await asyncio.Event().wait()

    task = asyncio.create_task(cancelled_bridge())
    await entered.wait()
    task.cancel()
    with pytest.raises(asyncio.CancelledError):
        await task

    operation = manager.get_sys_operation(SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID)

    async def remote_pwd(_command, **kwargs):
        await asyncio.sleep(0)
        return kwargs["cwd"]

    monkeypatch.setattr(operation.shell(), "execute_cmd", remote_pwd)

    async def execute(name):
        execution = context(name)
        token = set_conversation_execution_context(execution)
        binder = ConversationSandboxToolBinder(configured, manager)
        try:
            agent = Agent()
            binder.register(agent)
            command = next(c for c in agent.cards if c.name == "execute_cmd")
            tool = manager.get_tool(command.id, tag=binder.operation_id)
            return await tool.invoke({"command": "pwd"})
        finally:
            binder.cleanup()
            reset_conversation_execution_context(token)

    first, second = await asyncio.gather(execute("first"), execute("second"))
    assert first == str(context("first").workspace.work_dir)
    assert second == str(context("second").workspace.work_dir)
    assert first != second
    assert manager.get_sys_operation(SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID) is operation
    assert manager.get_sys_operation("flow_code_sandbox_sys_op") is flow


@pytest.mark.asyncio
async def test_registration_failure_preserves_stage_and_cause_and_allows_retry(monkeypatch):
    manager = ResourceMgr()
    original_add = manager.add_sys_operation
    card = factory().create()
    cause = ValueError("isolation key rejected")
    monkeypatch.setattr("openjiuwen.core.runner.Runner", SimpleNamespace(
        resource_mgr=manager, callback_framework=Runner.callback_framework,
    ))
    monkeypatch.setattr(ConversationSysOperationFactory, "create", lambda self: card)
    monkeypatch.setattr(manager, "add_sys_operation", lambda *a, **kw: SimpleNamespace(
        is_ok=lambda: False, error=lambda: cause,
    ))
    with pytest.raises(RuntimeError, match="conversation_output_bridge: sandbox registration failed.*isolation key rejected") as error:
        async with conversation_sandbox_operation("conversation_output_bridge"):
            pytest.fail("failed registration must not provide an operation")
    assert error.value.__cause__ is not None
    assert manager.get_sys_operation(SHARED_OPERATION_ID, tag=SHARED_OPERATION_ID) is None
    monkeypatch.setattr(manager, "add_sys_operation", original_add)
    async with conversation_sandbox_operation("conversation_output_bridge") as operation:
        assert operation is not None
