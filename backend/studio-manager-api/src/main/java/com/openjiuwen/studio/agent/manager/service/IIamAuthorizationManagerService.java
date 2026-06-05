/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.BaseResp;

/**
 * IamAuthorizationManager service
 */

public interface IIamAuthorizationManagerService {

    /**
     * doAuthorization
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    BaseResp doAuthorization(String projectId, String workspaceId);

    /**
     * queryAuthorization
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    BaseResp queryAuthorization(String projectId, String workspaceId);
}