/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.enums.TypeEnum;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.enums.OperatorType;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;

import org.apache.commons.lang3.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分支节点转换IR
 *
 */
public class BranchNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 分支模板
     */
    private static final String BRANCH_FORMAT = "%s-%s";

    /**
     * 分支
     */
    private static final String BRANCHES = "branches";

    /**
     * 分支判断表达式
     */
    private static final String BOOL_EXPRESSION = "boolExpression";

    private Map<String, Object> adaptBranch(WorkflowBranchVO workflowBranchVo, String nodeId) {
        Map<String, Object> workflowBranch = new HashMap<>();

        // 默认节点无需sourceId 前缀
        if (Strings.CS.equals(CommonConstant.DEFAULT_BRANCH_ID, workflowBranchVo.getId())) {
            workflowBranch.put("id", workflowBranchVo.getId());
            return workflowBranch;
        }
        workflowBranch.put("id", String.format(BRANCH_FORMAT, nodeId, workflowBranchVo.getId()));
        WorkflowBranchConfigVO workflowBranchConfig =
            JSON.parseObject(JSON.toJSONString(workflowBranchVo.getConfigs(), JSONWriter.Feature.WriteMapNullValue),
                WorkflowBranchConfigVO.class);
        workflowBranch.put(BOOL_EXPRESSION, adaptExpression(workflowBranchConfig));
        return workflowBranch;
    }

    /**
     * 适配表达式
     *
     * @param branchConfig 工作流分支配置
     * @return 工作流分支
     */
    public String adaptExpression(WorkflowBranchConfigVO branchConfig) {
        String operator = branchConfig.getLogic().toString();
        List<String> expressions = branchConfig.getConditions()
            .stream()
            .map(m -> adaptExpression(
                adaptExpressionValue(m.getLeft().getValue(), m.getLeft().getType(), m.getOperator()),
                m.getRight() != null
                    ? adaptExpressionValue(m.getRight().getValue(), m.getRight().getType(), m.getOperator()) : null,
                m.getOperator()))
            .toList();
        String boolExpression;
        if (Strings.CS.equals(operator, "or")) {
            boolExpression = String.join(" || ", expressions);
        } else {
            boolExpression = String.join(" && ", expressions);
        }
        return boolExpression;
    }

    private String adaptExpression(String left, String right, String operator) {
        for (OperatorType type : OperatorType.values()) {
            if (Strings.CS.equals(operator, type.getEiType())) {
                return formatExpression(left, right, type);
            }
        }
        throw new AgentStudioException(StudioError.WORKFLOW_UNEXPECTED_OPERATOR, operator);
    }

    private String adaptExpressionValue(WorkflowFieldVOValue workflowFieldValue, String fieldType, String operator) {
        String type = WorkflowUtils.formatWorkflowType(fieldType);
        String expressionValue = adaptBranchValue(workflowFieldValue, type);

        // string和array类型进行长度比较添加length
        if (operator.contains("than") && (Strings.CS.equals(type, TypeEnum.ARRAY.toString())
            || Strings.CS.equals(type, TypeEnum.STRING.toString()))) {
            expressionValue = String.format("length(%s)", expressionValue);
        }
        return expressionValue;
    }

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(IRUtils.adaptKey(BRANCHES),
            workflowNodeVo.getBranches()
                .stream()
                .map(m -> adaptBranch(m, workflowNodeVo.getId()))
                .collect(Collectors.toList()));
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.BRANCH.getIrType();
    }

    private String formatExpression(String left, String right, OperatorType type) {
        if (right == null) {
            return String.format(type.getTemplate(), left);
        } else {
            if (type.isReverse()) {
                return String.format(type.getTemplate(), right, left);
            } else {
                return String.format(type.getTemplate(), left, right);
            }
        }
    }

    private String adaptBranchValue(WorkflowFieldVOValue workflowFieldValue, String fieldType) {
        if (workflowFieldValue.getType().equals(WorkflowFieldVOValue.TypeEnum.REF)) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> refMap =
                objectMapper.convertValue(workflowFieldValue.getContent(), new TypeReference<>() {});
            return adaptValue(refMap.get(CommonConstant.Workflow.REF_NODE_ID),
                refMap.get(CommonConstant.Workflow.REF_VAR_NAME), refMap.get("source"));
        } else if (workflowFieldValue.getType().equals(WorkflowFieldVOValue.TypeEnum.LITERAL)) {
            // 兼容老版本未传类型情况
            boolean isStringType = fieldType == null || Strings.CS.equals(fieldType, TypeEnum.STRING.toString());
            return isStringType ? String.format("'%s'", workflowFieldValue.getContent().toString())
                : workflowFieldValue.getContent().toString();
        } else {
            throw new AgentStudioException(StudioError.WORKFLOW_BRANCH_FIELD_VALUE);
        }
    }
}
