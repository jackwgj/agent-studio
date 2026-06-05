/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationResp;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationReq;
import com.openjiuwen.studio.agent.runtime.service.IMcpServerRuntimeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * McpServerRuntime controller
 */
@RestController

public class McpServerRuntimeApiController implements McpServerRuntimeApi {
    private static final Logger log = LoggerFactory.getLogger(McpServerRuntimeApiController.class);

    @Autowired
    private IMcpServerRuntimeService mcpServerRuntimeService;

    @Override
    public ResponseEntity<McpCallToolResp> callTool(String projectId, McpCallToolReq body) {
        return ResponseModel.success(mcpServerRuntimeService.callTool(projectId, body));
    }

    @Override
    public ResponseEntity<Object> listMcpServerTools(String projectId, McpValidationReq body) {
        return ResponseModel.success(mcpServerRuntimeService.listMcpServerTools(projectId, body));
    }

    @Override
    public ResponseEntity<McpValidationResp> testServer(String projectId, McpValidationReq body) {
        return ResponseModel.success(mcpServerRuntimeService.testServer(projectId, body));
    }
}