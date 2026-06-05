/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryReposResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryRepositoriesQo;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowMemoryRepoResponseBody;

/**
 * MemoryRepoManagement service
 */

public interface IMemoryRepoManagementService {

    /**
     * createMemoryRepo
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreateMemoryRepoResponseBody createMemoryRepo(String projectId, String workspaceId,
        CreateMemoryRepoRequestBody body);

    /**
     * deleteMemoryRepo
     *
     * @param projectId projectId
     * @param memoryRepoId memoryRepoId
     * @param workspaceId workspaceId
     */
    DeleteMemoryRepoResponseBody deleteMemoryRepo(String projectId, String memoryRepoId, String workspaceId);

    /**
     * listMemoryRepositories
     *
     * @param projectId projectId
     * @param listMemoryRepositoriesQo listMemoryRepositoriesQo
     */
    ListMemoryReposResponseBody listMemoryRepositories(String projectId,
        ListMemoryRepositoriesQo listMemoryRepositoriesQo);

    /**
     * modifyMemoryRepo
     *
     * @param projectId projectId
     * @param memoryRepoId memoryRepoId
     * @param workspaceId workspaceId
     * @param body body
     */
    ModifyMemoryRepoResponseBody modifyMemoryRepo(String projectId, String memoryRepoId, String workspaceId,
        ModifyMemoryRepoRequestBody body);

    /**
     * showMemoryRepo
     *
     * @param projectId projectId
     * @param memoryRepoId memoryRepoId
     * @param workspaceId workspaceId
     */
    ShowMemoryRepoResponseBody showMemoryRepo(String projectId, String memoryRepoId, String workspaceId);
}