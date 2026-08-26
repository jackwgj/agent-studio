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

    async def attach(agent, catalog, recommended):
        captured["agent"] = agent
        captured["catalog"] = catalog
        captured["recommended"] = recommended

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


@pytest.mark.asyncio
@pytest.mark.parametrize("mode", ["Controller", "PlanExecute"])
async def test_controller_runner_injects_request_skill_function_into_agent_configs(monkeypatch, mode):
    captured = {}

    class _Config:
        def __init__(self):
            self.plugins = []

    class _GroupConfig:
        def __init__(self):
            self.main_agent = _Config()
            self.agents = [_Config()]

    async def create_group_config(*args, **kwargs):
        group_config = _GroupConfig()
        captured["group_config"] = group_config
        return group_config, {}

    class _IRConverter:
        @staticmethod
        async def create_agent_group_config(*args, **kwargs):
            return await create_group_config(*args, **kwargs)

    monkeypatch.setattr(conversation_controller_runner, "IRConverter", _IRConverter, raising=False)
    async def load_ir(_path):
        return {"agentId": "app-1", "configs": {}}

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
    request.params.conversation_history = []
    request.params.global_variables = request.params.global_variables
    request.user_id = "user-1"
    request.headers = {}
    request.query = "hello"

    await runner._build_request_agent_group(request, mode)

    assert len(captured["group_config"].main_agent.plugins) == 1
    assert len(captured["group_config"].agents[0].plugins) == 1
    function = captured["group_config"].main_agent.plugins[0]
    assert function.name == "activate_skill"
    assert function.params[0].name == "skill_id"
    assert "meeting-minutes" in function.description


@pytest.mark.asyncio
async def test_controller_skill_function_loads_instructions_through_shared_cache():
    class _Cache:
        async def load_instructions(self, skill):
            assert skill.skill_id == "meeting-minutes"
            return "# meeting instructions"

    from agent_runtime.conversation.runner.conversation_skill_function import (
        ConversationActivateSkillFunction,
    )
    from agent_runtime.supervisor.skill_context import build_skill_execution_context
    from agent_runtime.supervisor.skill_model import SkillDescriptor

    descriptor = SkillDescriptor(
        skill_id="meeting-minutes",
        version_id="v1",
        name="Meeting Minutes",
        description="Structure meeting notes",
        object_key="skills/meeting-minutes/v1/skill.zip",
    )
    context = build_skill_execution_context([descriptor], ["meeting-minutes"], _Cache())
    function = ConversationActivateSkillFunction(context)

    result = await function.ainvoke({"skill_id": "meeting-minutes"})

    assert result["errCode"] == 0
    assert result["data"]["skillId"] == "meeting-minutes"
    assert result["data"]["instructions"] == "# meeting instructions"


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
