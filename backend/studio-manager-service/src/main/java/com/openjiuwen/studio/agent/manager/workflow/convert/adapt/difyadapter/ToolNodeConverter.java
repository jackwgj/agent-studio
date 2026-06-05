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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Component
public class ToolNodeConverter extends AbstractSDSLNodeConverter {

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.TOOL.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.TOOL.getType());
        workflowNodeVO.setInputs(adaptInputs(data, workflowNodeMap));
        workflowNodeVO.setOutputs(adaptOutputs(data));
        workflowNodeVO.setConfigs(adaptConfigs(data));
        return workflowNodeVO;
    }


    @Override
    public List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, Map<String, WorkflowNodeVO> workflowNodeMap) {
        return Optional.ofNullable((Map<String, Map<String, Object>>) data.get("tool_parameters")).orElse(Collections.emptyMap())
                .entrySet().stream()
                .map(param -> {
                    return new WorkflowFieldVO()
                            .setName(param.getKey())
                            .setRequired(false)
                            .setValue(new WorkflowFieldVOValue()
                                    .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                                    .setContent(param.getValue().get("value")));
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {

        List<WorkflowFieldVO> outputs = new ArrayList<>();

        // 适配dify默认输出参数
        outputs.add(new WorkflowFieldVO()
                .setName("text")
                .setDescription("工具生成的内容")
                .setType(VariableEnum.STRING.getDslType())
                .setRequired(true)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent(null)));

        outputs.add(new WorkflowFieldVO()
                .setName("files")
                .setDescription("工具生成的文件")
                .setType(VariableEnum.ARRAY.getDslType())
                .setSchema(new WorkflowFieldVO().setType(VariableEnum.FILE.getDslType()).setName(""))
                .setRequired(true)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent(null)));

        outputs.add(new WorkflowFieldVO()
                .setName("json")
                .setDescription("工具生成的 json")
                .setType(VariableEnum.ARRAY.getDslType())
                .setSchema(new WorkflowFieldVO().setType(VariableEnum.JSON_OBJECT.getDslType()).setName(""))
                .setRequired(true)
                .setValue(new WorkflowFieldVOValue()
                        .setType(WorkflowFieldVOValue.TypeEnum.LITERAL)
                        .setContent(null)));

        return outputs;
    }

}
