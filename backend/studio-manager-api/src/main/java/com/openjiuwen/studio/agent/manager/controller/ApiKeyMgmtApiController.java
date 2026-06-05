/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ApiKeyRequest;
import com.openjiuwen.studio.agent.manager.dto.ApiKeysListResp;
import com.openjiuwen.studio.agent.manager.dto.ApikeyResponse;
import com.openjiuwen.studio.agent.manager.dto.QueryApiKeysQo;
import com.openjiuwen.studio.agent.manager.service.IApiKeyMgmtService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ApiKeyMgmt controller
 */
@RestController

public class ApiKeyMgmtApiController implements ApiKeyMgmtApi {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyMgmtApiController.class);

    @Autowired
    private IApiKeyMgmtService apiKeyMgmtService;

    @Override
    public ResponseEntity<ApikeyResponse> createApiKey(String projectId, String workspaceId, ApiKeyRequest body) {
        return ResponseModel.success(apiKeyMgmtService.createApiKey(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<Void> deleteApiKey(String id, String projectId, String workspaceId) {
        return ResponseModel.success(apiKeyMgmtService.deleteApiKey(id, projectId, workspaceId));
    }

    @Override
    public ResponseEntity<ApiKeysListResp> queryApiKeys(String projectId, QueryApiKeysQo queryApiKeysQo) {
        return ResponseModel.success(apiKeyMgmtService.queryApiKeys(projectId, queryApiKeysQo));
    }
}