/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.AuthConfigListQo;
import com.openjiuwen.studio.agent.manager.dto.CreateAuthConfigReq;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthCfgList;

/**
 * ProviderAuthMgmt service
 */

public interface IProviderAuthMgmtService {

    /**
     * authConfigList
     *
     * @param projectId projectId
     * @param authConfigListQo authConfigListQo
     */
    ProviderAuthCfgList authConfigList(String projectId, AuthConfigListQo authConfigListQo);

    /**
     * createAuthConfig
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param availableCheck availableCheck
     * @param body body
     */
    Void createAuthConfig(String projectId, String workspaceId, Boolean availableCheck, CreateAuthConfigReq body);

    /**
     * removeAuthConfig
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param id id
     */
    Void removeAuthConfig(String projectId, String workspaceId, String id);

    /**
     * removeProviderAuthCfg
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param providerId providerId
     * @param authId authId
     */
    Void removeProviderAuthCfg(String projectId, String workspaceId, String providerId, String authId);
}