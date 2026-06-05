/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBaseConfigsResponse;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeBaseConfigManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * KnowledgeBaseConfigManagement controller
 */
@RestController

public class KnowledgeBaseConfigManagementApiController implements KnowledgeBaseConfigManagementApi {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseConfigManagementApiController.class);

    @Autowired
    private IKnowledgeBaseConfigManagementService knowledgeBaseConfigManagementService;

    @Override
    public ResponseEntity<ListKnowledgeBaseConfigsResponse> listKnowledgeBaseConfigs(String projectId,
        String knowledgeBaseId, String workspaceId) {
        return ResponseModel.success(
            knowledgeBaseConfigManagementService.listKnowledgeBaseConfigs(projectId, knowledgeBaseId, workspaceId));
    }
}