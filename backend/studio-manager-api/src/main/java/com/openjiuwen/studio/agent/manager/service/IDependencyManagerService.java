/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.BaseResp;
import com.openjiuwen.studio.agent.manager.dto.ListDependenciesQo;
import com.openjiuwen.studio.agent.manager.dto.ListDependenciesVersionsQo;

import org.springframework.web.multipart.MultipartFile;

/**
 * DependencyManager service
 */

public interface IDependencyManagerService {

    /**
     * deleteDependency
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param packageId packageId
     */
    BaseResp deleteDependency(String projectId, String workspaceId, String packageId);

    /**
     * importDependency
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param file file
     * @param body body
     */
    BaseResp importDependency(String workspaceId, String projectId, MultipartFile file, String body);

    /**
     * updateDependency
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param packageId packageId
     * @param file file
     * @param body body
     */
    BaseResp updateDependency(String workspaceId, String projectId, String packageId, MultipartFile file, String body);
}