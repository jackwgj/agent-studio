/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.dto.knowledge.ListUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.BatchDeleteUserVariableMemoryRequestBody;
import com.openjiuwen.studio.agent.common.dto.BatchDeleteUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.common.dto.run.ResetUserVariableMemoryResponseBody;

/**
 * AgentMemoryManagement service
 */

public interface IAgentMemoryManagementService {

    /**
     * batchDeleteUserVariableMemory
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param workspaceId workspaceId
     * @param body body
     */
    BatchDeleteUserVariableMemoryResponseBody batchDeleteUserVariableMemory(String projectId, String agentId,
        String workspaceId, BatchDeleteUserVariableMemoryRequestBody body);

    /**
     * listUserVariableMemory
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param workspaceId workspaceId
     */
    ListUserVariableMemoryResponseBody listUserVariableMemory(String projectId, String agentId, String workspaceId);

    /**
     * resetUserVariableMemory
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param workspaceId workspaceId
     */
    ResetUserVariableMemoryResponseBody resetUserVariableMemory(String projectId, String agentId, String workspaceId);
}