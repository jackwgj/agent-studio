/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CreateDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateDefaultKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.dto.ListDefaultKnowledgeBaseConnectorsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowDefaultKnowledgeBaseConnectionDetailResponseBody;
import com.openjiuwen.studio.agent.manager.dto.TestDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.TestDefaultKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateDefaultKnowledgeBaseConnectionRequestBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateDefaultKnowledgeBaseConnectionResponse;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeBaseConnectionConfigManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * KnowledgeBaseConnectionConfigManagement controller
 */
@RestController

public class KnowledgeBaseConnectionConfigManagementApiController
    implements KnowledgeBaseConnectionConfigManagementApi {
    private static final Logger log = LoggerFactory.getLogger(
        KnowledgeBaseConnectionConfigManagementApiController.class);

    @Autowired
    private IKnowledgeBaseConnectionConfigManagementService knowledgeBaseConnectionConfigManagementService;

    @Override
    public ResponseEntity<CreateDefaultKnowledgeBaseConnectionResponse> createDefaultKbConnection(String projectId,
        CreateDefaultKnowledgeBaseConnectionRequestBody body) {
        return ResponseModel.success(
            knowledgeBaseConnectionConfigManagementService.createDefaultKbConnection(projectId, body));
    }

    @Override
    public ResponseEntity<ListDefaultKnowledgeBaseConnectorsResponseBody> listDefaultKnowledgeBaseConnectors(
        String projectId, Integer offset, Integer limit) {
        return ResponseModel.success(
            knowledgeBaseConnectionConfigManagementService.listDefaultKnowledgeBaseConnectors(projectId, offset,
                limit));
    }

    @Override
    public ResponseEntity<ShowDefaultKnowledgeBaseConnectionDetailResponseBody> showDefaultKnowledgeBaseConnection(
        String projectId, String kbConnectionId) {
        return ResponseModel.success(
            knowledgeBaseConnectionConfigManagementService.showDefaultKnowledgeBaseConnection(projectId,
                kbConnectionId));
    }

    @Override
    public ResponseEntity<TestDefaultKnowledgeBaseResponseBody> testDefaultKnowledgeBaseConnection(String projectId,
        TestDefaultKnowledgeBaseConnectionRequestBody body) {
        return ResponseModel.success(
            knowledgeBaseConnectionConfigManagementService.testDefaultKnowledgeBaseConnection(projectId, body));
    }

    @Override
    public ResponseEntity<TestDefaultKnowledgeBaseResponseBody> testDefaultKnowledgeBaseConnectionId(String projectId,
        String connectionId) {
        return ResponseModel.success(
            knowledgeBaseConnectionConfigManagementService.testDefaultKnowledgeBaseConnectionId(projectId,
                connectionId));
    }

    @Override
    public ResponseEntity<UpdateDefaultKnowledgeBaseConnectionResponse> updateDefaultKnowledgeBaseConnection(
        String projectId, String kbConnectionId, UpdateDefaultKnowledgeBaseConnectionRequestBody body) {
        return ResponseModel.success(
            knowledgeBaseConnectionConfigManagementService.updateDefaultKnowledgeBaseConnection(projectId,
                kbConnectionId, body));
    }
}