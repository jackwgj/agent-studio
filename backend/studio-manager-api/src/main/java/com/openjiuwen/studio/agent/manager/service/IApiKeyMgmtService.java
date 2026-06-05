/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ApiKeyRequest;
import com.openjiuwen.studio.agent.manager.dto.ApiKeysListResp;
import com.openjiuwen.studio.agent.manager.dto.ApikeyResponse;
import com.openjiuwen.studio.agent.manager.dto.QueryApiKeysQo;

/**
 * ApiKeyMgmt service
 */

public interface IApiKeyMgmtService {

    /**
     * createApiKey
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    ApikeyResponse createApiKey(String projectId, String workspaceId, ApiKeyRequest body);

    /**
     * deleteApiKey
     *
     * @param id id
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    Void deleteApiKey(String id, String projectId, String workspaceId);

    /**
     * queryApiKeys
     *
     * @param projectId projectId
     * @param queryApiKeysQo queryApiKeysQo
     */
    ApiKeysListResp queryApiKeys(String projectId, QueryApiKeysQo queryApiKeysQo);
}