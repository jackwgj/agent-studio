/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.models.SetVariableConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设置变量节点转换IR
 *
 */
public class SetVariableNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * settings
     */
    private static final String SETTINGS = "settings";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        Map<String, Object> configs = new HashMap<>();

        List<Map<String, Map<String, Object>>> settings = new ArrayList<>();
        if (nodeConfigs.get(SETTINGS) != null) {
            List<SetVariableConfig> variables =
                JsonUtils.objectToClassRef(nodeConfigs.get(SETTINGS), new TypeReference<List<SetVariableConfig>>() {});
            if (variables == null) {
                throw new AgentStudioException(StudioError.CONVERT_NODE_FAILED_INVALID_LENGTH);
            }
            for (SetVariableConfig variable : variables) {
                Map<String, Map<String, Object>> setting = new HashMap<>();
                setting.put("left", parseSettingField(variable.getLeft(), "left"));
                setting.put("right", parseSettingField(variable.getRight(), "right"));
                settings.add(setting);
            }
        }
        configs.put(SETTINGS, settings);
        return configs;
    }

    private Map<String, Object> parseSettingField(WorkflowFieldVO field, String side) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", field.getType());
        result.put("value", adaptFieldValue(field, NodeType.SET_VARIABLE, false));

        // 根据 side 参数执行不同的逻辑
        if ("right".equals(side)) {
            String operatorStr = field.getValue().getOperator() != null ? field.getValue().getOperator().toString() : null;
            boolean isValidOperator = isValidOperator(operatorStr);

            result.put("operator", isValidOperator ? operatorStr : null);
        }
        if (field.getSchema() != null) {
            result.put("schema", adaptSchema(field.getType(), field.getSchema(), 0,
                field.getSource().equals(WorkflowFieldVO.SourceEnum.SYSTEM)));
        }
        return result;
    }

    private boolean isValidOperator(String operatorStr) {
        if (operatorStr == null) {
            return false;
        }
        return operatorStr.equals(WorkflowFieldVOValue.OperatorEnum.INCREMENT.toString()) ||
                operatorStr.equals(WorkflowFieldVOValue.OperatorEnum.DECREMENT.toString()) ||
                operatorStr.equals(WorkflowFieldVOValue.OperatorEnum.EMPTY.toString()) ||
                operatorStr.equals(WorkflowFieldVOValue.OperatorEnum.EMPTY_STR.toString()) ||
                operatorStr.equals(WorkflowFieldVOValue.OperatorEnum.EMPTY_ARR.toString());
    }

    @Override
    public String getNodeType() {
        return NodeType.SET_VARIABLE.getIrType();
    }
}
