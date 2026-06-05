/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import java.util.Map;

/**
 *  空白节点
 */
public class EmptyNodeAdapter extends AbstractIRNodeAdapter {

    /**
     * 空白节点无配置信息，直接返回
     * @param workflowNodeVo 工作流节点信息
     * @return
     */
    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        return workflowNodeVo.getConfigs();
    }

    @Override
    public String getNodeType() {
        return NodeType.EMPTY.getIrType();
    }
}
