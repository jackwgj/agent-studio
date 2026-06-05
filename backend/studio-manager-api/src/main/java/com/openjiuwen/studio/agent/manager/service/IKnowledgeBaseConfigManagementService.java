/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBaseConfigsResponse;

/**
 * KnowledgeBaseConfigManagement service
 */

public interface IKnowledgeBaseConfigManagementService {

    /**
     * listKnowledgeBaseConfigs
     *
     * @param projectId projectId
     * @param knowledgeBaseId knowledgeBaseId
     * @param workspaceId workspaceId
     */
    ListKnowledgeBaseConfigsResponse listKnowledgeBaseConfigs(String projectId, String knowledgeBaseId,
        String workspaceId);
}