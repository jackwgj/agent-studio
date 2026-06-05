from memory_service.memory.llm.agentBuilder_memory_llm import AgentBuilderMemoryLLM


def test_call_llm_with_custom_header():
    custom_header = {"header1": "header1_value"}
    memory_llm = AgentBuilderMemoryLLM(
        model_name="DeepSeek",
        api_base="https://127.0.0.1:8010",
        custom_header=custom_header,
    )

    real_header = memory_llm._build_headers()
    assert ("header1" in real_header) == True
    assert real_header["header1"] == "header1_value"


def test_call_llm_with_api_key():
    memory_llm = AgentBuilderMemoryLLM(
        model_name="DeepSeek", api_base="https://127.0.0.1:8010", api_key="test-api-key"
    )

    real_header = memory_llm._build_headers()
    assert ("Authorization" in real_header) == True
    assert real_header["Authorization"] == "Bearer test-api-key"
