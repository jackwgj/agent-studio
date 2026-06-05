/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateThirdPartyKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectionsQo;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectionsResponse;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectorsQo;
import com.openjiuwen.studio.agent.manager.dto.ListThirdPartyKnowledgeBaseConnectorsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeBaseConnectorAbilitiesQo;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeBaseConnectorAbilitiesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody;
import com.openjiuwen.studio.agent.manager.dto.TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.TestConnectionThirdPartyKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateThirdPartyKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateThirdPartyKnowledgeBaseConnectionResponse;

/**
 * ThirdPartyKnowledgeBaseConnectionManagement service
 */

public interface IThirdPartyKnowledgeBaseConnectionManagementService {

    /**
     * createThirdPartyKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateThirdPartyKnowledgeBaseConnectionResponse createThirdPartyKnowledgeBaseConnection(String projectId,
        String workspaceId, CreateThirdPartyKnowledgeBaseConnectionRequestBody body);

    /**
     * deleteThirdPartyKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeBaseConnectionId knowledgeBaseConnectionId
     */
    CommonDeleteRsp deleteThirdPartyKnowledgeBaseConnection(String projectId, String workspaceId,
        String knowledgeBaseConnectionId);

    /**
     * listThirdPartyKnowledgeBaseConnections
     *
     * @param projectId projectId
     * @param listThirdPartyKnowledgeBaseConnectionsQo listThirdPartyKnowledgeBaseConnectionsQo
     */
    ListThirdPartyKnowledgeBaseConnectionsResponse listThirdPartyKnowledgeBaseConnections(String projectId,
        ListThirdPartyKnowledgeBaseConnectionsQo listThirdPartyKnowledgeBaseConnectionsQo);

    /**
     * listThirdPartyKnowledgeBaseConnectors
     *
     * @param projectId projectId
     * @param listThirdPartyKnowledgeBaseConnectorsQo listThirdPartyKnowledgeBaseConnectorsQo
     */
    ListThirdPartyKnowledgeBaseConnectorsResponseBody listThirdPartyKnowledgeBaseConnectors(String projectId,
        ListThirdPartyKnowledgeBaseConnectorsQo listThirdPartyKnowledgeBaseConnectorsQo);

    /**
     * showKnowledgeBaseConnectorAbilities
     *
     * @param projectId projectId
     * @param showKnowledgeBaseConnectorAbilitiesQo showKnowledgeBaseConnectorAbilitiesQo
     */
    ShowKnowledgeBaseConnectorAbilitiesResponseBody showKnowledgeBaseConnectorAbilities(String projectId,
        ShowKnowledgeBaseConnectorAbilitiesQo showKnowledgeBaseConnectorAbilitiesQo);

    /**
     * showThirdPartyKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeBaseConnectionId knowledgeBaseConnectionId
     */
    ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody showThirdPartyKnowledgeBaseConnection(String projectId,
        String workspaceId, String knowledgeBaseConnectionId);

    /**
     * testConnectionThirdPartyKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    TestConnectionThirdPartyKnowledgeBaseResponseBody testConnectionThirdPartyKnowledgeBaseConnection(String projectId,
        String workspaceId, TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody body);

    /**
     * testConnectionThirdPartyKnowledgeBaseConnectionById
     *
     * @param projectId projectId
     * @param knowledgeBaseConnectionId knowledgeBaseConnectionId
     * @param workspaceId workspaceId
     */
    TestConnectionThirdPartyKnowledgeBaseResponseBody testConnectionThirdPartyKnowledgeBaseConnectionById(
        String projectId, String knowledgeBaseConnectionId, String workspaceId);

    /**
     * updateThirdPartyKnowledgeBaseConnection
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param knowledgeBaseConnectionId knowledgeBaseConnectionId
     * @param body body
     */
    UpdateThirdPartyKnowledgeBaseConnectionResponse updateThirdPartyKnowledgeBaseConnection(String projectId,
        String workspaceId, String knowledgeBaseConnectionId, UpdateThirdPartyKnowledgeBaseConnectionRequestBody body);
}