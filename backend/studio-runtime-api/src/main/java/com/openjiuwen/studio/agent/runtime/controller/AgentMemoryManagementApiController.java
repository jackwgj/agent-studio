/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.knowledge.ListUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.dto.BatchDeleteUserVariableMemoryRequestBody;
import com.openjiuwen.studio.agent.common.dto.BatchDeleteUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.common.dto.run.ResetUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.runtime.service.IAgentMemoryManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * AgentMemoryManagement controller
 */
@RestController

public class AgentMemoryManagementApiController implements AgentMemoryManagementApi {
    private static final Logger log = LoggerFactory.getLogger(AgentMemoryManagementApiController.class);

    @Autowired
    private IAgentMemoryManagementService agentMemoryManagementService;

    @Override
    public ResponseEntity<BatchDeleteUserVariableMemoryResponseBody> batchDeleteUserVariableMemory(String projectId,
        String agentId, String workspaceId, BatchDeleteUserVariableMemoryRequestBody body) {
        return ResponseModel.success(
            agentMemoryManagementService.batchDeleteUserVariableMemory(projectId, agentId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ListUserVariableMemoryResponseBody> listUserVariableMemory(String projectId, String agentId,
        String workspaceId) {
        return ResponseModel.success(
            agentMemoryManagementService.listUserVariableMemory(projectId, agentId, workspaceId));
    }

    @Override
    public ResponseEntity<ResetUserVariableMemoryResponseBody> resetUserVariableMemory(String projectId, String agentId,
        String workspaceId) {
        return ResponseModel.success(
            agentMemoryManagementService.resetUserVariableMemory(projectId, agentId, workspaceId));
    }
}