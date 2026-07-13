# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""Rails config"""

import os
from collections import defaultdict
from typing import List, Optional, Dict, Any, Union

import yaml
from jiuwen.common.security.util.fileutil import validate_path_traversal
from jiuwen.planner.rails.common.constants import TriggerType, BuiltinTrigger
from pydantic import Field, BaseModel


class TriggerActionMapping(BaseModel):
    """Configuration of input rails."""

    name: str = ""
    actions: List[str] = Field(
        default_factory=list,
        description="The mapping for one trigger to many actions",
    )


class RailsMetaData(BaseModel):
    """Configuration of input rails."""

    triggers: List[TriggerActionMapping] = Field(
        default_factory=list,
        description="The list for input rails triggers",
    )

    @property
    def trigger_names(self) -> List[str]:
        """返回所有触发器名称的列表"""
        return [mapping.name for mapping in self.triggers]

    @property
    def all_actions(self) -> List[str]:
        """Returns all unique actions from all triggers."""
        action_set = set()
        for trigger in self.triggers:
            for action in trigger.actions:
                action_set.add(action)
        return list(action_set)


class ActionModelConfig(BaseModel):
    model: Optional[str] = Field(
        default="",
        description="The model name for action",
    )
    type: Optional[str] = Field(
        default="",
        description="The model type for action",
    )
    url: Optional[str] = Field(
        default="",
        description="The model url for action",
    )
    api_key: Optional[str] = Field(
        default="",
        description="The model api_key for action",
    )


class ActionConfig(BaseModel):
    """Configuration of specific action."""

    model: Optional[ActionModelConfig] = Field(
        default=None,
        description="Allows choosing between different model names.",
    )

    few_shot: Optional[str] = Field(
        default=None,
        description="Allows choosing between different few shots.",
    )

    prompt: Optional[List[Dict]] = Field(
        default=None,
        description="Allows choosing between different prompting strategies.",
    )

    action_extra_args: Optional[Dict] = Field(
        default=None,
        description="Allows choosing between different prompting strategies.",
    )


class ActionMetadata(BaseModel):
    """action's metadata, parsed from config file"""

    name: str
    config: ActionConfig = None


class TriggerConfig(BaseModel):
    """config of trigger, parsed from config file"""

    trigger_type: TriggerType
    default_trigger: bool
    keywords: List[str]
    exclude_keywords: List[str] = []
    intent: str = ""

    @staticmethod
    def check_trigger_type(trigger_type: str) -> TriggerType:
        """check trigger type"""
        for _, member_value in TriggerType.__members__.items():
            if member_value.value == trigger_type:
                res = member_value
                break
        else:
            raise ValueError(f"Trigger type [{trigger_type}] is undefined!")
        return res


class RailsConfigMetadata(BaseModel):
    """Configuration of specific rails."""

    input: RailsMetaData = Field(
        default_factory=RailsMetaData, description="Configuration of the input rails."
    )
    output: RailsMetaData = Field(
        default_factory=RailsMetaData, description="Configuration of the output rails."
    )
    execution: RailsMetaData = Field(
        default_factory=RailsMetaData,
        description="Configuration of the execution rails.",
    )

    @property
    def all_actions(self) -> List[str]:
        """Returns all unique actions from both input and output."""
        input_actions = set(self.input.all_actions)
        output_actions = set(self.output.all_actions)
        execution_actions = set(self.execution.all_actions)
        return list(input_actions | output_actions | execution_actions)


class RailsConfig(BaseModel):
    """Configuration object for the actions, triggers and the rails."""

    rails: RailsConfigMetadata = Field(
        default_factory=RailsConfigMetadata,
        description="Configuration for the various rails (input, output, etc.).",
    )

    actions: Optional[Dict[str, ActionConfig]] = Field(
        default_factory=None,
        description="Configuration for the action configs",
    )

    triggers: Optional[Dict[str, TriggerConfig]] = Field(
        default_factory=None,
        description="Configuration for the action configs",
    )

    @classmethod
    def config_file_dir(
        cls, config_path: str, config_dict: dict = None
    ) -> "RailsConfig":
        """Loads a configuration from a given path.

        Supports loading a from a single file, or from a directory.
        """
        if not config_dict:
            return cls._parse_yaml_config(config_path)
        # 处理json格式的配置
        return cls._parse_json_config(config_dict)

    @classmethod
    def _parse_yaml_config(cls, config_path):
        validate_path_traversal(config_path)
        config_file_path = os.path.join(config_path, "config.yml")
        if os.path.exists(config_file_path) and (
            config_file_path.endswith(".yaml") or config_file_path.endswith(".yml")
        ):
            with open(config_file_path, "r", encoding="utf-8") as f:
                config_data = yaml.safe_load(f)
        else:
            raise ValueError(f"rail config path: {config_file_path} is not existed.")
        # Extract rails metadata
        rails_data = config_data.get("rails", {})
        rails_config_metadata = cls._create_rails_metadata(rails_data)
        # Extract action configs
        actions = cls._create_action_configs(
            rails_config_metadata.all_actions, config_data
        )
        # Extract triggers
        triggers = cls._create_trigger_data(config_data)
        return cls(rails=rails_config_metadata, actions=actions, triggers=triggers)

    @classmethod
    def _create_trigger_data(cls, config_data):
        triggers_data = config_data.get("triggers_config", {})
        triggers = {}
        for trigger_name, trigger_properties in triggers_data.items():
            trigger_config = TriggerConfig(
                trigger_type=TriggerConfig.check_trigger_type(
                    trigger_properties.get("type", "")
                ),
                default_trigger=False,  # Assuming a default value
                keywords=trigger_properties.get("keywords", ""),
                exclude_keywords=trigger_properties.get("exclude_keywords", []),
            )
            triggers[trigger_name] = trigger_config

        cls._append_builtin_triggers(triggers)

        return triggers

    @classmethod
    def _create_action_configs(cls, all_actions, config_data):
        action_configs = config_data.get("actions_config", {})
        actions = {}
        for action_name in all_actions:
            action_properties = action_configs.get(action_name, {})
            actions[action_name] = ActionConfig(
                model=ActionModelConfig(**action_properties.get("model", {})),
                prompt=action_properties.get("prompt", []),
                few_shot=action_properties.get("few_shot", ""),
                action_extra_args=action_properties.get("action_extra_args", {}),
            )
        return actions

    @classmethod
    def _create_action_json_configs(cls, all_actions, config_data):
        action_configs = config_data.get("actions_config", [])
        actions = {}
        for action_name in all_actions:
            config = cls._get_action_config_by_name(action_configs, action_name)
            actions[action_name] = ActionConfig(
                model=ActionModelConfig(**config.get("model", {})),
                prompt=config.get("prompt", []),
                few_shot=config.get("few_shot", ""),
                action_extra_args=config.get("action_extra_args", {}),
            )
        return actions

    @classmethod
    def _get_action_config_by_name(cls, action_configs, action_name):
        config = {}
        for action_config in action_configs:
            if action_config["action"] == action_name:
                return action_config
        return config

    @classmethod
    def _create_rails_metadata(cls, rails_data):
        # 解析 rails
        input_triggers = []
        for item in rails_data.get("input", []):
            if isinstance(item, dict):
                for k, v in item.items():
                    input_triggers.append(TriggerActionMapping(name=k, actions=v))
            elif isinstance(
                item, str
            ):  # 支持不配置trigger、直接配置Action，框架提供一种默认打开的Trigger：_always_on_trigger
                input_triggers.append(
                    TriggerActionMapping(
                        name=BuiltinTrigger.ALWAYS_ON.value,
                        actions=rails_data.get("input"),
                    )
                )
                break
            else:
                raise ValueError("Failed to parse input rails config")
        output_triggers = []
        for item in rails_data.get("output", []):
            if isinstance(item, dict):
                for k, v in item.items():
                    output_triggers.append(TriggerActionMapping(name=k, actions=v))
            elif isinstance(
                item, str
            ):  # 支持不配置trigger、直接配置Action，框架提供一种默认打开的Trigger：_always_on_trigger
                output_triggers.append(
                    TriggerActionMapping(
                        name=BuiltinTrigger.ALWAYS_ON.value,
                        actions=rails_data.get("output"),
                    )
                )
                break
            else:
                raise ValueError("Failed to parse output rails config")
        execution_triggers = []
        for item in rails_data.get("execution", []):
            if isinstance(item, dict):
                for k, v in item.items():
                    execution_triggers.append(TriggerActionMapping(name=k, actions=v))
            elif isinstance(
                item, str
            ):  # 支持不配置trigger、直接配置Action，框架提供一种默认打开的Trigger：_always_on_trigger
                execution_triggers.append(
                    TriggerActionMapping(
                        name=BuiltinTrigger.ALWAYS_ON.value,
                        actions=rails_data.get("execution"),
                    )
                )
                break
            else:
                raise ValueError("Failed to parse execution rails config")
        # Create InputRailsMetaData and OutputRailsMetaData
        input_metadata = RailsMetaData(triggers=input_triggers)
        output_metadata = RailsMetaData(triggers=output_triggers)
        execution_metadata = RailsMetaData(triggers=execution_triggers)
        # Create RailsMetadata
        rails_metadata = RailsConfigMetadata(
            input=input_metadata, output=output_metadata, execution=execution_metadata
        )
        return rails_metadata

    @classmethod
    def _parse_json_config(cls, config_dict: Dict[str, Any]) -> "RailsConfig":
        # 解析 rails 配置
        input_metadate = cls._generate_rails_metadata_by_type("input", config_dict)
        output_metadata = cls._generate_rails_metadata_by_type("output", config_dict)
        execution_metadate = cls._generate_rails_metadata_by_type(
            "execution", config_dict
        )

        rails_metadata = RailsConfigMetadata(
            input=input_metadate, outout=output_metadata, execution=execution_metadate
        )

        # 解析 actions 配置
        # Extract action configs
        actions = cls._create_action_json_configs(
            rails_metadata.all_actions, config_dict
        )

        # 解析 triggers 配置
        triggers = {}
        for trigger_config in config_dict.get("triggers_config", []):
            trigger_name = trigger_config["name"]
            triggers[trigger_name] = TriggerConfig(
                trigger_type=TriggerConfig.check_trigger_type(trigger_config["type"]),
                default_trigger=False,  # 假设默认值为 False
                keywords=trigger_config.get("keywords", []),
                exclude_keywords=trigger_config.get("exclude_keywords", []),
                intent="",  # 示例中没有提供 intent，所以使用默认值
            )

        # 创建并返回 RailsConfig 对象
        return RailsConfig(rails=rails_metadata, actions=actions, triggers=triggers)

    @classmethod
    def _generate_rails_metadata_by_type(cls, rail_type, config_dict):
        rail_metadata = RailsMetaData()
        trigger_action_mapping = defaultdict(list)
        for item in config_dict.get("rails", {}).get(rail_type, []):
            trigger = item.get("trigger", "")
            action = item["action"]
            trigger_action_mapping[trigger].append(action)
        rail_metadata.triggers = [
            TriggerActionMapping(name=trigger, actions=actions)
            for trigger, actions in trigger_action_mapping.items()
        ]
        return rail_metadata

    @classmethod
    def _append_builtin_triggers(cls, triggers: dict):
        _builtin_triggers = [member.value for member in BuiltinTrigger]
        for trigger_name in _builtin_triggers:
            if trigger_name not in triggers:
                trigger_config = cls._create_builtin_trigger_by_name(trigger_name)
                if trigger_config is not None:
                    triggers.update({trigger_name: trigger_config})

    @classmethod
    def _create_builtin_trigger_by_name(
        cls, trigger_name: str
    ) -> Union[TriggerConfig, None]:
        trigger_config = None

        if trigger_name == BuiltinTrigger.ALWAYS_ON.value:
            trigger_config = TriggerConfig(
                trigger_type=TriggerType.DefaultTrigger,
                default_trigger=True,
                keywords=list(),
            )
        return trigger_config
