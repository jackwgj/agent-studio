from agent_runtime.conversation.config.supervisor_config import SupervisorConfig
from agent_runtime.conversation.runner.conversation_react_runner import (
    ConversationReActRunner,
)


def test_supervisor_config_converts_to_react_ir_shape():
    config = SupervisorConfig(
        agent_id="conversation_team_supervisor",
        agent_name="Team Supervisor",
        description="Dispatches work to configured agents",
        system_prompt="Delegate the task.",
        model_deployment_id="deployment-a",
        allowed_sub_agent_ids=["agent-a", "agent-b"],
    )

    ir = config.to_ir()

    assert ir["agentId"] == "conversation_team_supervisor"
    assert ir["agentName"] == "Team Supervisor"
    assert ir["description"] == "Dispatches work to configured agents"
    assert ir["configs"]["mode"] == "ReAct"
    assert ir["configs"]["sysPromptTemplate"] == "Delegate the task."
    assert ir["configs"]["modelConfig"]["modelName"] == "deployment-a"
    assert ir["configs"]["plugins"] == []
    assert ir["configs"]["workflows"] == []
    assert ir["configs"]["conversationTeam"]["subAgentIds"] == ["agent-a", "agent-b"]


def test_conversation_react_runner_exposes_supervisor_ir_conversion():
    config = SupervisorConfig(
        agent_id="supervisor",
        agent_name="Supervisor",
        description="desc",
        system_prompt="prompt",
        model_deployment_id="deployment-a",
        allowed_sub_agent_ids=[],
    )

    ir = ConversationReActRunner()._convert_supervisor_to_ir(config)

    assert ir["agentId"] == "supervisor"
    assert ir["configs"]["mode"] == "ReAct"
