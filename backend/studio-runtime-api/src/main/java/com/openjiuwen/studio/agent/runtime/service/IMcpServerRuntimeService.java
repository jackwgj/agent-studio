/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationResp;
import com.openjiuwen.studio.agent.runtime.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.common.dto.mcp.McpValidationReq;

/**
 * McpServerRuntime service
 */

public interface IMcpServerRuntimeService {

    /**
     * callTool
     *
     * @param projectId projectId
     * @param body body
     */
    McpCallToolResp callTool(String projectId, McpCallToolReq body) ;

    /**
     * listMcpServerTools
     *
     * @param projectId projectId
     * @param body body
     */
    Object listMcpServerTools(String projectId, McpValidationReq body) ;

    /**
     * testServer
     *
     * @param projectId projectId
     * @param body body
     */
    McpValidationResp testServer(String projectId, McpValidationReq body) ;
}