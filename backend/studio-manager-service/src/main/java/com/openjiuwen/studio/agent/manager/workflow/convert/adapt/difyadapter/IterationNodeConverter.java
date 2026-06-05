/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 迭代节点转换
 *
 */
@Component
public class IterationNodeConverter extends AbstractSDSLNodeConverter {

    @Autowired
    private com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.LoopNodeConverter loopNodeConverter;

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.ITERATION.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.LOOP.getType());
        // 开始节点只需适配输出参数
        workflowNodeVO.setInputs(loopNodeConverter.adaptInputs(data, workflowNodeVO, workflowNodeMap));
        workflowNodeVO.setOutputs(loopNodeConverter.adaptOutputs(data, workflowNodeVO, workflowNodeMap));
        workflowNodeVO.setConfigs(loopNodeConverter.adaptConfigs(data, workflowNodeVO, workflowNodeMap, "arrayLoop"));
        return workflowNodeVO;
    }
}
