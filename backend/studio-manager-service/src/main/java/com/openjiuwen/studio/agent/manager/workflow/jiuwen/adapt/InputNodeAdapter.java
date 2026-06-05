/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import java.util.HashMap;
import java.util.Map;

/**
 * 输入节点转换IR
 *
 */
public class InputNodeAdapter extends AbstractIRNodeAdapter {
    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        return new HashMap<>();
    }

    @Override
    public String getNodeType() {
        return NodeType.INPUT.getIrType();
    }
}
