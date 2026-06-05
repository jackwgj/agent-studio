/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ListMcpServersQo;
import com.openjiuwen.studio.agent.manager.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfoList;
import com.openjiuwen.studio.agent.manager.dto.McpServerReq;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;
import com.openjiuwen.studio.agent.manager.service.IMcpServerManagerService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * McpServerManager controller
 */
@RestController

public class McpServerManagerApiController implements McpServerManagerApi {
    private static final Logger log = LoggerFactory.getLogger(McpServerManagerApiController.class);

    @Autowired
    private IMcpServerManagerService mcpServerManagerService;

    @Override
    public ResponseEntity<McpServerInfo> addMcpServer(String projectId, McpServerReq body) {
        return ResponseModel.success(mcpServerManagerService.addMcpServer(projectId, body));
    }

    @Override
    public ResponseEntity<McpCallToolResp> callTool(String projectId, String serverId, McpCallToolReq body) {
        return ResponseModel.success(mcpServerManagerService.callTool(projectId, serverId, body));
    }

    @Override
    public ResponseEntity<McpServerConfig> configMcpServer(String projectId, String serverId, McpServerConfig body) {
        return ResponseModel.success(mcpServerManagerService.configMcpServer(projectId, serverId, body));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteMcpServer(String projectId, String serverId) {
        return ResponseModel.success(mcpServerManagerService.deleteMcpServer(projectId, serverId));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteMcpServerConfig(String projectId, String serverId) {
        return ResponseModel.success(mcpServerManagerService.deleteMcpServerConfig(projectId, serverId));
    }

    @Override
    public ResponseEntity<McpServerInfo> getMcpServer(String projectId, String serverId) {
        return ResponseModel.success(mcpServerManagerService.getMcpServer(projectId, serverId));
    }

    @Override
    public ResponseEntity<McpServerTools> listMcpServerTools(String projectId, Integer offset, Integer limit,
        McpServerReq body) {
        return ResponseModel.success(mcpServerManagerService.listMcpServerTools(projectId, offset, limit, body));
    }

    @Override
    public ResponseEntity<McpServerInfoList> listMcpServers(String projectId, ListMcpServersQo listMcpServersQo) {
        return ResponseModel.success(mcpServerManagerService.listMcpServers(projectId, listMcpServersQo));
    }

    @Override
    public ResponseEntity<McpServerInfo> updateMcpServer(String projectId, String serverId, McpServerReq body) {
        return ResponseModel.success(mcpServerManagerService.updateMcpServer(projectId, serverId, body));
    }

    @Override
    public ResponseEntity<McpServerConfig> updateMcpServerConfig(String projectId, String serverId,
        McpServerConfig body) {
        return ResponseModel.success(mcpServerManagerService.updateMcpServerConfig(projectId, serverId, body));
    }
}