#  Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.

"""
LLMChatService module defines an interface class to handle the interaction with the agent-buildermodels.
"""

__all__ = ("LLMChatService",)

import json
from typing import ClassVar, List, Dict, Any, Tuple, Type, Iterator

from jiuwen.common.llm_service.base import ModelFactory
from jiuwen.common.llm_service.language_model.base import LanguageModelInput
from jiuwen.common.llm_service.model_util import ModelUtil
from jiuwen.orchestration.model_io.base import BaseModel
from jiuwen.orchestration.model_io.types import (
    MessageChunk,
    Message,
    Dialog,
    MessageBatch,
    ListChunk,
    InputType,
)
from jiuwen.orchestration.model_io.utils import (
    SupportUniCreate,
    SupportUniUserId,
    SupportUniMsgId,
)
from pydantic import Field as pydantic_Field, StrictStr


class _LLMRole:
    ASSISTANT = "assistant"
    FUNCTION = "function"
    SYSTEM = "system"
    USER = "user"

    ALLOWED_LIST = [ASSISTANT, FUNCTION, SYSTEM, USER]


class _LLMMsgKey:
    ROLE = "role"
    CONTENT = "content"
    NAME = "name"
    FUNCTION_CALL = "function_call"


class LLMChatService(
    BaseModel, SupportUniCreate, SupportUniUserId, SupportUniMsgId, alias="agentBuilder"
):
    """
    Interface class for invoking the agent-buildermodel in dialog mode.

    Allows more freedom to control the list of conversations used when requesting, populates custom prompts, etc.

    If no URL is input to the constructor function, or the URL is empty, the constructor will attempt to
    read the value from the environment variable ``LLM_SERVICE_URL`` as the value of the
    member ``url`` during the construction.
    """

    class _BodyKey:
        # These keys are only used for building request body.
        FUNCTIONS = "functions"
        MSGS = "messages"
        STREAM = "stream"

    _URL_ENV_KEY = "LLM_SERVICE_URL"  # Let BaseModel initialize url by this environ.

    __DEFAULT_USER_ID = "default_user_id"

    __DEFAULT_SYSTEM_MSG: ClassVar[List[Dict[str, Any]]] = [
        dict(
            role=_LLMRole.SYSTEM,
            content=(
                "你是一个智能助手，你为用户生成综合质量（包括有用性，事实性，无害性）极好的回复。"
                "当详尽回复更好时，你会生成全面且深入的高质量回复。当简洁回复更好时，你会生成简明扼要的高质量回复。"
            ),
        )
    ]

    DEFAULT_LLM_MODEL: ClassVar[str] = "agentBuilder_sigma_sft"

    functions: list = pydantic_Field([])
    """
    Function declaration provided for a large model to select.
    Default: empty list, indicates that no function is available.
    """

    model: str = DEFAULT_LLM_MODEL
    """
    Type of a large model.
    The available values and features are as follows:
    ==============================  ========================================================
    Optional Value                  Model Feature
    ==============================  ========================================================
    ``"agentBuilder_sigma_sft"``           standard agent-buildermodel.
    ``"agentBuilder_sigma_sft_71B"``       agent-builderlarge model that contains 71 billion parameters.
    ``"agentBuilder_sigma_unify"``         agent-builderlarge model that supports user-defined functions.
    ``"agentBuilder_sigma_web_research"``  agent-builderlarge model that supports Web content search.
    ==============================  ========================================================

    Default: ``"agentBuilder_sigma_sft"``
    """

    stop: StrictStr = pydantic_Field(None)
    """
    str, stop word. When the model encounters the specified stop word, the token is not generated.
    Default: ``None`` , indicating that there is no stop word.
    """

    temperature: float = pydantic_Field(default=0.1, ge=0, le=2)
    """
    Parameter that controls the decoding policy.
    Temperature value, which is a floating point number ranging from 0 to 2.
    The higher the value is, the more scattered the result is.
    The lower the value is, the more conservative the result is.
    Default: ``None``, indicating that determined by the model.
    """

    top_p: float = pydantic_Field(default=0.1, ge=0, le=1)
    """
    Parameter for controlling the decoding policy, whose value is a floating point number ranging from 0 to 1.
    The higher the ``top_p`` value model is, the more diverse the generated results are.
    ``top_p`` can be set to 0 to ensure decoding deterministically. In other cases,
    adjust ``top_p`` and ``temperature`` at the same time to verify the effect of different combinations.
    Default: ``None``, indicating that determined by the model.
    """

    top_k_num: int = pydantic_Field(None, ge=1, le=5000)
    """
    Number of candidate texts, whose value ranges from 1 to 5000.
    The model first selects the first ``top_k_num`` texts with the highest probability, and then randomly
    selects one of them as the final reply. This parameter applies only to the agent-buildermodel.
    Default: ``None``, indicating that determined by the model.
    """

    model_source: str = ""

    user_id: StrictStr = pydantic_Field(__DEFAULT_USER_ID, alias="userId")
    """
    str, User ID. Currently, it is used only for fault locating.

    Default: uses default ID.
    """

    __DEFAULT_MSG_ID: ClassVar[str] = "default_message_id"
    message_id: str = pydantic_Field(__DEFAULT_MSG_ID, alias="messageId")
    """Message ID, which is used to locate an error."""

    def set_user_id(self, user_id: str) -> None:
        self.user_id = user_id

    def set_message_id(self, message_id: str) -> None:
        self.message_id = message_id

    async def ainvoke(self, inputs: InputType, **kwargs) -> MessageBatch:
        """chat_agent invoke"""
        utf_str = "utf-8"
        trace_manager = self._before_action(inputs, kwargs)
        try:
            query_dialog = self._ensure_query_dialog(inputs)

            count = 0
            for item in query_dialog:
                if item.get("role", "") == "system":
                    count += 1
                else:
                    break
            if count == 1:
                query_dialog = self.__DEFAULT_SYSTEM_MSG + query_dialog

            _, body = self._to_request_param(query_dialog)

            chat_agent = ModelFactory().get_model(
                model_type=self.model_source,
                model_name=self.model,
                temperature=self.temperature,
                top_p=self.top_p,
            )
            functions = json.loads(body.decode(utf_str)).get("functions", [])
            inputs = LanguageModelInput(
                messages=ModelUtil.switch_message(
                    json.loads(body.decode(utf_str))["messages"]
                ),
                tools=functions if functions else None,
            )

            ss = chat_agent.invoke(inputs)

            if ss.type == "ai":
                res = Message(role="assistant", content=ss.content)
                if ss.tool_calls.name:
                    res.update(
                        {
                            "function_call": {
                                "name": ss.tool_calls.name,
                                "arguments": json.dumps(
                                    ss.tool_calls.args, ensure_ascii=False
                                ),
                            }
                        }
                    )

            return [res]
        except Exception as e:
            await trace_manager.on_llm_error(e)
            raise e

    async def astream(self, inputs: InputType, **kwargs) -> Iterator[ListChunk[MessageChunk]]:
        """
        Used to read data from the response of an HTTP request in streaming mode.
        """
        utf_str = "utf-8"
        trace_manager = asyncio.run(self._before_action(inputs, kwargs))
        try:
            _, body = self._to_request_param(
                self._ensure_query_dialog(inputs), stream_mode=True
            )
            chat_agent = ModelFactory().get_model(
                model_type=self.model_source,
                model_name=self.model,
                temperature=self.temperature,
                top_p=self.top_p,
            )
            functions = json.loads(body.decode(utf_str)).get("functions", [])
            inputs = LanguageModelInput(
                messages=ModelUtil.switch_message(
                    json.loads(body.decode(utf_str))["messages"]
                ),
                tools=functions if functions else None,
            )

            response = chat_agent.stream(inputs)
            for ss in response:
                if ss.type == "ai":
                    res = Message(role="assistant", content=ss.content)
                    if ss.tool_calls.name:
                        res.update({"name": ss.tool_calls.name})
                        res.update({"arguments": ss.tool_calls.args})

                    yield [res]

        except Exception as e:
            await trace_manager.on_llm_error(e)
            raise e

    def _to_request_param(
        self, query_dialog: Dialog, stream_mode=False
    ) -> Tuple[dict, bytes]:
        # To prevent accidental bring-in of members from the base class,
        # only the specified content is exported.
        body: dict = self.dict(
            exclude_none=True,
            include={
                self._BodyKey.FUNCTIONS,
                "model",
                "stop",
                "temperature",
                "top_k_num",
                "top_p",
                "user_id",
                "message_id",
            },
            by_alias=True,
        )
        body[self._BodyKey.STREAM] = stream_mode
        body[self._BodyKey.MSGS] = query_dialog
        # In normal cases, `body.functions` is always a list.
        # If this list is empty, it is not transmitted in the request body.
        func = body.get(self._BodyKey.FUNCTIONS)
        if (func is not None) and (not func):
            del body[self._BodyKey.FUNCTIONS]
        return self.extra_headers, json.dumps(body, ensure_ascii=False).encode("utf-8")

    def __check_one_message(self, index: int, msg: dict) -> None:
        def check_and_raise(
            name: str, type_: Type, *, reason=None, required=True
        ) -> None:
            obj = msg.get(name)
            if obj is None:
                if not required:
                    return
                raise KeyError(
                    f"Message[{index}] missing field '{name}' but required{(' ' + reason) if reason else ''}."
                )
            if not isinstance(obj, type_):
                raise TypeError(
                    f"Field '{name}' of message[{index}] should be '{type_.__name__}' type"
                    f" but got '{type(obj).__name__}'."
                )

        # Required field.
        check_and_raise(_LLMMsgKey.ROLE, str)
        role: str = msg[_LLMMsgKey.ROLE]
        if role not in _LLMRole.ALLOWED_LIST:
            raise ValueError(
                f"Role of message[{index}] should in {_LLMRole.ALLOWED_LIST} but got '{role}'."
            )

        # Optional field.
        # Content is only optional while function_call from assistant.
        check_and_raise(
            _LLMMsgKey.CONTENT,
            str,
            reason="(except of message which has field 'function_call')",
            required=msg.get(_LLMMsgKey.FUNCTION_CALL) is None,
        )
        # Function_call should be 'dict' type if it exists.
        check_and_raise(
            _LLMMsgKey.FUNCTION_CALL,
            dict,
            required=msg.get(_LLMMsgKey.FUNCTION_CALL) is not None,
        )
        # Depends on role.
        if role == _LLMRole.FUNCTION:
            # Name is required in function messages.
            check_and_raise(_LLMMsgKey.NAME, str, reason="in function call message")

    def _ensure_query_dialog(self, inputs: Any) -> list:
        has_system = False
        # Runtime type check.
        if isinstance(inputs, list):
            for index, obj in enumerate(inputs):
                if not isinstance(obj, dict):
                    raise TypeError(
                        f"typeError: excepted 'dict' of inputs[{index}], but got '{type(obj).__name__}'."
                    )
                self.__check_one_message(index, obj)
                if obj[_LLMMsgKey.ROLE] == _LLMRole.SYSTEM:
                    has_system = True
        elif isinstance(inputs, str):
            if not inputs:
                raise ValueError("Empty request message.")
            inputs = [dict(role=_LLMRole.USER, content=inputs)]
        else:
            raise TypeError(
                f"typeError: excepted 'list', but got '{type(inputs).__name__}'."
            )

        # May need to insert system message
        if not has_system:
            inputs = self.__DEFAULT_SYSTEM_MSG + inputs
        return inputs
