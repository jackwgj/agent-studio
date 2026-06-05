/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.utils.IRUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MCP节点转换IR
 *
 */
public class McpNodeAdapter extends AbstractIRNodeAdapter {
    private static final String ID = "id";

    @Override
    public Map<String, Object> adaptConfig(WorkflowNodeVO workflowNode) {
        Map<String, Object> nodeConfigs = workflowNode.getConfigs();
        IrAdapterService irAdapterService = SpringBeanUtils.getBean(IrAdapterService.class);
        String toolName = null;
        if (!Objects.isNull(nodeConfigs.get(CommonConstant.TOOL_NAME))) {
            toolName = nodeConfigs.get(CommonConstant.TOOL_NAME).toString();
        }
        Map<String, Object> mcpServerConfigConfig =
            irAdapterService.parseMcpConfigForWorkflow(nodeConfigs.get(ID).toString(), toolName,
                RequestContextUtils.getRequestProjectId(), RequestContextUtils.getRequestUserId(), RequestContextUtils.getRequestWorkspaceId());

        Map<String, Object> irConfigs = new HashMap<>(mcpServerConfigConfig);
        if (nodeConfigs.get(CommonConstant.Http.EXCEPTION_ENABLE) != null) {
            irConfigs.put(IRUtils.adaptKey(CommonConstant.Http.EXCEPTION_ENABLE),
                nodeConfigs.get(CommonConstant.Http.EXCEPTION_ENABLE));
            irConfigs.put(IRUtils.adaptKey(CommonConstant.Http.EXCEPTION_SUPPRESSION),
                nodeConfigs.get(CommonConstant.Http.EXCEPTION_SUPPRESSION));
        }
        return irConfigs;
    }

    @Override
    public String getNodeType() {
        return NodeType.MCP.getIrType();
    }
}
