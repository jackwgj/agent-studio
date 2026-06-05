/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import java.util.HashMap;
import java.util.Map;

/**
 * 开始节点转换IR
 *
 */
public class StartNodeAdapter extends AbstractIRNodeAdapter {
    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        return new HashMap<>();
    }

    @Override
    public String getNodeType() {
        return NodeType.START.getIrType();
    }
}
