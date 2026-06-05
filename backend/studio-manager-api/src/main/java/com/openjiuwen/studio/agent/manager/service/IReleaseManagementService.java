/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ReleaseAppInfo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveReleaseAppInfoQo;

/**
 * ReleaseManagement service
 */

public interface IReleaseManagementService {

    /**
     * retrieveReleaseAppInfo
     *
     * @param projectId projectId
     * @param releaseId releaseId
     * @param retrieveReleaseAppInfoQo retrieveReleaseAppInfoQo
     */
    ReleaseAppInfo retrieveReleaseAppInfo(String projectId, String releaseId,
        RetrieveReleaseAppInfoQo retrieveReleaseAppInfoQo);
}