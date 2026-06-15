/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.BatchDeleteMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryItemResponseBody;
import com.openjiuwen.studio.agent.manager.dto.SearchMemoryItemRequestBody;

public interface IMemoryItemManagementService {

    ListMemoryItemResponseBody listMemoryItems(String projectId, String memoryRepoId, Integer pageNum, Integer pageSize);

    void deleteMemoryItem(String projectId, String memoryRepoId, String memoryId);

    void batchDeleteMemoryItems(String projectId, String memoryRepoId, BatchDeleteMemoryItemRequestBody body);

    ListMemoryItemResponseBody searchMemoryItems(String projectId, String memoryRepoId, SearchMemoryItemRequestBody body);
}
