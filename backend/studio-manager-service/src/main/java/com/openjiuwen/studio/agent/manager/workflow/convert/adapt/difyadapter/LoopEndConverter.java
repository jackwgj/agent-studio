/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * 循环退出节点
 *
 */
@Component
public class LoopEndConverter extends AbstractSDSLNodeConverter {
    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.LOOP_END.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVO,
        Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVO.setType(NodeType.LOOP_END.getType());
        workflowNodeVO.setConfigs(adaptConfigs(data));
        workflowNodeVO.setInputs(new ArrayList<>());
        workflowNodeVO.setOutputs(new ArrayList<>());
        return workflowNodeVO;
    }
}
