# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""Rails runtime"""

from typing import List, Dict, Any

from jiuwen.common.log.base import logger
from jiuwen.planner.rails.common.constants import (
    RailsType,
    Input_Keywords_Map,
    Output_Keywords_Map,
)
from jiuwen.planner.rails.config import RailsConfig, TriggerConfig, TriggerActionMapping
from jiuwen.planner.rails.runtime.action_manager import RuntimeActionManager
from jiuwen.planner.rails.runtime.context import RailsContext
from jiuwen.planner.rails.schema.action import ActionMetadata, Action
from jiuwen.planner.rails.schema.trigger import Trigger


class RailsRuntime:
    def __init__(self, rails_config: RailsConfig, rail_type: RailsType):
        self._action_manager = RuntimeActionManager(rails_config)
        self._rails_metadata = rails_config.rails
        self._rail_type: RailsType = rail_type
        self._triggers: List[Trigger] = self._create_triggers(rails_config.triggers)
        self._context: RailsContext = RailsContext()
        self._input_keys: List[str] = Input_Keywords_Map.get(self._rail_type.value)
        self._output_keys: List[str] = Output_Keywords_Map.get(self._rail_type.value)

    @staticmethod
    def _exec_single_action(action: Action, context: Dict[str, Any]) -> Dict[str, Any]:
        return action.invoke(context)

    @staticmethod
    async def _aexec_single_action(
        action: Action, context: Dict[str, Any]
    ) -> Dict[str, Any]:
        """异步执行单个 action - 调用 action 的 ainvoke 方法"""
        return await action.ainvoke(context)

    def run(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        """runtime execution: execute trigger and actions"""

        self._on_pre_exec(inputs)

        for trigger in self._triggers:
            current_context = self._context.var_space
            if trigger.should_trigger(current_context):
                logger.info("Begin to trigger actions!")
                action_output = self._trigger_action(
                    trigger.trigger_include_actions, current_context
                )
                self._context.batch_update(action_output)
        return self._on_post_exec()

    async def arun(self, inputs: Dict[str, Any]) -> Dict[str, Any]:
        """异步版本"""
        self._on_pre_exec(inputs)

        for trigger in self._triggers:
            current_context = self._context.var_space
            if trigger.should_trigger(current_context):
                logger.info("Begin to trigger actions!")
                action_output = await self._atrigger_action(
                    trigger.trigger_include_actions, current_context
                )
                self._context.batch_update(action_output)

        return self._on_post_exec()

    def _create_triggers(
        self, trigger_candidates: Dict[str, TriggerConfig]
    ) -> List[Trigger]:
        triggers: List[Trigger] = list()
        if self._rail_type == RailsType.InputRails:
            rail = self._rails_metadata.input
        elif self._rail_type == RailsType.ExecutionRails:
            rail = self._rails_metadata.execution
        elif self._rail_type == RailsType.OutputRails:
            rail = self._rails_metadata.output
        else:
            rail = None

        if rail:
            triggers_in_rail = rail.triggers
            triggers_name: List[str] = rail.trigger_names
            triggers = [
                self._exec_create_single_trigger(
                    name, trigger_candidates, triggers_in_rail
                )
                for name in triggers_name
            ]

        return triggers

    def _exec_create_single_trigger(
        self,
        trigger_name,
        trigger_candidates,
        triggers_in_rail: List[TriggerActionMapping],
    ) -> Trigger:
        trigger_include_actions: List[ActionMetadata] = list()
        for item in triggers_in_rail:
            if item.name == trigger_name:
                actions_name: List[str] = item.actions
                for name in actions_name:
                    action = self._action_manager.get_action(name)
                    if action is not None:
                        action_config = action.action_metadata.config
                        action_metadata = ActionMetadata(
                            name=name, config=action_config
                        )
                        trigger_include_actions.append(action_metadata)
                break

        return Trigger(
            trigger_config=trigger_candidates.get(trigger_name),
            trigger_include_actions=trigger_include_actions,
        )

    def _on_pre_exec(self, inputs: Dict[str, Any]):
        """before rails execution, update context from inputs"""
        self._context.batch_update(inputs)

    def _on_post_exec(self):
        """after rails execution, return rails output"""
        output_dict: Dict[str, Any] = dict()
        for key in self._output_keys:
            value = self._context.var_space.pop(key, None)
            if value is not None:
                output_dict.update({key: value})
        self._context.clear()
        return output_dict

    def _trigger_action(
        self, trigger_action: List[ActionMetadata], context_dict: Dict[str, Any]
    ):
        """execute actions belong to one trigger, in serial order"""
        action_output = {}
        for action in trigger_action:
            action_name = action.name
            action = self._action_manager.get_action(action_name)
            if action is None:
                raise ValueError(
                    f"Failed to get an unregistered action [{action_name}]!"
                )
            args = context_dict.get("arguments", {})
            args.update(action_output.get("arguments", {}))
            context_dict["arguments"] = args
            action_output = self._exec_single_action(action, context_dict)
            logger.info(
                f"Success to invoke action [{action_name}], action's output = {type(action_output)}"
            )
        return action_output

    async def _atrigger_action(
        self, trigger_action: List[ActionMetadata], context_dict: Dict[str, Any]
    ):
        """execute actions belong to one trigger, in serial order - 支持同步和异步action"""
        action_output = {}
        for action_metadata in trigger_action:
            action_name = action_metadata.name
            action = self._action_manager.get_action(action_name)
            if action is None:
                raise ValueError(
                    f"Failed to get an unregistered action [{action_name}]!"
                )

            args = context_dict.get("arguments", {})
            args.update(action_output.get("arguments", {}))
            context_dict["arguments"] = args

            # 异步执行单个 action
            action_output = await self._aexec_single_action(action, context_dict)
            logger.info(
                f"Success to invoke action [{action_name}], action's output = {action_output}",
                simple_log="Success to invoke action",
            )
        return action_output
