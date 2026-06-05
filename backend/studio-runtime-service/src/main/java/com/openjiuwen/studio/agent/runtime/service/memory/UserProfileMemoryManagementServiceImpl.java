/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.memory;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.dto.ClearLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesQo;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.client.MemoryServiceClient;
import com.openjiuwen.studio.agent.runtime.service.IUserMemoryManagementService;

import org.springframework.stereotype.Service;

@Service
public class UserProfileMemoryManagementServiceImpl implements IUserMemoryManagementService {

    private final MemoryServiceClient memoryServiceClient;

    public UserProfileMemoryManagementServiceImpl(MemoryServiceClient memoryServiceClient) {
        this.memoryServiceClient = memoryServiceClient;
    }

    @Override
    public ClearLongTermMemoriesResponseBody clearLongTermMemories(String projectId, String memoryRepoId) {
        String authToken = RequestContextUtils.getRequestAuthToken();
        memoryServiceClient.clearLongTermMemories(authToken, projectId, memoryRepoId,
            RequestContextUtils.getRequestUserId());
        return new ClearLongTermMemoriesResponseBody().setSuccess(true);
    }

    @Override
    public DeleteLongTermMemoriesResponseBody deleteLongTermMemories(String projectId, String memoryRepoId,
        DeleteLongTermMemoriesRequestBody body) {
        String authToken = RequestContextUtils.getRequestAuthToken();
        memoryServiceClient.deleteLongTermMemories(authToken, projectId, memoryRepoId, body);
        return new DeleteLongTermMemoriesResponseBody().setResult(true);
    }

    @Override
    public ListLongTermMemoriesResponseBody listLongTermMemories(String projectId, ListLongTermMemoriesQo body) {
        String authToken = RequestContextUtils.getRequestAuthToken();
        return memoryServiceClient.listLongTermMemories(authToken,
            projectId, body.getMemoryRepoId(), body.getMemoryType(), body.getOffset(), body.getLimit()).getBody();
    }

    @Override
    public UpdateLongTermMemoriesResponseBody updateLongTermMemories(String projectId, String memoryRepoId,
        UpdateLongTermMemoriesRequestBody body) {
        String authToken = RequestContextUtils.getRequestAuthToken();
        memoryServiceClient.updateLongTermMemories(authToken, projectId, memoryRepoId, body);
        return new UpdateLongTermMemoriesResponseBody().setResult(true);
    }
}
