/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档提取器转换
 *
 */
@Component
public class DocumentExtractorConverter extends AbstractSDSLNodeConverter {
    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.DOCUMENT_EXTRACTOR.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.DOCUMENT_EXTRACTOR.getType());
        workflowNodeVO.setConfigs(adaptConfigs(data));
        workflowNodeVO.setInputs(adaptInputs(data, workflowNodeVO, workflowNodeMap));
        workflowNodeVO.setOutputs(adaptOutputs(data));
        return workflowNodeVO;
    }

    @SuppressWarnings("unchecked")
    public List<WorkflowFieldVO> adaptInputs(Map<String, Object> data, WorkflowNodeVO workflowNodeVO, Map<String, WorkflowNodeVO> workflowNodeMap) {
        List<String> selector = (List<String>) data.get("variable_selector");
        List<WorkflowFieldVO> inputs = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(selector)) {
            WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
            workflowFieldVO.setSource(WorkflowFieldVO.SourceEnum.USER);
            workflowFieldVO.setName(selector.get(1));
            workflowFieldVO.setType("string");
            workflowFieldVO.setValue(adaptWorkflowFieldVoValue(selector, workflowNodeMap, workflowNodeVO));
            inputs.add(workflowFieldVO);
        }
        return inputs;
    }

    @Override
    public List<WorkflowFieldVO> adaptOutputs(Map<String, Object> data) {
        Boolean isArrayFile = (Boolean) data.get("is_array_file");
        WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO()
            .setName("text")
            .setSource(WorkflowFieldVO.SourceEnum.USER)
            .setType(parseSourceType(isArrayFile ? "array[file/default]" : "file/default"))
            .setSchema(parseSchema(isArrayFile ? "array[file/default]" : "file/default"))
            .setValue(new WorkflowFieldVOValue()
                .setType(WorkflowFieldVOValue.TypeEnum.GENERATED)
                .setContent(""));
        return List.of(workflowFieldVO);
    }
}
