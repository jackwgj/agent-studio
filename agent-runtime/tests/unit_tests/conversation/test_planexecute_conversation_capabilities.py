from __future__ import annotations

import copy
import asyncio
from types import SimpleNamespace
from unittest.mock import AsyncMock, MagicMock

import pytest

from agent_runtime.conversation import execution_context as execution_context_module
from agent_runtime.conversation.execution_context import (
    ConversationExecutionContext,
    ConversationIdentity,
)
from agent_runtime.conversation.runner.conversation_controller_runner import (
    ConversationControllerRunner,
)
from agent_runtime.conversation.runner.conversation_sandbox_function import (
    ConversationSandboxFunctionBinder,
)
from agent_runtime.conversation.runner.conversation_planexecute_agent import (
    ConversationPlanExecuteAgent,
)
from jiuwen.controller.common.config import SkillInjectionContext
from jiuwen.controller.agent.control_mode.plan_execute_mode import PlanExecuteMode
from jiuwen.controller.task_planner.planners.plan_execute_planner import PlanExecutePlanner


class _RemoteOperation:
    def __init__(self):
        self.calls = []

    def fs(self):
        return self

    def code(self):
        return self

    def shell(self):
        return self

    async def read_file(self, path, **kwargs):
        self.calls.append(("read_file", path, kwargs))
        return SimpleNamespace(
            code=0,
            message="success",
            data=SimpleNamespace(path=path, content="material", mode="text"),
        )

    async def execute_code(self, code, **kwargs):
        self.calls.append(("execute_code", code, kwargs))
        return SimpleNamespace(
            code=0,
            message="success",
            data=SimpleNamespace(cwd=kwargs["cwd"], exit_code=0, stdout="ok", stderr=""),
        )

    async def execute_cmd(self, command, **kwargs):
        self.calls.append(("execute_cmd", command, kwargs))
        return SimpleNamespace(
            code=0,
            message="success",
            data=SimpleNamespace(
                command=command,
                cwd=kwargs["cwd"],
                exit_code=0,
                stdout="ok",
                stderr="",
            ),
        )


class _Factory:
    def __init__(self, card):
        self.card = card

    def create(self):
        return self.card


def _context(conversation_id="conversation-a", execution_id="execution-a"):
    return ConversationExecutionContext.create(
        ConversationIdentity(
            project_id="project-a",
            workspace_id="workspace-a",
            user_id="user-a",
            conversation_id=conversation_id,
            execution_id=execution_id,
        ),
        "/workspace",
    )


def _team_config():
    return {
        "skillCatalog": [
            {
                "skillId": "bound",
                "versionId": "v1",
                "name": "同名技能",
                "description": "绑定能力",
                "objectKey": "skills/bound/v1/skill.zip",
            },
            {
                "skillId": "extra",
                "versionId": "v2",
                "name": "同名技能",
                "description": "补充能力",
                "objectKey": "skills/extra/v2/skill.zip",
            },
        ],
        "agentBoundSkillIds": ["bound"],
        "recommendedSkillIds": ["extra"],
    }


def test_planexecute_safe_ir_is_request_local_and_controller_is_unchanged():
    original = {
        "agentId": "agent-1",
        "is_published": True,
        "ir_path": "agents/agent-1.json",
        "configs": {
            "mode": "PlanExecute",
            "modelConfig": {"name": "model"},
            "plugins": [{"name": "plugin-a"}],
            "workflows": [{"name": "flow-a"}],
            "scenes": [{"id": "scene-a", "tools": ["plugin-a"]}],
            "planning": {"max_steps": 8},
            "skills": {"skill_dir": "skills/agent-1", "skill_info": [{"name": "x"}]},
        },
    }
    snapshot = copy.deepcopy(original)

    safe = ConversationControllerRunner._build_conversation_safe_ir(original, "PlanExecute")

    assert safe is not original
    assert "skills" not in safe["configs"]
    assert safe["configs"]["modelConfig"] == original["configs"]["modelConfig"]
    assert safe["configs"]["plugins"] == original["configs"]["plugins"]
    assert safe["configs"]["workflows"] == original["configs"]["workflows"]
    assert safe["configs"]["scenes"] == original["configs"]["scenes"]
    assert safe["configs"]["planning"] == original["configs"]["planning"]
    assert safe["is_published"] is False
    assert original == snapshot
    assert ConversationControllerRunner._build_conversation_safe_ir(original, "Controller") is original


def test_planexecute_skill_context_keeps_source_groups_and_turn_recommendation():
    execution_context = _context()
    token = execution_context_module.set_conversation_execution_context(execution_context)
    try:
        context = ConversationControllerRunner._build_skill_context(
            _team_config(), prepare_sandbox_resources=True
        )

        assert [item.skill_id for item in context.agent_bound_skills] == ["bound"]
        assert [item.skill_id for item in context.workspace_supplement_skills] == ["extra"]
        assert context.recommended_skill_ids == ("extra",)
        assert context.prepare_sandbox_resources is True

        prompt = ConversationControllerRunner._build_skill_prompt_suffix(context)
        assert "当前会话沙箱目录协议" in prompt
        assert str(execution_context.workspace.input_dir) in prompt
        assert str(execution_context.workspace.output_dir) in prompt
        assert "当前智能体已绑定 Skill" in prompt
        assert "工作空间补充 Skill" in prompt
        assert "本轮推荐 Skill" in prompt
        assert '"skillId": "bound"' in prompt
        assert '"skillId": "extra"' in prompt
        assert "versionId" not in prompt
        assert "objectKey" not in prompt
        assert "/opt/" not in prompt
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_conversation_planexecute_agent_bypasses_official_skill_sysoperation():
    expected = SkillInjectionContext(
        prompt_suffix="request prompt", tool_names={"activate_skill"}
    )
    agent = object.__new__(ConversationPlanExecuteAgent)
    agent._conversation_skill_context = expected
    plugin = SimpleNamespace(name="activate_skill")

    plugins, actual = await agent._inject_skills_if_needed(
        SimpleNamespace(sys_operation_card=object()), [plugin]
    )

    assert plugins == [plugin]
    assert actual is expected


def test_scene_protection_only_adds_functions_that_really_exist():
    scene = SimpleNamespace(tools=["business-tool"])
    plan_config = SimpleNamespace(scenes=[scene])
    agent_config = SimpleNamespace(plan_config=plan_config)

    ConversationControllerRunner._protect_planexecute_capabilities(
        agent_config, {"activate_skill", "read_file", "execute_cmd"}
    )

    assert scene.tools == ["business-tool", "activate_skill", "execute_cmd", "read_file"]


def test_protected_capabilities_survive_planning_and_step_scene_filters():
    business = SimpleNamespace(name="business-tool")
    hidden = SimpleNamespace(name="hidden-tool")
    activate = SimpleNamespace(name="activate_skill")
    shell = SimpleNamespace(name="execute_cmd")
    scene = SimpleNamespace(name="scene-a", tools=["business-tool"])
    config = SimpleNamespace(plan_config=SimpleNamespace(scenes=[scene]))
    ConversationControllerRunner._protect_planexecute_capabilities(
        config, {"activate_skill", "execute_cmd"}
    )

    planner = object.__new__(PlanExecutePlanner)
    planner.plugins = [business, hidden, activate, shell]
    planner.task_id = "task-a"
    planning = planner._filter_tools_by_scene(scene)

    mode = object.__new__(PlanExecuteMode)
    mode.task_id = "task-a"
    mode._skill_context = SkillInjectionContext(
        tool_names={"activate_skill", "execute_cmd"}
    )
    executing = mode._filter_plugins_by_scene(
        [business, hidden, activate, shell], scene
    )

    assert [item.name for item in planning] == [
        "business-tool",
        "activate_skill",
        "execute_cmd",
    ]
    assert [item.name for item in executing] == [
        "business-tool",
        "activate_skill",
        "execute_cmd",
    ]


def test_no_scene_keeps_all_planexecute_plugins():
    plugins = [SimpleNamespace(name="business-tool"), SimpleNamespace(name="execute_cmd")]
    planner = object.__new__(PlanExecutePlanner)
    planner.plugins = plugins
    planner.task_id = "task-a"

    assert planner._filter_tools_by_scene(None) == plugins


def test_planexecute_step_system_prompt_receives_request_skill_context():
    mode = object.__new__(PlanExecuteMode)
    mode.context_manager = SimpleNamespace(
        get_latest_k_chat_history=lambda _rounds: [{"role": "user", "content": "任务"}]
    )
    mode.plan_config = SimpleNamespace(reserved_max_chat_rounds=20)
    mode._skill_context = SkillInjectionContext(prompt_suffix="## Skill 使用规则")

    messages = mode._build_llm_messages("执行当前步骤")

    assert messages[0]["role"] == "system"
    assert "## Skill 使用规则" in messages[0]["content"]
    assert messages[-1] == {"role": "user", "content": "执行当前步骤"}


@pytest.mark.asyncio
async def test_planexecute_aio_functions_use_remote_operation_and_conversation_cwd(monkeypatch):
    from agent_runtime.conversation.sandbox.factory import ConversationSysOperationFactory
    from agent_runtime.conversation.sandbox.config import (
        ConversationSandboxConfig,
        ConversationSandboxMode,
    )

    card = ConversationSysOperationFactory(
        ConversationSandboxConfig(
            mode=ConversationSandboxMode.SANDBOX,
            server="http://sandbox.example",
            ssl_verify=False,
            sandbox_type="aio",
            idle_ttl_seconds=600,
            timeout_seconds=300,
            scope="system",
        )
    ).create()
    operation = _RemoteOperation()
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        monkeypatch.setattr(
            "agent_runtime.conversation.runner.conversation_sandbox_function.get_conversation_sandbox_operation",
            lambda _manager, _card: operation,
        )
        binder = ConversationSandboxFunctionBinder(_Factory(card), resource_manager=object())
        functions = binder.build()
        by_name = {function.name: function for function in functions}

        assert set(by_name) == {"read_file", "execute_code", "execute_cmd"}
        read_result = await by_name["read_file"].ainvoke({"path": "../input/材料.txt"})
        command_result = await by_name["execute_cmd"].ainvoke({"command": "pwd"})
        code_result = await by_name["execute_code"].ainvoke({"code": "print('ok')"})

        assert operation.calls[0][1] == str(context.workspace.input_dir / "材料.txt")
        assert operation.calls[1][2]["cwd"] == str(context.workspace.work_dir)
        assert operation.calls[2][2]["cwd"] == str(context.workspace.work_dir)
        assert operation.calls[1][2]["environment"]["CONVERSATION_OUTPUT_DIR"] == str(
            context.workspace.output_dir
        )
        assert read_result["errCode"] == 0
        assert read_result["data"]["content"] == "material"
        assert command_result["errCode"] == 0
        assert command_result["data"]["stdout"] == "ok"
        assert code_result["errCode"] == 0
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_planexecute_aio_rejects_paths_outside_conversation_before_remote_call(monkeypatch):
    from agent_runtime.conversation.sandbox.factory import ConversationSysOperationFactory
    from agent_runtime.conversation.sandbox.config import (
        ConversationSandboxConfig,
        ConversationSandboxMode,
    )

    card = ConversationSysOperationFactory(
        ConversationSandboxConfig(
            mode=ConversationSandboxMode.SANDBOX,
            server="http://sandbox.example",
            ssl_verify=False,
            sandbox_type="aio",
            idle_ttl_seconds=600,
            timeout_seconds=300,
            scope="system",
        )
    ).create()
    operation = _RemoteOperation()
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        monkeypatch.setattr(
            "agent_runtime.conversation.runner.conversation_sandbox_function.get_conversation_sandbox_operation",
            lambda _manager, _card: operation,
        )
        functions = ConversationSandboxFunctionBinder(
            _Factory(card), resource_manager=object()
        ).build()
        command = next(item for item in functions if item.name == "execute_cmd")

        with pytest.raises(ValueError, match="outside the active conversation"):
            await command.ainvoke({"command": "cat /etc/passwd"})
        assert operation.calls == []
    finally:
        execution_context_module.reset_conversation_execution_context(token)


@pytest.mark.asyncio
async def test_planexecute_aio_preserves_remote_execution_error(monkeypatch):
    from agent_runtime.conversation.sandbox.factory import ConversationSysOperationFactory
    from agent_runtime.conversation.sandbox.config import (
        ConversationSandboxConfig,
        ConversationSandboxMode,
    )

    card = ConversationSysOperationFactory(
        ConversationSandboxConfig(
            mode=ConversationSandboxMode.SANDBOX,
            server="http://sandbox.example",
            ssl_verify=False,
            sandbox_type="aio",
            idle_ttl_seconds=600,
            timeout_seconds=300,
            scope="system",
        )
    ).create()
    operation = _RemoteOperation()

    async def fail(_command, **_kwargs):
        raise RuntimeError("AIO shell unavailable")

    operation.execute_cmd = fail
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        monkeypatch.setattr(
            "agent_runtime.conversation.runner.conversation_sandbox_function.get_conversation_sandbox_operation",
            lambda _manager, _card: operation,
        )
        function = next(
            item
            for item in ConversationSandboxFunctionBinder(
                _Factory(card), resource_manager=object()
            ).build()
            if item.name == "execute_cmd"
        )

        with pytest.raises(RuntimeError, match="AIO shell unavailable"):
            await function.ainvoke({"command": "pwd"})
    finally:
        execution_context_module.reset_conversation_execution_context(token)


def test_planexecute_aio_normalizes_failed_operation_result_for_plugin_handler():
    result = SimpleNamespace(
        code=0,
        message="success",
        data=SimpleNamespace(exit_code=7, stdout="", stderr="command failed"),
    )

    normalized = ConversationSandboxFunctionBinder._to_plugin_result(result)

    assert normalized["errCode"] == 7
    assert normalized["errMessage"] == "command failed"


def test_planexecute_aio_normalizes_mapping_operation_result_for_plugin_handler():
    normalized = ConversationSandboxFunctionBinder._to_plugin_result(
        {
            "code": 0,
            "message": "success",
            "data": {"exit_code": 0, "stdout": "ok", "stderr": ""},
        }
    )

    assert normalized == {
        "errCode": 0,
        "data": {"exit_code": 0, "stdout": "ok", "stderr": ""},
    }


def test_unconfigured_aio_builds_no_functions():
    binder = ConversationSandboxFunctionBinder(_Factory(None), resource_manager=object())
    assert binder.build() == []


@pytest.mark.asyncio
async def test_planexecute_request_build_injects_only_main_agent_and_uses_safe_ir(
    monkeypatch,
):
    captured = {}
    scene = SimpleNamespace(tools=["business-tool"])
    main = SimpleNamespace(
        plugins=[SimpleNamespace(name="business-tool")],
        plan_config=SimpleNamespace(scenes=[scene]),
        skill_dir="published/skills",
        skill_info=[{"name": "published"}],
    )
    child = SimpleNamespace(plugins=[SimpleNamespace(name="child-tool")])
    group_config = SimpleNamespace(main_agent=main, agents=[main, child])

    class _IRConverter:
        @staticmethod
        async def create_agent_group_config(ir, *_args, **_kwargs):
            captured["ir"] = ir
            return group_config, {}

    fake_binder = SimpleNamespace(
        build=lambda: [SimpleNamespace(name="read_file"), SimpleNamespace(name="execute_cmd")],
        cleanup=lambda: None,
    )
    monkeypatch.setattr(
        "agent_runtime.conversation.runner.conversation_controller_runner.IRConverter",
        _IRConverter,
    )
    monkeypatch.setattr(
        "agent_runtime.conversation.runner.conversation_controller_runner.ConversationSandboxFunctionBinder.from_runtime_settings",
        lambda: fake_binder,
    )
    request = SimpleNamespace(
        conversation_id="conversation-a",
        params=SimpleNamespace(
            global_variables={"conversationTeam": _team_config()}
        ),
    )
    ir = {
        "agentId": "agent-a",
        "is_published": True,
        "configs": {
            "mode": "PlanExecute",
            "skills": {"skill_dir": "published/skills", "skill_info": [{"name": "x"}]},
        },
    }
    context = _context()
    token = execution_context_module.set_conversation_execution_context(context)
    try:
        actual_group, _, skill_context, binder = await ConversationControllerRunner()._build_request_agent_group(
            request, "PlanExecute", ir
        )
    finally:
        execution_context_module.reset_conversation_execution_context(token)

    assert actual_group is group_config
    assert binder is fake_binder
    assert skill_context.prepare_sandbox_resources is True
    assert captured["ir"] is not ir
    assert captured["ir"]["is_published"] is False
    assert "skills" not in captured["ir"]["configs"]
    assert [plugin.name for plugin in main.plugins] == [
        "business-tool",
        "activate_skill",
        "read_file",
        "execute_cmd",
    ]
    assert [plugin.name for plugin in child.plugins] == ["child-tool"]
    assert main.skill_dir == ""
    assert main.skill_info == []
    assert set(scene.tools) == {
        "business-tool",
        "activate_skill",
        "read_file",
        "execute_cmd",
    }


@pytest.mark.asyncio
async def test_two_planexecute_aio_bindings_keep_conversation_paths_isolated(monkeypatch):
    from agent_runtime.conversation.sandbox.factory import ConversationSysOperationFactory
    from agent_runtime.conversation.sandbox.config import (
        ConversationSandboxConfig,
        ConversationSandboxMode,
    )

    card = ConversationSysOperationFactory(
        ConversationSandboxConfig(
            mode=ConversationSandboxMode.SANDBOX,
            server="http://sandbox.example",
            ssl_verify=False,
            sandbox_type="aio",
            idle_ttl_seconds=600,
            timeout_seconds=300,
            scope="system",
        )
    ).create()
    first_operation = _RemoteOperation()
    second_operation = _RemoteOperation()
    operations = iter([first_operation, second_operation])
    monkeypatch.setattr(
        "agent_runtime.conversation.runner.conversation_sandbox_function.get_conversation_sandbox_operation",
        lambda _manager, _card: next(operations),
    )

    async def build_and_run(context):
        token = execution_context_module.set_conversation_execution_context(context)
        try:
            binder = ConversationSandboxFunctionBinder(
                _Factory(card), resource_manager=object()
            )
            command = next(item for item in binder.build() if item.name == "execute_cmd")
            await command.ainvoke({"command": "pwd"})
            return binder.scope_id
        finally:
            execution_context_module.reset_conversation_execution_context(token)

    first_context = _context("conversation-a", "execution-a")
    second_context = _context("conversation-b", "execution-b")
    first_scope, second_scope = await asyncio.gather(
        build_and_run(first_context), build_and_run(second_context)
    )

    assert first_scope != second_scope
    assert first_operation.calls[0][2]["cwd"] == str(first_context.workspace.work_dir)
    assert second_operation.calls[0][2]["cwd"] == str(second_context.workspace.work_dir)


@pytest.mark.asyncio
async def test_planexecute_group_creation_failure_cleans_request_binder(monkeypatch):
    binder = SimpleNamespace(cleanup=MagicMock())
    group_config = SimpleNamespace()
    runner = ConversationControllerRunner()
    runner._build_request_agent_group = AsyncMock(
        return_value=(group_config, {}, None, binder)
    )

    class _FailingGroup:
        def __init__(self, _config):
            pass

        async def start(self, _state):
            raise RuntimeError("start failed")

    monkeypatch.setattr(
        "agent_runtime.conversation.runner.conversation_controller_runner.ConversationPlanExecuteAgentGroup",
        _FailingGroup,
    )
    monkeypatch.setattr(
        "agent_runtime.conversation.runner.conversation_controller_runner.AsyncStateManager",
        lambda: SimpleNamespace(get_state=AsyncMock(return_value=None)),
    )
    request = SimpleNamespace(conversation_id="conversation-a")

    with pytest.raises(RuntimeError, match="start failed"):
        await runner._create_request_agent_group(request, "PlanExecute", {})

    binder.cleanup.assert_called_once_with()
