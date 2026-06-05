/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 长期记忆节点configs字段的DSL转IR
 *
 */
public class LTMNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 召回方式
     */
    private static final String OUTPUT_MODE = "output_mode";

    /**
     * 召回数量
     */
    private static final String TOP_K = "top_k";

    /**
     * 相关度阈值
     */
    private static final String RECALL_THRESHOLD = "recall_threshold";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> irConfigs = new HashMap<>();
        Map<String, Object> dslConfigs = workflowNodeVo.getConfigs();
        irConfigs.put(IRUtils.adaptKey(OUTPUT_MODE), dslConfigs.get(OUTPUT_MODE));
        adaptModel(irConfigs, dslConfigs);
        irConfigs.put(TOP_K, dslConfigs.get(TOP_K));
        irConfigs.put(IRUtils.adaptKey(RECALL_THRESHOLD), dslConfigs.get(RECALL_THRESHOLD));
        return irConfigs;
    }

    @Override
    public String getNodeType() {
        return NodeType.LTM.getIrType();
    }
}
