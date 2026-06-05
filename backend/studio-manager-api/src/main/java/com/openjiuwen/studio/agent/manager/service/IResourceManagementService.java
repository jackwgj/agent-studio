/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ResourceUsageDetails;

/**
 * ResourceManagement service
 */

public interface IResourceManagementService {

    /**
     * resourceUsageDetails
     *
     * @param projectId projectId
     * @param resourceType resourceType
     */
    ResourceUsageDetails resourceUsageDetails(String projectId, String resourceType);
}