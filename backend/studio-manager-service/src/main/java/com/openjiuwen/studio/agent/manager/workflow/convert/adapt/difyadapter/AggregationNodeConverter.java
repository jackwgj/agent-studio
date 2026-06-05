/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.models.GroupConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
@Component
public class AggregationNodeConverter extends AbstractSDSLNodeConverter {

    // 分组名称前缀
    private static final String GROUP_PREFIX = "group";

    // 模式当前支持第一个非空返回
    private static final String FIRST_NON_NULL = "first-non-null";

    // groups名称解析
    private static final Pattern GROUP_VARIABLE_PATTERN =
            Pattern.compile("^" + Pattern.quote(GROUP_PREFIX) + "(\\d+)_(\\d+)$");

    // 判断是否是支持的类型
    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.AGGREGATION.equals(nodeType);
    }

    // 节点转换
    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        // 构造返回
        List<WorkflowFieldVO> inputs = new ArrayList<>();;
        Map<String, WorkflowFieldVO> outputs = new HashMap<>();

        // 获取变量：是否多个分组
        Boolean groupEnabled = Optional.ofNullable((Map<String, Object>) data.get("advanced_settings"))
                .map(advancedSettings -> (Boolean) advancedSettings.get("group_enabled"))
                .orElse(false);

        // 是否分组会导致获取变量位置不一样
        if (groupEnabled) {
            // 获取分组变量
            List<Map<String, Object>> groups = Optional.ofNullable((Map<String, Object>) data.get("advanced_settings"))
                    .map(advancedSettings -> (List<Map<String, Object>>) advancedSettings.get("groups"))
                    .orElse(List.of());
            // 遍历，构造
            Integer groupIndex = 1;
            for (Map<String, Object> group : groups) {
                Integer variableIndex = 1;
                String groupName = group.get("group_name").toString().toLowerCase(Locale.ROOT);
                for (List<String> variable : (List<List<String>>) group.get("variables")) {
                    WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO().setDescription("")
                            .setName(groupName + "_" + variableIndex++)
                            .setReflection(false)
                            .setRequired(true)
                            .setSource(WorkflowFieldVO.SourceEnum.USER)
                            .setValue(
                                    adaptWorkflowFieldVoValue(variable, workflowNodeMap, workflowNodeVO)
                            );
                    setTypeAndSchemaByName(workflowFieldVO, workflowNodeMap.get(variable.get(0)), variable);
                    inputs.add(workflowFieldVO);

                    if (!outputs.containsKey(groupName)) {
                        WorkflowFieldVO output = new WorkflowFieldVO()
                                .setName(groupName)
                                .setDescription("")
                                .setReflection(false)
                                .setRequired(true)
                                .setSource(WorkflowFieldVO.SourceEnum.USER)
                                .setValue(new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED));
                        setTypeAndSchemaByName(output, workflowNodeMap.get(variable.get(0)), variable);
                        outputs.put(groupName, output);
                    }
                }
                groupIndex++;
            }
        } else {
            // 不分组时只有一个组
            List<List<String>> variables = Optional.ofNullable((List<List<String>>) data.get("variables")).orElse(List.of());
            Integer variableIndex = 1;
            String groupName = GROUP_PREFIX + "1";
            for (List<String> variable : variables) {
                WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO().setDescription("")
                        .setName(groupName + "_" + variableIndex++)
                        .setReflection(false)
                        .setRequired(true)
                        .setSource(WorkflowFieldVO.SourceEnum.USER)
                        .setValue(
                                adaptWorkflowFieldVoValue(variable, workflowNodeMap, workflowNodeVO)
                        );
                setTypeAndSchemaByName(workflowFieldVO, workflowNodeMap.get(variable.get(0)), variable);
                inputs.add(workflowFieldVO);

                if (!outputs.containsKey(groupName)) {
                    WorkflowFieldVO output = new WorkflowFieldVO()
                            .setName(groupName)
                            .setDescription("")
                            .setReflection(false)
                            .setRequired(true)
                            .setSource(WorkflowFieldVO.SourceEnum.USER)
                            .setValue(new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED));
                    setTypeAndSchemaByName(output, workflowNodeMap.get(variable.get(0)), variable);
                    outputs.put(groupName, output);
                }
            }
        }

        // 返回
        workflowNodeVO.setType(NodeType.AGGREGATION.getType());
        workflowNodeVO.setInputs(inputs);
        workflowNodeVO.setOutputs(new ArrayList<>(outputs.values()));
        workflowNodeVO.setConfigs(adaptConfigs(workflowNodeVO.getInputs()));
        return workflowNodeVO;
    }

    // 配置适配
    public Map<String, Object> adaptConfigs(List<WorkflowFieldVO> names) {
        // 构造分组的名称与索引映射
        Map<String, List<GroupConfig>> groups = new HashMap<>();
        names.stream().forEach(name -> {
            Matcher matcher = GROUP_VARIABLE_PATTERN.matcher(name.getName());
            if (matcher.find()) {
                GroupConfig groupConfig = new GroupConfig();
                groupConfig.setIndex(Integer.valueOf(matcher.group(2)) - 1);
                groupConfig.setName(name.getName());
                groups.computeIfAbsent(GROUP_PREFIX + matcher.group(1), key -> new ArrayList<>()).add(groupConfig);
            }
        });

        // 构造返回
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonConstant.DIFY.CONFIG_DEFAULT_NAME, false);
        configs.put("mode", FIRST_NON_NULL);
        configs.put("groups", groups);
        return configs;
    }

}
