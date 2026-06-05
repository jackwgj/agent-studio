/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowBranchConfigVOConditions;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 循环节点转换
 *
 */
@Component
public class LoopNodeConverter extends AbstractSDSLNodeConverter {

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.LOOP.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.LOOP.getType());
        workflowNodeVO.setInputs(adaptInputs(data, workflowNodeVO, workflowNodeMap));
        workflowNodeVO.setOutputs(adaptOutputs(data, workflowNodeVO, workflowNodeMap));
        workflowNodeVO.setConfigs(adaptConfigs(data, workflowNodeVO, workflowNodeMap, "numLoop"));
        return workflowNodeVO;
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        List<WorkflowFieldVO> inputs = new ArrayList<>();
        // 循环次数适配
        if (ObjectUtils.isNotEmpty(data.get("loop_count"))) {
            WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
            workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED);
            workflowFieldVO.setDescription("循环次数");
            workflowFieldVO.setName("num_loop_var");
            workflowFieldVO.setReflection(false);
            workflowFieldVO.setRequired(true);
            workflowFieldVO.setType("integer");
            workflowFieldVO.setValue(new WorkflowFieldVOValue().setContent(data.get("loop_count"))
                .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                .setHint("")
                .setDefault(""));
            inputs.add(workflowFieldVO);
        }
        // 循环中间变量适配
        if (ObjectUtils.isNotEmpty(data.get("loop_variables"))) {
            WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
            workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED);
            workflowFieldVO.setDescription("循环之间共享参数");
            workflowFieldVO.setName("intermediate_loop_var");
            workflowFieldVO.setRequired(true);
            workflowFieldVO.setType("object");
            workflowFieldVO.setValue(new WorkflowFieldVOValue().setContent(null)
                .setType(WorkflowFieldVOValue.TypeEnum.NESTED)
                .setHint("")
                .setDefault(""));
            List<Map<String, Object>> loopVariables = (List<Map<String, Object>>) data.get("loop_variables");
            List<WorkflowFieldVO> schema = new ArrayList<>();
            loopVariables.forEach(loopVariable -> {
                WorkflowFieldVO fieldVoSchema = new WorkflowFieldVO();
                fieldVoSchema.setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED);
                fieldVoSchema.setName((String) loopVariable.get("label"));
                fieldVoSchema.setRequired(true);
                fieldVoSchema.setType(parseSourceType((String) loopVariable.get("var_type")));
                fieldVoSchema.setSchema(parseSchema((String) loopVariable.get("var_type")));
                if ("variable".equals(loopVariable.get("value_type"))) {
                    List<String> selector = (List<String>) loopVariable.get("value");
                    fieldVoSchema.setValue(adaptWorkflowFieldVoValue(selector, workflowNodeMap, workflowNodeVO));
                } else if ("constant".equals(loopVariable.get("value_type"))) {
                    fieldVoSchema.setValue(new WorkflowFieldVOValue().setContent(loopVariable.get("value"))
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setHint(""));
                }
                schema.add(fieldVoSchema);
            });
            workflowFieldVO.setSchema(schema);
            inputs.add(workflowFieldVO);
        }
        if (ObjectUtils.isNotEmpty(data.get("iterator_selector"))) {
            WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
            workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.PRE_DEFINED);
            workflowFieldVO.setName("arr_loop_var");
            workflowFieldVO.setRequired(true);
            List<String> selector = (List<String>) data.get("iterator_selector");
            setTypeAndSchemaByName(workflowFieldVO, workflowNodeMap.get(selector.get(0)), selector);
            workflowFieldVO.setValue(adaptWorkflowFieldVoValue(selector, workflowNodeMap, workflowNodeVO));
            inputs.add(workflowFieldVO);
        }
        return inputs;
    }

    // Dify循环节点的循环变量也是输出
    @SuppressWarnings("unchecked")
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        // 循环中间变量适配
        List<WorkflowFieldVO> outputs = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(data.get("loop_variables"))) {
            List<Map<String, Object>> loopVariables = (List<Map<String, Object>>) data.get("loop_variables");
            loopVariables.forEach(loopVariable -> {
                WorkflowFieldVO outputFieldVO = new WorkflowFieldVO();
                outputFieldVO.setSource(WorkflowFieldVO.SourceEnum.USER);
                outputFieldVO.setName((String) loopVariable.get("label"));
                outputFieldVO.setRequired(true);
                outputFieldVO.setReflection(false);
                outputFieldVO.setType(parseSourceType((String) loopVariable.get("var_type")));
                outputFieldVO.setSchema(parseSchema((String) loopVariable.get("var_type")));
                Map<String, String> contentRef = new HashMap<>();
                contentRef.put("ref_node_id", workflowNodeVO.getId());
                contentRef.put("ref_var_name", "intermediate_loop_var." + loopVariable.get("label"));
                contentRef.put("source", WorkflowFieldVO.SourceEnum.PRE_DEFINED.toString());
                outputFieldVO.setValue(new WorkflowFieldVOValue().setContent(contentRef)
                    .setType(WorkflowFieldVOValue.TypeEnum.REF)
                    .setHint(""));
                outputs.add(outputFieldVO);
            });
        }
        if (ObjectUtils.isNotEmpty(data.get("output_selector"))) {
            List<String> selector = (List<String>) data.get("output_selector");
            WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
            workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.USER);
            workflowFieldVO.setName(selector.get(1));
            workflowFieldVO.setReflection(false);
            workflowFieldVO.setRequired(true);
            workflowFieldVO.setType(parseSourceType((String) data.get("output_type")));
            workflowFieldVO.setSchema(parseSchema((String) data.get("output_type")));

            workflowFieldVO.setValue(adaptWorkflowFieldVoValue(selector, workflowNodeMap, workflowNodeVO));
            outputs.add(workflowFieldVO);
        }
        return outputs;
    }

    public Map<String, Object> adaptConfigs(Map<String, Object> data, WorkflowNodeVO workflowNodeVO,
        Map<String, WorkflowNodeVO> workflowNodeMap, String loopType) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonConstant.DIFY.LOOP_BODY, new ArrayList<>());
        configs.put(CommonConstant.DIFY.LOOP_TYPE, loopType);
        // 循环适配终止条件
        if (ObjectUtils.isNotEmpty(data.get("break_conditions"))) {
            WorkflowBranchConfigVO workflowBranchConfigVO = new WorkflowBranchConfigVO();
            workflowBranchConfigVO.setLogic(
                WorkflowBranchConfigVO.LogicEnum.fromValue((String) data.get("logical_operator")));
            workflowBranchConfigVO.setConditions(adaptConditions(data, workflowNodeVO, workflowNodeMap));
            configs.put("break_condition", workflowBranchConfigVO);
        }
        return configs;
    }

    @SuppressWarnings("unchecked")
    private List<WorkflowBranchConfigVOConditions> adaptConditions(Map<String, Object> data,
        WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        ArrayList<WorkflowBranchConfigVOConditions> configVOConditions = new ArrayList<>();
        List<Map<String, Object>> loopVariables = (List<Map<String, Object>>) data.get("break_conditions");
        loopVariables.forEach(loopVariable -> {
            WorkflowBranchConfigVOConditions workflowBranchConfigVOConditions = new WorkflowBranchConfigVOConditions();
            String operator = (String) loopVariable.get("comparison_operator");
            workflowBranchConfigVOConditions.setOperator(OPERATOR_MAP.getOrDefault(operator, ""));
            List<String> selector = (List<String>) loopVariable.get("variable_selector");
            // 适配左边条件
            WorkflowFieldVO leftFieldVO = getBranchFieldVO(loopVariable, workflowNodeVO, selector);
            selector.add(1, workflowNodeVO.getId().equals("node_" + selector.get(0))
                ? "intermediate_loop_var." + selector.get(1)
                : selector.get(1));
            leftFieldVO.setValue(adaptWorkflowFieldVoValue(selector, workflowNodeMap, workflowNodeVO));
            workflowBranchConfigVOConditions.setLeft(leftFieldVO);
            // 适配右边条件
            WorkflowFieldVO rightFieldVO = getBranchFieldVO(loopVariable, workflowNodeVO, selector);
            String value = loopVariable.get("value").toString();
            rightFieldVO.setValue(extractValueToField(value, workflowNodeVO, workflowNodeMap));
            workflowBranchConfigVOConditions.setRight(rightFieldVO);
            configVOConditions.add(workflowBranchConfigVOConditions);
        });
        return configVOConditions;
    }
}
