/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 迭代开始节点
 *
 */
@Component
public class IterationStartNodeConverter extends AbstractSDSLNodeConverter {
    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.ITERATION_START.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.LOOP_INPUT.getType());
        workflowNodeVO.setName("循环输入");
        workflowNodeVO.setId(workflowNodeVO.getId().replaceAll("start", "_input"));
        return workflowNodeVO;
    }
}
