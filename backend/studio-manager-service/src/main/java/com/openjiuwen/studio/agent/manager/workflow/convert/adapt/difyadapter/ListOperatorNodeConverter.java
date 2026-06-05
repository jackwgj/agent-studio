/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.VariableEnum;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ListOperatorNodeConverter extends AbstractSDSLNodeConverter {

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.LIST_OPERATOR.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.LIST_OPERATOR.getType());
        workflowNodeVO.setInputs(adaptInputs(data, workflowNodeMap));
        workflowNodeVO.setConfigs(adaptConfigs(data));
        workflowNodeVO.setOutputs(adaptOutputs(data));
        return workflowNodeVO;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap) {
        if (data.get("variable") instanceof String) {
            return List.of();
        }
        List<String> variableSelector = (List<String>) data.get("variable");
        if (!CollectionUtils.isEmpty(variableSelector)) {
            WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
            workflowFieldVO.setRequired(false);
            workflowFieldVO.setName(variableSelector.get(1));
            workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.USER);
            workflowFieldVO.setType(VariableEnum.SELECT.getDslType());
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", VariableEnum.fromDifyValue(data.getOrDefault("item_var_type", "string").toString()).getDslType());
            workflowFieldVO.setSchema(schema);
            workflowFieldVO.setValue(adaptWorkflowFieldVoValue(variableSelector, workflowNodeMap, workflowNodeMap.get(variableSelector.get(0))));
            return List.of(workflowFieldVO);
        }
        return List.of();
    }

    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {
        WorkflowFieldVO result = new WorkflowFieldVO()
                .setName("result")
                .setSchema(Map.of(
                        "type", VariableEnum.fromDifyValue(data.getOrDefault("item_var_type", "string").toString()).getDslType()))
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.SELECT.getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));

        WorkflowFieldVO firstRecord = new WorkflowFieldVO()
                .setName("first_record")
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.fromDifyValue(data.getOrDefault("item_var_type", "string").toString()).getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));

        WorkflowFieldVO lastRecord = new WorkflowFieldVO()
                .setName("last_record")
                .setSource(WorkflowFieldVO.SourceEnum.USER)
                .setType(VariableEnum.fromDifyValue(data.getOrDefault("item_var_type", "string").toString()).getDslType())
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                        .setContent(""));
        return List.of(result, firstRecord, lastRecord);
    }
}
