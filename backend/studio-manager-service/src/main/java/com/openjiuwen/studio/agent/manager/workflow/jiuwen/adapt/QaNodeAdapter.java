/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import java.util.Map;

/**
 * 问答节点转换IR
 *
 */
public class QaNodeAdapter extends AbstractIRNodeAdapter {
    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO node) {
        return node.getConfigs();
    }

    @Override
    public String getNodeType() {
        return NodeType.QA.getIrType();
    }
}
