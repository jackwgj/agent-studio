/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 结束节点转换IR
 *
 */
public class EndNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 回复模式
     */
    private static final String RESPONSE_MODE = "response_mode";

    /**
     * 是否流式输出
     */
    private static final String IS_STREAM_OUT = "is_stream_out";

    /**
     * 响应模板
     */
    private static final String RESPONSE_TEMPLATE = "response_template";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        Map<String, Object> nodeConfigs = workflowNodeVo.getConfigs();
        configs.put(IRUtils.adaptKey(RESPONSE_TEMPLATE), nodeConfigs.get(RESPONSE_TEMPLATE));
        configs.put(IRUtils.adaptKey(IS_STREAM_OUT), nodeConfigs.get(IS_STREAM_OUT));
        configs.put(IRUtils.adaptKey(RESPONSE_MODE), nodeConfigs.get(RESPONSE_MODE));
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.END.getIrType();
    }
}
