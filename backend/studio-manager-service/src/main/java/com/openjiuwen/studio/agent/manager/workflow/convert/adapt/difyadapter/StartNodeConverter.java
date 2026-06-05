/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.VariableEnum;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StartNodeConverter extends AbstractSDSLNodeConverter {

    private final static String START_NODE_NAME = "开始";

    private final static String VISUAL_MODE = "visual";

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.START.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setId("node_start");
        workflowNodeVO.setType(NodeType.START.getType());
        workflowNodeVO.setName(START_NODE_NAME);
        // 开始节点只需适配输出参数
        workflowNodeVO.setOutputs(adaptOutputs(data));
        workflowNodeVO.setConfigs(adaptConfigs(data));
        return workflowNodeVO;
    }

    @Override
    public Map<String, Object> adaptConfigs(Map<String, Object> data) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonConstant.DIFY.CONFIG_DEFAULT_NAME, true);
        // 适配触发器
        if (NodeType.TRIGGER_SCHEDULE.getDifyType().equals(data.get("type"))) {
            Map<String, Object> trigger = new HashMap<>();
            String triggerMode = data.get("mode").toString();
            trigger.put("mode", triggerMode);
            if (VISUAL_MODE.equalsIgnoreCase(triggerMode)) {
                // 可视化配置
                trigger.put("frequency", data.get("frequency").toString());
                trigger.put("visual_config", data.get("visual_config"));
            } else {
                // cron表达式
                trigger.put("cron_expression", data.get("cron_expression").toString());
            }
            trigger.put("type", NodeType.TRIGGER_SCHEDULE.getDifyType());
            configs.put(CommonConstant.DIFY.TRIGGER, trigger);
        } else if (NodeType.TRIGGER_WEBHOOK.getDifyType().equals(data.get("type"))) {
            Map<String, String> trigger = new HashMap<>();
            trigger.put("type", NodeType.TRIGGER_WEBHOOK.getDifyType());
            configs.put(CommonConstant.DIFY.TRIGGER, trigger);
        }
        return configs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {
        List<WorkflowFieldVO> outputs = new ArrayList<>();
        List<Map<String, Object>> variables = (List<Map<String, Object>>) data.get("variables");
        if (variables != null && !NodeType.TRIGGER_WEBHOOK.getDifyType().equals(data.get("type"))) {
            for (Map<String, Object> variable : variables) {
                if ("query".equalsIgnoreCase(variable.get("variable").toString())) {
                    // query参数为系统参数，单独适配
                    continue;
                }
                outputs.add(adaptWorkflowField(variable));
            }
        }
        // 适配系统变量
        outputs.addAll(adaptPresetVariable());
        return outputs;
    }

    public WorkflowFieldVO adaptWorkflowField(Map<String, Object> variable) {
        WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
        workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.USER);
        workflowFieldVO.setName((String) variable.get("variable"));
        workflowFieldVO.setRequired((Boolean) variable.get("required"));
        workflowFieldVO.setValue(new WorkflowFieldVOValue().setType(WorkflowFieldVOValue.TypeEnum.GENERATED));
        VariableEnum variableType = VariableEnum.fromDifyValue(variable.get("type").toString());
        workflowFieldVO.setType(variableType.getDslType());
        switch (variableType) {
            case FILE_LIST -> {
                Map<String, String> fileSchema = new HashMap<>();
                fileSchema.put("name", "");
                fileSchema.put("type", VariableEnum.FILE.getDslType());
                workflowFieldVO.setSchema(fileSchema);
            }
            case JSON_OBJECT -> workflowFieldVO.setSchema(new ArrayList<>());
        }
        return workflowFieldVO;
    }


    /**
     * 适配开始节点固定的系统变量、query参数和dify自带的files参数
     * @return
     */
    private List<WorkflowFieldVO> adaptPresetVariable() {
        List<WorkflowFieldVO> presetVariables = new ArrayList<>();
        // 适配系统变量
        WorkflowFieldVOValue workflowFieldVOValue = new WorkflowFieldVOValue();
        workflowFieldVOValue.setContent(new HashMap<>()).setHint("系统变量").setType(WorkflowFieldVOValue.TypeEnum.LITERAL);
        WorkflowFieldVO systemVariable = new WorkflowFieldVO();
        systemVariable.setDescription("系统变量").setName("sys")
                .setType(VariableEnum.JSON_OBJECT.getDslType()).setRequired(true).setSource(WorkflowFieldVO.SourceEnum.SYSTEM).setValue(workflowFieldVOValue)
                .setSchema(adaptSystemSchemas());

        // 适配query参数
        WorkflowFieldVO query = new WorkflowFieldVO();
        query.setDescription("用户输入").setName("query").setType(VariableEnum.TEXT_INPUT.getDslType()).setRequired(false)
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM).setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent("")
                        .setHint("用户输入"));

        presetVariables.add(systemVariable);
        presetVariables.add(query);
        return presetVariables;
    }

    private List<WorkflowFieldVO> adaptSystemSchemas() {
        List<WorkflowFieldVO> schemas = new ArrayList<>();
        // 适配role和content
        List<WorkflowFieldVO> historySubSchemas = new ArrayList<>();
        WorkflowFieldVO roleField = new WorkflowFieldVO()
                .setName("role")
                .setType("string")
                .setDescription("角色")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent("")
                        .setHint("角色")
                );
        WorkflowFieldVO contentField = new WorkflowFieldVO()
                .setName("content")
                .setType("string")
                .setDescription("消息内容")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent("")
                        .setHint("消息内容")
                );
        historySubSchemas.add(roleField);
        historySubSchemas.add(contentField);
        WorkflowFieldVO historySchema = new WorkflowFieldVO()
                .setSchema(historySubSchemas)
                .setType("object")
                .setName("");

        // 适配conversation_history
        WorkflowFieldVO conversationHistory = new WorkflowFieldVO()
                .setName("conversation_history")
                .setType("array")
                .setDescription("会话历史消息")
                .setRequired(true)
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent(new ArrayList<>()) // 空数组 []
                        .setHint("会话历史消息")
                )
                .setSchema(historySchema); // 设置嵌套 schema
        schemas.add(conversationHistory);

        // 适配current_time
        WorkflowFieldVO currentTime = new WorkflowFieldVO()
                .setName("current_time")
                .setType("string")
                .setDescription("当前系统时间")
                .setRequired(true)
                .setFieldType("input")
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent("")
                        .setHint("当前系统时间")
                );
        schemas.add(currentTime);

        // 适配user_id
        WorkflowFieldVO userId = new WorkflowFieldVO()
                .setName("user_id")
                .setType("string")
                .setDescription("使用该工作流的唯一标识")
                .setRequired(true)
                .setFieldType("input")
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent("")
                        .setHint("用户唯一标识")
                );
        schemas.add(userId);

        // 适配构建 conversation_id
        WorkflowFieldVO conversationId = new WorkflowFieldVO()
                .setName("conversation_id")
                .setType("string")
                .setDescription("工作流对话id")
                .setRequired(true)
                .setFieldType("input")
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent("")
                        .setHint("工作流对话id")
                );
        schemas.add(conversationId);
        return schemas;
    }
}
