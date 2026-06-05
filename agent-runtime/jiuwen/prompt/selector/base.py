#  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
"""
prompt selector
"""

from abc import ABC, abstractmethod
from typing import Dict, List, Literal, Any

from jiuwen.prompt.selector.memory import MemoryExtractor
from jiuwen.prompt.selector.memory import SummaryUpdator
from pydantic import BaseModel, Field, field_validator, ConfigDict


class BaseVariableExtractor(ABC):
    """BaseVariableExtractor"""

    @abstractmethod
    def run(self) -> Dict[str, Any]:
        """abstractmethod run"""

    @abstractmethod
    async def arun(self) -> Dict[str, Any]:
        """abstractmethod run"""


class ModelInfo(BaseModel):
    """ModelInfo"""

    model: str
    model_source: str
    temperature: float = Field(default=0.1)
    top_p: float = Field(default=0.1)


class MemoryVariableExtractor(BaseModel, BaseVariableExtractor):
    """MemoryVariableExtractor with Pydantic validation"""

    conversation_history: List[Dict] = Field(
        description="List of conversation history dictionaries"
    )

    summaries_params: List[Dict] = Field(
        description="List of parameters for generating summaries"
    )

    params_value: List[Dict] = Field(
        default_factory=list, description="List of parameter values"
    )

    model_info: dict = Field(description="Dictionary containing model information")

    summaries: dict = Field(
        default_factory=dict, description="Dictionary containing generated summaries"
    )

    validation: Literal["none", "all"] = Field(
        default="all", description="Validation level for the extractor"
    )

    language: str = Field(
        default="zh_CN",
        description="Language code for the content",
        pattern="^zh_CN$",  # Changed from regex to pattern in v2
    )

    # Replace Config class with model_config
    model_config = ConfigDict(
        extra="forbid",
        validate_assignment=True,
        frozen=False,  # Added for clarity (default is False)
    )

    @field_validator("params_value")
    @classmethod
    def validate_params_value(cls, v: List[Dict]) -> List[Dict]:
        """Updated validator syntax for v2"""
        if not isinstance(v, list):
            raise ValueError("params_value must be a list")
        for param in v:
            if "name" not in param:
                raise ValueError("params value not correct, name is missing")
            if "value" not in param:
                raise ValueError("params value not correct, value is missing")
        return v

    @field_validator("summaries_params")
    @classmethod
    def validate_summaries_params(cls, v: List[Dict]) -> List[Dict]:
        """Updated validator syntax for v2"""
        if not v:
            raise ValueError("summaries_params cannot be empty")
        for summary in v:
            if "name" not in summary:
                raise ValueError("summaries_params value not correct, name is missing")
            if "description" not in summary:
                raise ValueError(
                    "summaries_params value not correct, description is missing"
                )
            if "default_value" not in summary:
                raise ValueError(
                    "summaries_params value not correct, default_value is missing"
                )
        return v

    @field_validator("conversation_history")
    @classmethod
    def validate_conversation_history(cls, v: List[Dict]) -> List[Dict]:
        """Updated validator syntax for v2"""
        if not v:
            raise ValueError("conversation_history cannot be empty")
        for conversation in v:
            if "role" not in conversation:
                raise ValueError(
                    "conversation_history value not correct, role is missing"
                )
            if "content" not in conversation:
                raise ValueError(
                    "conversation_history value not correct, content is missing"
                )
        return v

    # Updated validator syntax for v2 with mode='before' option
    @field_validator("model_info", mode="before")
    @classmethod
    def validate_model_info(cls, v: Dict) -> Dict:
        """validate_model_info"""
        required_keys = {"model", "model_source"}
        if not isinstance(v, dict):
            raise ValueError("model_info must be a dictionary")
        if not required_keys.issubset(v.keys()):
            missing = required_keys - set(v.keys())
            raise ValueError(f"model_info missing required keys: {missing}")
        return v

    def run(self) -> Dict:
        """memory variable run"""
        try:
            memory_extractor = MemoryExtractor()
            result = memory_extractor.query(
                conversation_history=self.conversation_history,
                summaries_params=self.summaries_params,
                params_value=self.params_value,
                model_info=self.model_info,
                summaries=self.summaries,
                validation=self.validation,
                language=self.language,
            )
        except Exception as error:
            raise Exception("MemoryVariableExtractor run failed!") from error
        if result[0] == memory_extractor.status_success:
            return dict(code=memory_extractor.status_success.value, data=result[1])
        return dict(code=memory_extractor.status_failed.value, data=result[1])

    async def arun(self) -> Dict:
        """async_run"""
        try:
            memory_extractor = MemoryExtractor()
            result = await memory_extractor.async_query(
                conversation_history=self.conversation_history,
                summaries_params=self.summaries_params,
                params_value=self.params_value,
                model_info=self.model_info,
                summaries=self.summaries,
                validation=self.validation,
                language=self.language,
            )
        except Exception as error:
            raise Exception("MemoryVariableExtractor run failed!") from error
        if result[0] == memory_extractor.status_success:
            return dict(code=memory_extractor.status_success.value, data=result[1])
        return dict(code=memory_extractor.status_failed.value, data=result[1])


class SummaryAdjuster(BaseModel, BaseVariableExtractor):
    conversation_history: List[Dict] = Field(
        description="List of conversation history dictionaries"
    )

    summary: str = Field(
        default="", description="The summary collected so far and to be updated."
    )

    window_size: int = Field(
        default=5,
        description="Window Size",
        ge=0,
    )

    topics: List[str] = Field(
        default=[],
        description="List of Topics which should condition the summary updates.",
    )

    language: str = Field(
        default="zh_CN",
        description="Language code for the content",
        pattern="^zh_CN$",  # Changed from regex to pattern in v2
    )

    model_info: dict = Field(description="Dictionary containing model information")

    def run(self) -> Dict:
        """memory variable run"""
        try:
            summary_updator = SummaryUpdator()
            if not (self.conversation_history and self.window_size):
                result = summary_updator.status_success, dict(updated_summary="")
            else:
                result = summary_updator.update_summary(
                    conversation_history=self.conversation_history,
                    summary=self.summary,
                    window_size=self.window_size,
                    topics=self.topics,
                    language=self.language,
                    model_info=self.model_info,
                )
        except Exception as error:
            raise Exception("SummaryUpdator run failed!") from error
        if result[0] == summary_updator.status_success:
            return dict(code=summary_updator.status_success.value, data=result[1])
        return dict(code=summary_updator.status_failed.value, data=result[1])

    async def arun(self) -> Dict:
        """async_run"""
        try:
            summary_updator = SummaryUpdator()
            if not (self.conversation_history and self.window_size):
                result = summary_updator.status_success, dict(updated_summary="")
            else:
                result = await summary_updator.async_update_summary(
                    conversation_history=self.conversation_history,
                    summary=self.summary,
                    window_size=self.window_size,
                    topics=self.topics,
                    language=self.language,
                    model_info=self.model_info,
                )
        except Exception as error:
            raise Exception("SummaryAdjuster run failed!") from error
        if result[0] == summary_updator.status_success:
            return dict(code=summary_updator.status_success.value, data=result[1])
        return dict(code=summary_updator.status_failed.value, data=result[1])
