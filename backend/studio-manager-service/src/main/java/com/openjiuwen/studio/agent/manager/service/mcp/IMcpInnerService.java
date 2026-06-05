/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp;

import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;

import java.util.List;

public interface IMcpInnerService {
    List<McpServiceEntity> getMcpByIds(String workspaceId, List<String> validMcpServerIds);

    List<McpServerInfo> getMcpServerInfoByIds(String workspaceId, List<String> validMcpServerIds);

    McpServerInfo getMcpServerInfoById(String id, String targetWorkspaceId);

    boolean createOrUpdate(McpServerInfo mcpServer, String targetWorkspaceId);

    boolean isMcpExist(String projectId, String name);

    McpServiceEntity getMcpByName(String targetWorkspaceId, String name);

    String buildServerConfig(String text);
}
