/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.runtime.dto.ClearLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesQo;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesResponseBody;


/**
 * UserMemoryManagement service
 */

public interface IUserMemoryManagementService {

    /**
     * clearLongTermMemories
     *
     * @param projectId projectId
     * @param memoryRepoId memoryRepoId
     */
    ClearLongTermMemoriesResponseBody clearLongTermMemories(String projectId, String memoryRepoId) ;

    /**
     * deleteLongTermMemories
     *
     * @param projectId projectId
     * @param memoryRepoId memoryRepoId
     * @param body body
     */
    DeleteLongTermMemoriesResponseBody deleteLongTermMemories(String projectId, String memoryRepoId, DeleteLongTermMemoriesRequestBody body) ;

    /**
     * listLongTermMemories
     *
     * @param projectId projectId
     * @param listLongTermMemoriesQo listLongTermMemoriesQo
     */
    ListLongTermMemoriesResponseBody listLongTermMemories(String projectId, ListLongTermMemoriesQo listLongTermMemoriesQo) ;

    /**
     * updateLongTermMemories
     *
     * @param projectId projectId
     * @param memoryRepoId memoryRepoId
     * @param body body
     */
    UpdateLongTermMemoriesResponseBody updateLongTermMemories(String projectId, String memoryRepoId, UpdateLongTermMemoriesRequestBody body) ;
}