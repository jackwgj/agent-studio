#!/usr/bin/env python
# coding=utf-8
# Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
from typing import Dict, List, Any

from jiuwen.common.exception import JiuWenBaseException
from jiuwen.common.exception.status_code import StatusCode
from pydantic import BaseModel, Field

USER_PROFILE_KEY = "userProfile"
ENABLE_KEY = "enable"
USER_PROFILE_TOPICS_KEY = "topics"


DEFAULT_USER_PROFILE_CONFIG = {ENABLE_KEY: False, USER_PROFILE_TOPICS_KEY: []}


class MemoryIrConfig(BaseModel):
    """memory config definition"""

    enable_memory: bool = False  # enable memory from memory ir config
    enable_user_profile: bool = False
    user_profile_topics: List[Dict[str, Any]] = Field(default=[])

    @staticmethod
    def _get_and_validate_user_profile(user_profile: dict):
        """get and validate user profile"""
        if not isinstance(user_profile, dict):
            raise JiuWenBaseException(
                error_code=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.code,
                message=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.errmsg.format(
                    error_msg="Invalid userProfile."
                ),
            )
        user_profile_enable = user_profile.get(ENABLE_KEY, False)
        if not isinstance(user_profile_enable, bool):
            raise JiuWenBaseException(
                error_code=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.code,
                message=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.errmsg.format(
                    error_msg="Invalid userProfile enable."
                ),
            )
        user_profile_topics = user_profile.get(USER_PROFILE_TOPICS_KEY, [])
        if not isinstance(user_profile_topics, list):
            raise JiuWenBaseException(
                error_code=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.code,
                message=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.errmsg.format(
                    error_msg="Invalid tags value in userProfile configuration."
                ),
            )

        return user_profile_enable, user_profile_topics

    @classmethod
    def from_config_dict(cls, memory_config: Dict[str, Any]):
        """construct memory config from a dict
        Args:
            memory_config (Dict[str, Any]): a dict of ir config
        Returns:
            MemoryIrConfig
        """
        if not isinstance(memory_config, dict):
            raise JiuWenBaseException(
                error_code=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.code,
                message=StatusCode.MEMORY_ENGINE_IR_CONFIG_ERROR.errmsg.format(
                    error_msg="Invalid memory config which should be a dict."
                ),
            )
        if not memory_config:
            return cls()
        # parse user profile config from memory config
        enable_memory = memory_config.get(ENABLE_KEY, False)
        # New IR structure: if memory_repo_id or strategies present, enable memory
        # even when "enable" field is missing (new IR sets enable=true only for
        # LLM node injection, not for the overall memory switch).
        if not enable_memory:
            memory_repo_id = memory_config.get("memory_repo_id", "")
            strategies = memory_config.get("strategies") or []
            if memory_repo_id or strategies:
                enable_memory = True
        user_profile = memory_config.get(USER_PROFILE_KEY, DEFAULT_USER_PROFILE_CONFIG)
        enable_user_profile, user_profile_topics = cls._get_and_validate_user_profile(
            user_profile
        )
        # For new IR structure without userProfile, enable user_profile when
        # strategies contain user_profile type.
        if not enable_user_profile:
            strategies = memory_config.get("strategies") or []
            strategy_types = {s.get("type") for s in strategies if isinstance(s, dict)}
            if "user_profile" in strategy_types or strategy_types:
                enable_user_profile = True
        return cls(
            enable_memory=enable_memory,
            enable_user_profile=enable_user_profile,
            user_profile_topics=user_profile_topics,
        )
