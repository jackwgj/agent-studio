from typing import Any, AsyncIterable, List, Optional, Union

from jiuwen.extension.wrapper.test.mock.openjiuwen_customer_chat_llm import (
    MockModelClient,
)
from openjiuwen.core.common.clients.client_registry import get_client_registry
from openjiuwen.core.foundation.llm.schema.config import (
    ModelClientConfig,
    ModelRequestConfig,
)
from openjiuwen.core.foundation.llm.schema.message import (
    AssistantMessage,
    BaseMessage,
    UsageMetadata,
)
from openjiuwen.core.foundation.llm.schema.message_chunk import AssistantMessageChunk
from openjiuwen.core.foundation.llm.schema.tool_call import ToolCall
from openjiuwen.core.foundation.tool import ToolInfo

CONTRACT_MODEL_CALLS = []
CONTRACT_MODEL_RESPONSE_QUEUE = []
CONTRACT_MODEL_STREAM_RESPONSE_QUEUE = []


def reset_contract_model_calls() -> None:
    """Clear recorded contract mock calls."""
    CONTRACT_MODEL_CALLS.clear()
    CONTRACT_MODEL_RESPONSE_QUEUE.clear()
    CONTRACT_MODEL_STREAM_RESPONSE_QUEUE.clear()


def get_contract_model_calls() -> list[dict]:
    """Return recorded contract mock calls."""
    return list(CONTRACT_MODEL_CALLS)


def set_contract_model_responses(responses: list[str | AssistantMessage]) -> None:
    """Set deterministic invoke responses consumed by subsequent model calls."""
    CONTRACT_MODEL_RESPONSE_QUEUE.clear()
    CONTRACT_MODEL_RESPONSE_QUEUE.extend(responses)


def set_contract_model_stream_responses(
    responses: list[list[AssistantMessageChunk]],
) -> None:
    """Set deterministic stream responses consumed by subsequent model stream calls."""
    CONTRACT_MODEL_STREAM_RESPONSE_QUEUE.clear()
    CONTRACT_MODEL_STREAM_RESPONSE_QUEUE.extend(responses)


def _record_call(method: str, messages: list[dict], tools, **kwargs) -> None:
    CONTRACT_MODEL_CALLS.append(
        {
            "method": method,
            "messages": messages,
            "tools": tools,
            "kwargs": kwargs,
        }
    )


class ContractMockModelClient(MockModelClient):
    """Deterministic model client for local model contract integration tests."""

    __client_name__ = "ContractMock"
    __client_type__ = "llm"

    def __init__(
        self, model_config: ModelRequestConfig, model_client_config: ModelClientConfig
    ):
        super().__init__(model_config, model_client_config)

    async def invoke(
        self,
        messages: Union[str, List[BaseMessage], List[dict]],
        *,
        tools: Union[List[ToolInfo], List[dict], None] = None,
        temperature: Optional[float] = None,
        top_p: Optional[float] = None,
        model: str = None,
        max_tokens: Optional[int] = None,
        stop: Union[Optional[str], None] = None,
        output_parser: Optional[Any] = None,
        timeout: float = None,
        **kwargs,
    ) -> AssistantMessage:
        messages_dict = self._convert_messages_to_dict(messages)
        _record_call(
            "invoke",
            messages_dict,
            tools,
            temperature=temperature,
            top_p=top_p,
            model=model,
            max_tokens=max_tokens,
            stop=stop,
            timeout=timeout,
            extra_kwargs=kwargs,
        )

        last_content = messages_dict[-1].get("content", "") if messages_dict else ""
        if CONTRACT_MODEL_RESPONSE_QUEUE:
            response = CONTRACT_MODEL_RESPONSE_QUEUE.pop(0)
            if isinstance(response, AssistantMessage):
                return response
            return AssistantMessage(content=response)
        if "__MODEL_CONTRACT_INTENTION__" in last_content:
            return AssistantMessage(
                content='{"result": "7", "reason": "mock intention"}'
            )
        if "__MODEL_CONTRACT_ROUTE_CHILD_THEN_GLOBAL_WORKFLOW__" in last_content:
            return AssistantMessage(
                content='{"result": 2, "class": "\\u5206\\u7c7b2", "reason": "mock route"}'
            )
        if "__MODEL_CONTRACT_ROUTE_CHILD_THEN_NORMAL_WORKFLOW__" in last_content:
            route_calls = [
                call
                for call in CONTRACT_MODEL_CALLS
                if call["method"] == "invoke"
                and "__MODEL_CONTRACT_ROUTE_CHILD_THEN_NORMAL_WORKFLOW__"
                in str(call["messages"][-1].get("content", ""))
            ]
            result = 2 if len(route_calls) == 1 else 3
            return AssistantMessage(
                content=(
                    f'{{"result": {result}, "class": "\\u5206\\u7c7b{result}", '
                    f'"reason": "mock normal workflow route"}}'
                )
            )
        if "__MODEL_CONTRACT_INVALID_INTENTION__" in last_content:
            return AssistantMessage(content="not a json intention result")
        if "__MODEL_CONTRACT_ROUTE_CHILD_THEN_SERVICE__" in last_content:
            route_calls = [
                call
                for call in CONTRACT_MODEL_CALLS
                if call["method"] == "invoke"
                and "__MODEL_CONTRACT_ROUTE_CHILD_THEN_SERVICE__"
                in str(call["messages"][-1].get("content", ""))
            ]
            result = 2 if len(route_calls) == 1 else 4
            return AssistantMessage(
                content=(
                    f'{{"result": {result}, "class": "\\u5206\\u7c7b{result}", '
                    f'"reason": "mock route"}}'
                )
            )
        if "__MODEL_CONTRACT_TASK_COMPLEX__" in last_content:
            return AssistantMessage(content="1")
        if "__MODEL_CONTRACT_TASK_SIMPLE__" in last_content:
            return AssistantMessage(content="0")
        if "__MODEL_CONTRACT_TOOL_CALL__" in last_content:
            return AssistantMessage(
                content="",
                tool_calls=[
                    ToolCall(
                        id="call_contract_invoke",
                        type="function",
                        name="mock_contract_tool",
                        arguments='{"query": "contract"}',
                    )
                ],
            )
        return AssistantMessage(content="contract mock response")

    async def stream(
        self,
        messages: Union[str, List[BaseMessage], List[dict]],
        *,
        tools: Union[List[ToolInfo], List[dict], None] = None,
        temperature: Optional[float] = None,
        top_p: Optional[float] = None,
        model: str = None,
        max_tokens: Optional[int] = None,
        stop: Union[Optional[str], None] = None,
        output_parser: Optional[Any] = None,
        timeout: float = None,
        **kwargs,
    ) -> AsyncIterable[AssistantMessageChunk]:
        messages_dict = self._convert_messages_to_dict(messages)
        _record_call(
            "stream",
            messages_dict,
            tools,
            temperature=temperature,
            top_p=top_p,
            model=model,
            max_tokens=max_tokens,
            stop=stop,
            timeout=timeout,
            extra_kwargs=kwargs,
        )

        last_content = messages_dict[-1].get("content", "") if messages_dict else ""
        if CONTRACT_MODEL_STREAM_RESPONSE_QUEUE:
            for chunk in CONTRACT_MODEL_STREAM_RESPONSE_QUEUE.pop(0):
                yield chunk
            return
        if "__MODEL_CONTRACT_STREAM_TEXT__" in last_content:
            yield AssistantMessageChunk(content="mock ")
            yield AssistantMessageChunk(content="stream")
            return
        if "__MODEL_CONTRACT_STREAM_USAGE__" in last_content:
            yield AssistantMessageChunk(content="usage ")
            yield AssistantMessageChunk(
                content="stream",
                usage_metadata=UsageMetadata(
                    model_name="contract_mock_model",
                    total_latency=1.25,
                    input_tokens=11,
                    output_tokens=7,
                    total_tokens=18,
                ),
            )
            return
        if "__MODEL_CONTRACT_STREAM_TOOL_CALL__" in last_content:
            yield AssistantMessageChunk(
                content="",
                tool_calls=[
                    ToolCall(
                        id="call_contract_stream",
                        type="function",
                        name="mock_contract_tool",
                        arguments='{"query": "stream"}',
                    )
                ],
            )
            return
        yield AssistantMessageChunk(content="contract stream response")


def register_contract_mock_model() -> None:
    """Register the deterministic contract mock model client."""
    get_client_registry().register_class(ContractMockModelClient)


register_contract_mock_model()
