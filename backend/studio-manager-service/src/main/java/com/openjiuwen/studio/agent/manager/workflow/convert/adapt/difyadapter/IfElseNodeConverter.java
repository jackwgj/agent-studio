/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVOConditions;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IfElseNodeConverter extends AbstractSDSLNodeConverter{

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.BRANCH.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.BRANCH.getType());
        Map<String, String> branchMap = new HashMap<>();
        workflowNodeVO.setConfigs(adaptConfigs(data, branchMap));
        workflowNodeVO.setBranches(adaptBranches(data, branchMap, workflowNodeVO, workflowNodeMap));
        return workflowNodeVO;
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowBranchVO> adaptBranches(Map<String, Object> data, Map<String, String> branchMap, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        List<WorkflowBranchVO> branches = new ArrayList<>();
        List<Map<String, Object>> casesMap = (List<Map<String, Object>>) data.get("cases");
        int branchCount = 1;
        if (casesMap != null) {
            for (Map<String, Object> caseData : casesMap) {
                WorkflowBranchVO workflowBranchVO = new WorkflowBranchVO();
                workflowBranchVO.setId("true".equalsIgnoreCase(caseData.get("case_id").toString()) ? "if" : "elseIf-" + branchCount++);

                Map<String, Object> configsMap = new HashMap<>();
                List<Map<String, Object>> conditionMaps = (List<Map<String, Object>>) caseData
                        .get("conditions");
                if (CollectionUtils.isEmpty(conditionMaps)) {
                    // 默认conditions
                    configsMap.put("logic", WorkflowBranchConfigVO.LogicEnum.AND);
                    configsMap.put("conditions", generateDefaultConditions());
                } else {
                    // 适配conditions
                    configsMap.put("logic", WorkflowBranchConfigVO.LogicEnum.fromValue((String) caseData.get("logical_operator")));
                    configsMap.put("conditions", adaptConditions(conditionMaps, workflowNodeVO, workflowNodeMap));
                }
                workflowBranchVO.setConfigs(configsMap);
                branches.add(workflowBranchVO);
                branchMap.put(caseData.get("case_id").toString(), workflowBranchVO.getId());
            }
        }

        // 适配默认分支
        branches.add(new WorkflowBranchVO().setId("default"));
        branchMap.put("false", "default");
        return branches;
    }

    @SuppressWarnings("unchecked")
    private List<WorkflowBranchConfigVOConditions> adaptConditions(List<Map<String, Object>> conditionMaps, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        List<WorkflowBranchConfigVOConditions> configVOConditions = new ArrayList<>();
        conditionMaps.forEach(conditionMap -> {
            WorkflowBranchConfigVOConditions workflowBranchConfigVOConditions = new WorkflowBranchConfigVOConditions();
            String operator = conditionMap.get("comparison_operator").toString();
            workflowBranchConfigVOConditions.setOperator(OPERATOR_MAP.getOrDefault(operator, ""));
            List<String> selector = (List<String>) conditionMap.get("variable_selector");
            if ("env".equalsIgnoreCase(selector.get(0))) {
                configVOConditions.add(getDefaultCondition());
            } else {
                // 适配左值
                WorkflowFieldVO leftFieldVO = getBranchFieldVO(conditionMap, workflowNodeVO, selector);
                leftFieldVO.setValue(adaptWorkflowFieldVoValue(selector, workflowNodeMap, workflowNodeVO));
                workflowBranchConfigVOConditions.setLeft(leftFieldVO);
                // 适配右边条件
                WorkflowFieldVO rightFieldVO = getBranchFieldVO(conditionMap, workflowNodeVO, selector);
                String value = conditionMap.get("value").toString();
                rightFieldVO.setValue(extractValueToField(value, workflowNodeVO, workflowNodeMap));
                workflowBranchConfigVOConditions.setRight(rightFieldVO);
                configVOConditions.add(workflowBranchConfigVOConditions);
            }
        });
        return configVOConditions;
    }

    public List<WorkflowBranchConfigVOConditions> generateDefaultConditions() {
        List<WorkflowBranchConfigVOConditions> conditions = new ArrayList<>();
        conditions.add(getDefaultCondition());
        return conditions;
    }

    public WorkflowBranchConfigVOConditions getDefaultCondition() {
        return new WorkflowBranchConfigVOConditions()
                .setLeft(new WorkflowFieldVO().setSource(WorkflowFieldVO.SourceEnum.USER)
                        .setRequired(false)
                        .setValue(new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.REF)
                                .setContent(Map.of("ref_node_id", "", "ref_var_name", "", "source", "")))
                )
                .setOperator("eq")
                .setRight(new WorkflowFieldVO().setSource(WorkflowFieldVO.SourceEnum.USER)
                        .setRequired(false)
                        .setValue(new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                                .setContent(""))
                );
    }

    public Map<String, Object> adaptConfigs(Map<String, Object> data, Map<String, String> branchMap) {
        Map<String, Object> configsMap = new HashMap<>();
        configsMap.put(CommonConstant.DIFY.CONFIG_DEFAULT_NAME, false);
        configsMap.put("branchMap", branchMap);
        return configsMap;
    }
}
