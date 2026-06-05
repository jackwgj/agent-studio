/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.runtime.dto.ResourceClearResp;


/**
 * ResourcesCleaner service
 */

public interface IResourcesCleanerService {

    /**
     * clearResource
     *
     * @param projectId projectId
     * @param resourceId resourceId
     * @param type type
     */
    ResourceClearResp clearResource(String projectId, String resourceId, String type) ;
}