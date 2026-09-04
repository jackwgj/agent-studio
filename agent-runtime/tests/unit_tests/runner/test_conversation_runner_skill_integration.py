import pytest

from agent_runtime.conversation.runner import conversation_controller_runner
from agent_runtime.conversation.runner.conversation_controller_runner import (
    ConversationControllerRunner,
)
from agent_runtime.conversation.runner.conversation_react_runner import (
    ConversationReActRunner,
)


class _AbilityManager:
    def add(self, card):
        return type("AddResult", (), {"added": True})()


class _Agent:
    def __init__(self):
        self.ability_manager = _AbilityManager()
        self.prompt_sections = []

    def add_prompt_builder_section(self, name, content, priority):
        self.prompt_sections.append((name, content, priority))


@pytest.mark.asyncio
async def test_react_runner_attaches_request_supervisor_skill_context(monkeypatch):
    captured = {}

    async def attach(agent, catalog, recommended, agent_bound_skill_ids=None):
        captured["agent"] = agent
        captured["catalog"] = catalog
        captured["recommended"] = recommended
        captured["bound"] = agent_bound_skill_ids

    monkeypatch.setattr(
        "agent_runtime.conversation.runner.conversation_react_runner.attach_skill_context",
        attach,
    )

    runner = ConversationReActRunner()
    agent = _Agent()
    team_config = {
        "type": "SUPERVISOR",
        "skillCatalog": [
            {
                "skillId": "meeting-minutes",
                "versionId": "v1",
                "name": "Meeting Minutes",
                "description": "Structure meeting notes",
                "objectKey": "user/skills/meeting-minutes/v1/skill.zip",
            }
        ],
        "recommendedSkillIds": ["meeting-minutes"],
    }

    await runner._attach_supervisor_skill_context(agent, team_config)

    assert captured["agent"] is agent
    assert captured["catalog"][0].skill_id == "meeting-minutes"
    assert captured["recommended"] == ["meeting-minutes"]
    assert captured["bound"] == []


def test_react_runner_builds_current_turn_recommendation_without_changing_source_group():
    team_config = {
        "skillCatalog": [
            {
                "skillId": "bound",
                "versionId": "v1",
                "name": "Bound Skill",
                "description": "bound",
                "objectKey": "skills/bound/v1/skill.zip",
            },
            {
                "skillId": "extra",
                "versionId": "v2",
                "name": "Extra Skill",
                "description": "extra",
                "objectKey": "skills/extra/v2/skill.zip",
            },
        ],
        "recommendedSkillIds": ["extra", "bound"],
        "agentBoundSkillIds": ["bound"],
    }

    prompt = ConversationReActRunner._build_request_skill_recommendation(team_config)

    assert "本轮推荐 Skill" in prompt
    assert '"skillId": "extra"' in prompt
    assert '"source": "工作空间补充"' in prompt
    assert '"skillId": "bound"' in prompt
    assert '"source": "当前智能体已绑定"' in prompt
    assert ConversationReActRunner._build_request_skill_recommendation({
        **team_config, "recommendedSkillIds": []
    }) == ""


def test_conversation_safe_ir_removes_only_bound_skill_config_from_deep_copy():
    published = {
        "agentId": "agent-1",
        "configs": {
            "mode": "ReAct",
            "modelConfig": {"modelName": "model-1"},
            "plugins": [{"id": "plugin-1"}],
            "mcpServers": [{"id": "mcp-1"}],
            "workflows": [{"id": "flow-1"}],
            "maxIterations": 30,
            "skills": {"skill_dir": "agent-skills", "skill_info": [{"name": "old"}]},
        },
    }

    safe = ConversationReActRunner._build_conversation_safe_ir(published)

    assert "skills" not in safe["configs"]
    for key in ("mode", "modelConfig", "plugins", "mcpServers", "workflows", "maxIterations"):
        assert safe["configs"][key] == published["configs"][key]
    assert "skills" in published["configs"]
    safe["configs"]["modelConfig"]["modelName"] = "changed"
    assert published["configs"]["modelConfig"]["modelName"] == "model-1"


@pytest.mark.asyncio
@pytest.mark.parametrize("mode", ["Controller", "PlanExecute"])
async def test_controller_modes_build_real_agent_group_without_mode_rejection(
    monkeypatch, mode
):
    captured = {}

    class _Config:
        def __init__(self):
            self.plugins = []

    class _GroupConfig:
        def __init__(self):
            self.main_agent = _Config()
            self.agents = [_Config()]

    class _IRConverter:
        @staticmethod
        async def create_agent_group_config(*args, **kwargs):
            captured["group_config"] = _GroupConfig()
            return captured["group_config"], {}

    async def load_ir(_path):
        return {"agentId": "app-1", "configs": {"mode": mode}}

    monkeypatch.setattr(conversation_controller_runner, "IRConverter", _IRConverter)
    monkeypatch.setattr(conversation_controller_runner, "async_ir_load", load_ir)

    runner = ConversationControllerRunner()
    request = type("Request", (), {})()
    request.conversation_id = "conversation-1"
    request.ir_path = "agent/ir/app/app.json"
    request.params = type("Params", (), {})()
    request.params.global_variables = {
        "conversationTeam": {
            "type": "APP",
            "skillCatalog": [
                {
                    "skillId": "meeting-minutes",
                    "versionId": "v1",
                    "name": "Meeting Minutes",
                    "description": "Structure meeting notes",
                    "objectKey": "skills/meeting-minutes/v1/skill.zip",
                }
            ],
            "recommendedSkillIds": ["meeting-minutes"],
        }
    }

    _, _, skill_context = await runner._build_request_agent_group(request, mode)

    assert len(captured["group_config"].main_agent.plugins) == 1
    assert captured["group_config"].main_agent.plugins[0].name == "activate_skill"
    assert skill_context.prepare_sandbox_resources is False


@pytest.mark.asyncio
async def test_controller_skill_function_loads_instructions_without_preparing_sandbox(
    monkeypatch,
):
    class _Cache:
        async def load_instructions(self, skill):
            assert skill.skill_id == "meeting-minutes"
            return "# meeting instructions"

    from agent_runtime.conversation.runner.conversation_skill_function import (
        ConversationActivateSkillFunction,
    )
    from agent_runtime.supervisor.tool import activate_skill_tool
    from agent_runtime.supervisor.skill_context import build_skill_execution_context
    from agent_runtime.supervisor.skill_model import SkillDescriptor

    descriptor = SkillDescriptor(
        skill_id="meeting-minutes",
        version_id="v1",
        name="Meeting Minutes",
        description="Structure meeting notes",
        object_key="skills/meeting-minutes/v1/skill.zip",
    )
    context = build_skill_execution_context(
        [descriptor],
        ["meeting-minutes"],
        _Cache(),
        prepare_sandbox_resources=False,
    )
    function = ConversationActivateSkillFunction(context)

    monkeypatch.setattr(
        activate_skill_tool, "conversation_skill_sandbox_enabled", lambda: True
    )

    async def unexpected_prepare(*_args, **_kwargs):
        raise AssertionError("Controller/PlanExecute must not prepare AIO resources")

    monkeypatch.setattr(
        activate_skill_tool, "prepare_conversation_skill", unexpected_prepare
    )

    result = await function.ainvoke({"skill_id": "meeting-minutes"})

    assert result["errCode"] == 0
    assert result["data"]["skillId"] == "meeting-minutes"
    assert result["data"]["instructions"] == "# meeting instructions"
    assert result["data"]["resourceState"] == "instructions_only"
    assert "sandboxPath" not in result["data"]


@pytest.mark.asyncio
async def test_controller_skill_function_returns_jiuwen_error_contract_for_unknown_skill():
    from agent_runtime.conversation.runner.conversation_skill_function import (
        ConversationActivateSkillFunction,
    )
    from agent_runtime.supervisor.skill_context import build_skill_execution_context

    function = ConversationActivateSkillFunction(build_skill_execution_context([], []))

    result = await function.ainvoke({"skill_id": "missing"})

    assert result["errCode"] == -1
    assert "not available" in result["errMessage"]
