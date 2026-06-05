# -*- coding: utf-8 -*-
# Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
"""utils of IR"""

import copy
from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum
from typing import Dict, Union

from jiuwen.common.log.base import logger


class SupportedUpstreamComponent(Enum):
    QUESTIONER = "Questioner"
    INTENT_DETECTION = "IntentDetection"
    DEFAULT = "default"


@dataclass
class ComponentInfo:
    """component info"""

    component_id: str
    component_type: str
    component_config: dict
    index: int
    component_enum_type: SupportedUpstreamComponent = SupportedUpstreamComponent.DEFAULT


class BranchComponentInserter:
    """Utils of insert branch component in Dag"""

    BoolExpressionInBranchConfig = {
        SupportedUpstreamComponent.QUESTIONER.value: "${<ComponentId>.optionId} == '<id>'",
        SupportedUpstreamComponent.INTENT_DETECTION.value: "${<ComponentId>.classificationId} == '<id>'",
    }

    def __init__(self):
        """initialization of BranchComponentInserter"""
        self.upstream_component_type = [
            SupportedUpstreamComponent.QUESTIONER,
            SupportedUpstreamComponent.INTENT_DETECTION,
        ]
        self.component_handler: Dict[str, ComponentProcessHandler] = dict(
            Questioner=QuestionerHandler()
        )
        self.components_to_be_handled: Dict[str, ComponentInfo] = dict()
        self.upstream_component_dict: Dict[str, ComponentInfo] = dict()
        self.upstream_map_branch_component: Dict[str, str] = dict()

    def run(self, ori_ir: Union[dict, None], **kwargs) -> Union[dict, None]:
        """entry of execution"""
        if ori_ir is None or not isinstance(ori_ir, dict):
            return None
        result: dict = copy.deepcopy(ori_ir)

        components_list = result.get("components", list())
        self._register_components_to_be_handled(components_list)

        self._search_supported_upstream_components(components_list)

        self._insert_branch_component(components_list)

        self._reconnect_edge_for_new_branch_component(result)

        self._handle_component(components_list, result)

        return result

    def _handle_component(self, components_list, result):
        if not self.components_to_be_handled:
            return

        for _, component_info in self.components_to_be_handled.items():
            handler = self.component_handler.get(
                component_info.component_enum_type.value
            )
            if handler is not None:
                index = component_info.index
                new_component = handler.handle(components_list[index])
                components_list.pop(index)
                components_list.insert(index, new_component)
        result.update(dict(components=components_list))

    def _insert_branch_component(self, components_list):
        for _, component_info in self.upstream_component_dict.items():
            new_branch_component = self._create_branch_component(component_info)
            if new_branch_component:
                components_list.append(new_branch_component)

    def _reconnect_edge_for_new_branch_component(self, result):
        if not self.upstream_map_branch_component:
            return

        ir_connections = copy.deepcopy(result.get("connections", list()))
        for item in ir_connections:
            ori_source = item.get("source", dict()).get("componentId", "")
            if ori_source and ori_source in self.upstream_map_branch_component:
                branch_id_to_replace = self.upstream_map_branch_component.get(
                    ori_source
                )
                item["source"].update(dict(componentId=branch_id_to_replace))

        for source_id, dest_id in self.upstream_map_branch_component.items():
            ir_connections.append(
                dict(
                    source=dict(componentId=source_id), target=dict(componentId=dest_id)
                )
            )

        result.update(dict(connections=ir_connections))

    def _create_branch_component(self, component_info):
        upstream_component_id = component_info.component_id
        uid = upstream_component_id[max(0, len(upstream_component_id) - 6) :]
        switch_component_id = f"Switch{uid}"
        branch_component = dict(
            id=switch_component_id,
            type="jiuwen.switchCase",
            configs=dict(debugType=dict(debugTree=False)),
        )

        branches_config = []
        component_branches_config = component_info.component_config.get(
            "branches", list()
        )
        if component_branches_config:
            for index in range(1, len(component_branches_config)):
                bool_expression = self.BoolExpressionInBranchConfig.get(
                    component_info.component_enum_type.value
                )
                if bool_expression:
                    bool_expression = bool_expression.replace(
                        "<ComponentId>", component_info.component_id
                    ).replace("<id>", str(index))
                    branches_config.append(
                        dict(branchId=f"branch_{index}", boolExpression=bool_expression)
                    )

            branches_config.append(dict(branchId="default"))

        if branches_config:
            branch_component.update(dict(branches=branches_config))
            self.upstream_map_branch_component.update(
                {component_info.component_id: switch_component_id}
            )
            return branch_component
        return dict()

    def _search_supported_upstream_components(self, components_list):
        if components_list is None or not isinstance(components_list, list):
            return

        for index, component in enumerate(components_list):
            if not isinstance(component, dict):
                continue
            component_configs = component.get("configs", dict())
            component_enum_type: SupportedUpstreamComponent = (
                SupportedUpstreamComponent.DEFAULT
            )
            component_type = component.get("type")
            if component_type is None or not isinstance(component_type, str):
                continue
            if SupportedUpstreamComponent.QUESTIONER.value in component_type:
                if component_configs.get(
                    "responseType", ""
                ) == "reply_based_on_options" and component_configs.get("branches"):
                    component_enum_type = SupportedUpstreamComponent.QUESTIONER
            elif SupportedUpstreamComponent.INTENT_DETECTION.value in component_type:
                if component_configs.get("branches"):
                    component_enum_type = SupportedUpstreamComponent.INTENT_DETECTION

            if component_enum_type in self.upstream_component_type:
                component_id = component.get("id")
                if component_id is not None:
                    self.upstream_component_dict.update(
                        {
                            component_id: ComponentInfo(
                                component_id=component_id,
                                component_type=component_type,
                                component_config=component_configs,
                                index=index,
                                component_enum_type=component_enum_type,
                            )
                        }
                    )

    def _register_components_to_be_handled(self, components_list):
        if not isinstance(components_list, list):
            return

        for index, component in enumerate(components_list):
            if not isinstance(component, dict):
                continue
            component_type = component.get("type")
            if component_type is None or not isinstance(component_type, str):
                continue
            component_configs = component.get("configs", dict())
            component_id = component.get("id")
            if (
                SupportedUpstreamComponent.QUESTIONER.value in self.component_handler
                and SupportedUpstreamComponent.QUESTIONER.value in component_type
            ):
                self.components_to_be_handled.update(
                    {
                        component_id: ComponentInfo(
                            component_id=component_id,
                            component_type=component_type,
                            component_config=component_configs,
                            index=index,
                            component_enum_type=SupportedUpstreamComponent.QUESTIONER,
                        )
                    }
                )


class ComponentProcessHandler(ABC):
    """abstract interface of Component Processor"""

    @abstractmethod
    def handle(self, component_ir: dict) -> dict:
        """interface of handler"""
        raise NotImplementedError()


class QuestionerHandler(ComponentProcessHandler):
    """implementation of Questioner Component Processor"""

    def handle(self, component_ir: dict) -> dict:
        """implementation of handler"""
        result = copy.deepcopy(component_ir)

        self._add_reference_to_inputs_user_fields(result)

        return result

    def _add_reference_to_inputs_user_fields(self, component_ir: dict):
        """add refValue in config's extractedFields, to inputs' userField"""
        if not component_ir.get("configs", dict()).get("isExtractedFields", False):
            return

        ori_inputs = copy.deepcopy(component_ir.get("inputs", dict()))

        extract_fields_config = component_ir.get("configs", dict()).get(
            "extractedFields", list()
        )
        extract_key_and_ref_value = dict()
        new_extract_fields_config = list()
        for item in extract_fields_config:
            field_name = item.get("fieldName")
            ref_value = item.get("refValue")
            field_type = item.get("fieldType")
            if field_name and ref_value:
                format_field_name = f"X1111X{field_name}"
                extract_key_and_ref_value.update({format_field_name: ref_value})
                new_extract_fields_config.append(
                    dict(id=format_field_name, type=field_type, sourceType="ref")
                )

        if extract_key_and_ref_value:
            try:
                ori_inputs["userFields"].update(extract_key_and_ref_value)
                component_ir.update(dict(inputs=ori_inputs))
                component_ir["configs"]["userFields"]["inputs"].extend(
                    new_extract_fields_config
                )
            except KeyError as e:
                logger.error(
                    f"Failed to add refValue in config's extractedFields, to inputs' userField,"
                    f"root cause = {type(e).__name__}"
                )
