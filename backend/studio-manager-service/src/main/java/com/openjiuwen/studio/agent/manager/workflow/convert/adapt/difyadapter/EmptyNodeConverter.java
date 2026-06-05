/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmptyNodeConverter extends AbstractSDSLNodeConverter {

    @Override
    public Boolean supportNodeType(NodeType nodeType) {
        return NodeType.EMPTY.equals(nodeType);
    }

    @Override
    public WorkflowNodeVO adapt(Map<String, Object> data, WorkflowNodeVO workflowNodeVo, Map<String, WorkflowNodeVO> workflowNodeMap) {
        workflowNodeVo.setType(NodeType.EMPTY.getType());
        return workflowNodeVo;
    }

}
