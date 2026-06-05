/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CreateDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateDefaultKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.dto.ListDefaultKnowledgeBaseConnectorsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowDefaultKnowledgeBaseConnectionDetailResponseBody;
import com.openjiuwen.studio.agent.manager.dto.TestDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.TestDefaultKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateDefaultKnowledgeBaseConnectionResponse;

/**
 * KnowledgeBaseConnectionConfigManagement service
 */

public interface IKnowledgeBaseConnectionConfigManagementService {

    /**
     * createDefaultKbConnection
     *
     * @param projectId projectId
     * @param body body
     */
    CreateDefaultKnowledgeBaseConnectionResponse createDefaultKbConnection(String projectId,
        CreateDefaultKnowledgeBaseConnectionRequestBody body);

    /**
     * listDefaultKnowledgeBaseConnectors
     *
     * @param projectId projectId
     * @param offset offset
     * @param limit limit
     */
    ListDefaultKnowledgeBaseConnectorsResponseBody listDefaultKnowledgeBaseConnectors(String projectId, Integer offset,
        Integer limit);

    /**
     * showDefaultKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param kbConnectionId kbConnectionId
     */
    ShowDefaultKnowledgeBaseConnectionDetailResponseBody showDefaultKnowledgeBaseConnection(String projectId,
        String kbConnectionId);

    /**
     * testDefaultKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param body body
     */
    TestDefaultKnowledgeBaseResponseBody testDefaultKnowledgeBaseConnection(String projectId,
        TestDefaultKnowledgeBaseConnectionRequestBody body);

    /**
     * testDefaultKnowledgeBaseConnectionId
     *
     * @param projectId projectId
     * @param connectionId connectionId
     */
    TestDefaultKnowledgeBaseResponseBody testDefaultKnowledgeBaseConnectionId(String projectId, String connectionId);

    /**
     * updateDefaultKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param kbConnectionId kbConnectionId
     * @param body body
     */
    UpdateDefaultKnowledgeBaseConnectionResponse updateDefaultKnowledgeBaseConnection(String projectId,
        String kbConnectionId, UpdateDefaultKnowledgeBaseConnectionRequestBody body);
}