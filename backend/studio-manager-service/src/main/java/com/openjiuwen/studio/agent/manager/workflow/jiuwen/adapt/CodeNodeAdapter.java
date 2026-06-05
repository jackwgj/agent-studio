/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.apache.commons.lang3.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码节点转换IR
 *
 */
public class CodeNodeAdapter extends AbstractIRNodeAdapter {
    /**
     * 代码
     */
    private static final String CODE = "code";

    private static final String ENABLE_SANDBOX = "enable_sandbox";

    private static final String EXEC_ENV = "exec_env";

    private static final String FG_ID = "fg_id";

    private static final String FG_URN = "fg_urn";

    private static final String FG_URL = "fg_url";

    private static final String TEMPLATE_VERSION = "template_version";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNodeVo) {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CODE, workflowNodeVo.getConfigs().get(CODE));
        configs.put(ENABLE_SANDBOX, workflowNodeVo.getConfigs().get(ENABLE_SANDBOX));
        configs.put(EXEC_ENV, workflowNodeVo.getConfigs().get(EXEC_ENV));
        configs.put(FG_ID, ObjectUtils.isNotEmpty(workflowNodeVo.getConfigs().get(FG_ID)) ? workflowNodeVo.getConfigs().get(FG_ID) : "");
        configs.put(FG_URN, ObjectUtils.isNotEmpty(workflowNodeVo.getConfigs().get(FG_URN)) ? workflowNodeVo.getConfigs().get(FG_URN) : "");
        configs.put(FG_URL, ObjectUtils.isNotEmpty(workflowNodeVo.getConfigs().get(FG_URL)) ? workflowNodeVo.getConfigs().get(FG_URL) : "");
        configs.put(TEMPLATE_VERSION, ObjectUtils.isNotEmpty(workflowNodeVo.getConfigs().get(TEMPLATE_VERSION)) ? workflowNodeVo.getConfigs().get(TEMPLATE_VERSION) : "");
        return configs;
    }

    @Override
    public String getNodeType() {
        return NodeType.CODE.getIrType();
    }
}
