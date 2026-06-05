/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
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
public class EndNodeConverter extends AbstractSDSLNodeConverter {

    private static final String RESPONSE_TEMPLATE = "response_template";

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.END.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        WorkflowNodeVO endNode = workflowNodeMap.values().stream().filter(node -> node.getType().equals(NodeType.END.getType())).findFirst().orElse(null);
        if (endNode == null) {
            workflowNodeVO.setId("node_end");
            workflowNodeVO.setType(NodeType.END.getType());
            StringBuilder template = new StringBuilder();
            workflowNodeVO.setInputs(adaptInputs(data, workflowNodeMap, template));
            workflowNodeVO.setConfigs(adaptConfigs(template.toString()));
            workflowNodeVO.setOutputs(adaptOutputs(data));
            return workflowNodeVO;
        } else {
            StringBuilder template = new StringBuilder(endNode.getConfigs().get(RESPONSE_TEMPLATE).toString());
            List<WorkflowFieldVO> inputs = endNode.getInputs();
            inputs.addAll(adaptInputs(data, workflowNodeMap, template));
            endNode.setInputs(inputs);
            endNode.setConfigs(adaptConfigs(template.toString()));
            return endNode;
        }
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap, StringBuilder template) {
        List<Map<String, Object>> variables = (List<Map<String, Object>>) data.get("outputs");
        List<WorkflowFieldVO> inputs = new ArrayList<>();
        if (variables != null) {
            variables.forEach(variable -> {
                List<String> selector = (List<String>) variable.get("value_selector");
                WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
                workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.USER);
                String varName = "_" + String.join("_", selector);
                workflowFieldVO.setName(varName);
                workflowFieldVO.setType((String) variable.get("value_type"));
                workflowFieldVO.setValue(adaptWorkflowFieldVoValue(selector, workflowNodeMap, workflowNodeMap.get(selector.get(0))));
                template.append("{{").append(varName).append("}}\n");
                inputs.add(workflowFieldVO);
            });
        }
        return inputs;
    }

    public Map<String, Object> adaptConfigs(String template) {
        Map<String, Object> configsMap = new HashMap<>();
        configsMap.put(CommonConstant.DIFY.CONFIG_DEFAULT_NAME, false);
        configsMap.put("is_stream_out", true);
        configsMap.put("response_mode", "directResponse");
        configsMap.put(RESPONSE_TEMPLATE, template);
        return configsMap;
    }

    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {
        return List.of(new WorkflowFieldVO()
                .setName("response_content")
                .setSource(WorkflowFieldVO.SourceEnum.SYSTEM)
                .setDescription("最终输出")
                .setType("string")
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)));
    }
}
