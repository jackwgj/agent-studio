/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

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
import com.openjiuwen.studio.agent.manager.service.IThirdPartyKnowledgeBaseConnectionManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ThirdPartyKnowledgeBaseConnectionManagement controller
 */
@RestController

public class ThirdPartyKnowledgeBaseConnectionManagementApiController
    implements ThirdPartyKnowledgeBaseConnectionManagementApi {
    private static final Logger log = LoggerFactory.getLogger(
        ThirdPartyKnowledgeBaseConnectionManagementApiController.class);

    @Autowired
    private IThirdPartyKnowledgeBaseConnectionManagementService thirdPartyKnowledgeBaseConnectionManagementService;

    @Override
    public ResponseEntity<CreateThirdPartyKnowledgeBaseConnectionResponse> createThirdPartyKnowledgeBaseConnection(
        String projectId, String workspaceId, CreateThirdPartyKnowledgeBaseConnectionRequestBody body) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.createThirdPartyKnowledgeBaseConnection(projectId,
                workspaceId, body));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteThirdPartyKnowledgeBaseConnection(String projectId, String workspaceId,
        String knowledgeBaseConnectionId) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.deleteThirdPartyKnowledgeBaseConnection(projectId,
                workspaceId, knowledgeBaseConnectionId));
    }

    @Override
    public ResponseEntity<ListThirdPartyKnowledgeBaseConnectionsResponse> listThirdPartyKnowledgeBaseConnections(
        String projectId, ListThirdPartyKnowledgeBaseConnectionsQo listThirdPartyKnowledgeBaseConnectionsQo) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.listThirdPartyKnowledgeBaseConnections(projectId,
                listThirdPartyKnowledgeBaseConnectionsQo));
    }

    @Override
    public ResponseEntity<ListThirdPartyKnowledgeBaseConnectorsResponseBody> listThirdPartyKnowledgeBaseConnectors(
        String projectId, ListThirdPartyKnowledgeBaseConnectorsQo listThirdPartyKnowledgeBaseConnectorsQo) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.listThirdPartyKnowledgeBaseConnectors(projectId,
                listThirdPartyKnowledgeBaseConnectorsQo));
    }

    @Override
    public ResponseEntity<ShowKnowledgeBaseConnectorAbilitiesResponseBody> showKnowledgeBaseConnectorAbilities(
        String projectId, ShowKnowledgeBaseConnectorAbilitiesQo showKnowledgeBaseConnectorAbilitiesQo) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.showKnowledgeBaseConnectorAbilities(projectId,
                showKnowledgeBaseConnectorAbilitiesQo));
    }

    @Override
    public ResponseEntity<ShowThirdPartyKnowledgeBaseConnectionDetailResponseBody> showThirdPartyKnowledgeBaseConnection(
        String projectId, String workspaceId, String knowledgeBaseConnectionId) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.showThirdPartyKnowledgeBaseConnection(projectId,
                workspaceId, knowledgeBaseConnectionId));
    }

    @Override
    public ResponseEntity<TestConnectionThirdPartyKnowledgeBaseResponseBody> testConnectionThirdPartyKnowledgeBaseConnection(
        String projectId, String workspaceId, TestConnectionThirdPartyKnowledgeBaseConnectionRequestBody body) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.testConnectionThirdPartyKnowledgeBaseConnection(
                projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<TestConnectionThirdPartyKnowledgeBaseResponseBody> testConnectionThirdPartyKnowledgeBaseConnectionById(
        String projectId, String knowledgeBaseConnectionId, String workspaceId) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.testConnectionThirdPartyKnowledgeBaseConnectionById(
                projectId, knowledgeBaseConnectionId, workspaceId));
    }

    @Override
    public ResponseEntity<UpdateThirdPartyKnowledgeBaseConnectionResponse> updateThirdPartyKnowledgeBaseConnection(
        String projectId, String workspaceId, String knowledgeBaseConnectionId,
        UpdateThirdPartyKnowledgeBaseConnectionRequestBody body) {
        return ResponseModel.success(
            thirdPartyKnowledgeBaseConnectionManagementService.updateThirdPartyKnowledgeBaseConnection(projectId,
                workspaceId, knowledgeBaseConnectionId, body));
    }
}