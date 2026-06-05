#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
基类定义模块
"""

from abc import ABC, abstractmethod
from typing import List, Dict, Optional, Any, TypedDict, Literal

from pydantic import BaseModel, Field

METADATA_KEY_CANDIDATES = "candidates"
METADATA_KEY_MATCHED_LAYER = "matched_layer"
NO_MATCHED_LAYER = "None"

INTENT_FIELD_SIMILARITY = "similarity"
INTENT_FIELD_CONFIDENCE = "confidence"
INTENT_FIELD_INTENT = "intent"
INTENT_FIELD_INTENT_ID = "intent_id"


class IntentMetadata(TypedDict, total=False):
    candidates: List[str]
    error: str
    entities: Dict[str, Any]
    all_results: List[Dict[str, Any]]
    use_history: bool
    llm_reason: str


class DialogueMessage(BaseModel):
    """对话消息格式"""

    role: Literal["user", "assistant"]
    content: str = Field(min_length=1)


class Intent(BaseModel):
    """意图格式"""

    id: str = Field(min_length=1)
    name: str = Field(min_length=1)


class IntentInput(BaseModel):
    """
    输入
    """

    query: str = Field(min_length=1)
    dialogue_history: Optional[List[DialogueMessage]] = None
    intents: Optional[List[Intent]] = None
    valid_intents: Optional[Dict[str, str]] = None


class IntentOutput(BaseModel):
    """
    输出
    """

    intent_id: Optional[str] = None
    intent_name: Optional[str] = None
    score: float = Field(default=0.0)
    is_matched: bool = False
    matched_layer: Optional[str] = None
    candidates: List[str] = Field(default_factory=list)
    metadata: Dict[str, Any] = Field(default_factory=dict)


class BaseMatcher(ABC):
    """
    意图匹配基类
    """

    @abstractmethod
    async def invoke(
        self, input_data: IntentInput, top_k: Optional[int] = None, **kwargs
    ) -> IntentOutput:
        """
        异步匹配接口

        Args:
            input_data: 输入
            top_k: 候选意图数量
            **kwargs:

        Returns:
            IntentOutput: 匹配结果
        """
        pass
