#  Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.

"""
全局配置模块
"""

from typing import Dict, Any, Optional, List

from pydantic import BaseModel, Field


class RuleMatcherConfig(BaseModel):
    enabled: bool = True  # 开关
    tokenizer_config: Optional[Dict[str, Any]] = None  # 分词器配置
    threshold: float = Field(default=0.9)

    use_history: bool = False  # 是否启用多轮对话
    history_max_turns: int = Field(default=3)  # 使用的最大对话轮数


class QueryMatcherConfig(BaseModel):
    """QueryMatcher 配置"""

    enabled: bool = True
    threshold: float = Field(default=0.5)  # 阈值

    use_history: bool = False  # 是否启用多轮对话
    history_max_turns: int = Field(default=3)  # 使用的最大对话轮数


class SemanticMatcherConfig(BaseModel):
    """SemanticMatcher 配置"""

    enabled: bool = True
    threshold: float = Field(default=0.5)

    use_history: bool = False  # 是否启用多轮对话
    history_max_turns: int = Field(default=3)  # 使用的最大对话轮数


class LLMConfig(BaseModel):
    """LLM配置"""

    model_type: str = ""
    model_name: str = ""
    hyper_parameters: Dict[str, Any] = Field(default_factory=dict)
    extension: Dict[str, Any] = Field(default_factory=dict)


class LLMMatcherConfig(BaseModel):
    enabled: bool = True
    llm_config: Optional[LLMConfig] = Field(default_factory=LLMConfig)  # llm配置
    user_prompt: str = ""

    use_history: bool = False
    history_max_turns: int = Field(default=3)


class MultiLayerIntentDetectionConfig(BaseModel):
    """全局配置"""

    rule_matcher_config: RuleMatcherConfig = Field(default_factory=RuleMatcherConfig)
    query_matcher_config: QueryMatcherConfig = Field(default_factory=QueryMatcherConfig)
    semantic_matcher_config: SemanticMatcherConfig = Field(
        default_factory=SemanticMatcherConfig
    )
    llm_matcher_config: LLMMatcherConfig = Field(default_factory=LLMMatcherConfig)
    candidate_layer: str = (
        "semantic"  # 指定LLMMatcher使用哪一层召回的topk意图作为候选意图
    )
    candidate_top_k: int = Field(default=10)  # 候选层召回的候选意图数量
    # 意图包名
    intent_pkgs: List[str] = Field(default_factory=list)

    @classmethod
    def from_dict(
        cls, config_dict: Dict[str, Any]
    ) -> "MultiLayerIntentDetectionConfig":
        """
        Args:
            config_dict: 配置项

        Returns:
            MultiLayerIntentDetectionConfig: 配置项
        """
        return cls.model_validate(config_dict)
