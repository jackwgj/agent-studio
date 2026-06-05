/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ListMcpServersQo;
import com.openjiuwen.studio.agent.manager.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfoList;
import com.openjiuwen.studio.agent.manager.dto.McpServerReq;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;

/**
 * McpServerManager service
 */

public interface IMcpServerManagerService {

    /**
     * addMcpServer
     *
     * @param projectId projectId
     * @param body body
     */
    McpServerInfo addMcpServer(String projectId, McpServerReq body);

    /**
     * callTool
     *
     * @param projectId projectId
     * @param serverId serverId
     * @param body body
     */
    McpCallToolResp callTool(String projectId, String serverId, McpCallToolReq body);

    /**
     * configMcpServer
     *
     * @param projectId projectId
     * @param serverId serverId
     * @param body body
     */
    McpServerConfig configMcpServer(String projectId, String serverId, McpServerConfig body);

    /**
     * deleteMcpServer
     *
     * @param projectId projectId
     * @param serverId serverId
     */
    CommonDeleteRsp deleteMcpServer(String projectId, String serverId);

    /**
     * deleteMcpServerConfig
     *
     * @param projectId projectId
     * @param serverId serverId
     */
    CommonDeleteRsp deleteMcpServerConfig(String projectId, String serverId);

    /**
     * getMcpServer
     *
     * @param projectId projectId
     * @param serverId serverId
     */
    McpServerInfo getMcpServer(String projectId, String serverId);

    /**
     * listMcpServerTools
     *
     * @param projectId projectId
     * @param offset offset
     * @param limit limit
     * @param body body
     */
    McpServerTools listMcpServerTools(String projectId, Integer offset, Integer limit, McpServerReq body);

    /**
     * listMcpServers
     *
     * @param projectId projectId
     * @param listMcpServersQo listMcpServersQo
     */
    McpServerInfoList listMcpServers(String projectId, ListMcpServersQo listMcpServersQo);

    /**
     * updateMcpServer
     *
     * @param projectId projectId
     * @param serverId serverId
     * @param body body
     */
    McpServerInfo updateMcpServer(String projectId, String serverId, McpServerReq body);

    /**
     * updateMcpServerConfig
     *
     * @param projectId projectId
     * @param serverId serverId
     * @param body body
     */
    McpServerConfig updateMcpServerConfig(String projectId, String serverId, McpServerConfig body);
}