/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息节点转换IR
 *
 */
public class MessageNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 消息节点模板名称
     */
    private static final String TEMPLATE = "template";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        configs.put(TEMPLATE, nodeConfigs.get(TEMPLATE));
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.MESSAGE.getIrType();
    }
}
