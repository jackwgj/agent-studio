/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.enums.VariableOperationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
@Component
public class SetVariableNodeConverter extends AbstractSDSLNodeConverter {

    // 分组名称前缀
    private static final String INPUT_DESCRIPTION = "循环之间共享参数";

    // 判断是否是支持的类型
    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.SET_VARIABLE.equals(nodeType);
    }

    // 节点转换
    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        // 构造返回
        List<WorkflowFieldVO> inputs = new ArrayList<>();
        List<Map<String, WorkflowFieldVO>> settings = new ArrayList<>();


        // 遍历items中的变量设置 inputs 和 configs
        Optional.ofNullable((List<Map<String, Object>>) data.get("items"))
                .ifPresent(items -> items.forEach(item -> {
                    inputs.add(adaptInput(item, workflowNodeVO, workflowNodeMap));
                    settings.add(adaptSettings(item, workflowNodeVO, workflowNodeMap));
                }));

        // 设置类型
        workflowNodeVO.setType(NodeType.SET_VARIABLE.getType());
        // 设置输入
        workflowNodeVO.setInputs(inputs);
        // 设置配置
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonConstant.DIFY.CONFIG_DEFAULT_NAME, false);
        configs.put("settings", settings);
        workflowNodeVO.setConfigs(configs);
        // 返回
        return workflowNodeVO;
    }


    // 转换输入逻辑
    public WorkflowFieldVO adaptInput(Map<String, Object> item, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        // 构造返回
        return Optional.ofNullable((List<String>) item.get("variable_selector"))
                .map(variable -> {
                    WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO()
                            .setDescription(INPUT_DESCRIPTION)
                            .setName(variable.get(1))
                            .setReflection(false)
                            .setRequired(true)
                            .setSource(WorkflowFieldVO.SourceEnum.USER)
                            .setValue(adaptWorkflowFieldVoValue(variable, workflowNodeMap, workflowNodeVO));
                    setTypeAndSchemaByName(workflowFieldVO, workflowNodeMap.get(variable.get(0)), variable);
                    return workflowFieldVO;
                })
                .orElse(null);
    }


    // 转换配置逻辑
    public Map<String, WorkflowFieldVO> adaptSettings(Map<String, Object> item, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        WorkflowFieldVO leftWorkflowFieldVO = Optional.ofNullable((List<String>) item.get("variable_selector"))
                .map(variable -> {
                    WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO().setValue(
                            adaptWorkflowFieldVoValue(variable, workflowNodeMap, workflowNodeVO));
                    setTypeAndSchemaByName(workflowFieldVO, workflowNodeMap.get(variable.get(0)), variable);
                    return workflowFieldVO;
                })
                .orElse(null);

        WorkflowFieldVO rightWorkflowFieldVO = getConvertedValue(item, workflowNodeVO, workflowNodeMap, leftWorkflowFieldVO);

        // 返回
        Map<String, WorkflowFieldVO> settings = new HashMap<>();
        settings.put("left", leftWorkflowFieldVO);
        settings.put("right", rightWorkflowFieldVO);
        return settings;
    }

    /**
     * 变量赋值节点的操作转换
     * dify -> ours: 不兼容的转换，返回前端报错
     *
     * @param item dify数据
     * @param workflowNodeMap 已经转换的节点
     * @param originalVariable  原始变量
     * @return 转换后的数据
     */
    private WorkflowFieldVO getConvertedValue(Map<String, Object> item, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap, WorkflowFieldVO originalVariable) {
        // 获取变量的操作类型
        VariableOperationType operation = Optional.ofNullable((String) item.get("operation"))
                .map(VariableOperationType::fromValue)
                .orElse(VariableOperationType.UNKNOW);
        switch (operation) {
            case OVER_WRITE -> {
                return Optional.ofNullable((List<String>) item.get("value"))
                        .map(variable -> new WorkflowFieldVO()
                                .setType(originalVariable.getType())
                                .setSchema(originalVariable.getSchema())
                                .setValue(
                                        adaptWorkflowFieldVoValue(variable, workflowNodeMap, workflowNodeVO)
                                ))
                        .orElse(new WorkflowFieldVO());
            }
            case SET -> {
                return Optional.ofNullable(item.get("value"))
                        .map(value -> new WorkflowFieldVO()
                                    .setType(originalVariable.getType())
                                    .setSchema(originalVariable.getSchema())
                                    .setValue(new WorkflowFieldVOValue().setType(operation.toTypeEnum()).setContent(value)))
                        .orElse(new WorkflowFieldVO());
            }
            case CLEAR -> {
                return new WorkflowFieldVO()
                        .setType(originalVariable.getType())
                        .setSchema(originalVariable.getSchema())
                        .setValue(new WorkflowFieldVOValue()
                                .setType(WorkflowFieldVOValue.TypeEnum.REF)
                                .setOperator(operation.toOperatorEnum())
                                .setContent(originalVariable.getValue().getContent()));
            }
            default -> {
                return Optional.ofNullable(item.get("value"))
                        .map(value -> new WorkflowFieldVO()
                                .setType(originalVariable.getType())
                                .setSchema(originalVariable.getSchema())
                                .setValue(new WorkflowFieldVOValue()
                                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                                        .setContent(value)))
                        .orElse(new WorkflowFieldVO());
            }
        }
    }

}
