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
import com.openjiuwen.studio.agent.runtime.dto.Memory;
import com.openjiuwen.studio.agent.runtime.dto.MemoryUpdate;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;
import com.openjiuwen.studio.agent.runtime.service.IUserMemoryManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Memory item management backed by LTM via Python agent-runtime internal endpoints.
 */
@Service
public class UserProfileMemoryManagementServiceImpl implements IUserMemoryManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileMemoryManagementServiceImpl.class);

    private final JiuWenClient jiuWenClient;

    public UserProfileMemoryManagementServiceImpl(JiuWenClient jiuWenClient) {
        this.jiuWenClient = jiuWenClient;
    }

    @Override
    public ClearLongTermMemoriesResponseBody clearLongTermMemories(String projectId, String memoryRepoId) {
        String userId = RequestContextUtils.getRequestUserId();
        try {
            jiuWenClient.clearUserMemories(memoryRepoId, userId);
            return new ClearLongTermMemoriesResponseBody().setSuccess(true);
        } catch (Exception e) {
            log.error("Failed to clear memories for user {} in repo {}: {}", userId, memoryRepoId, e.getMessage());
            return new ClearLongTermMemoriesResponseBody().setSuccess(false);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public DeleteLongTermMemoriesResponseBody deleteLongTermMemories(String projectId, String memoryRepoId,
        DeleteLongTermMemoriesRequestBody body) {
        String userId = RequestContextUtils.getRequestUserId();
        try {
            Map<String, Object> reqBody = Collections.singletonMap("memory_ids", body.getMemoryIds());
            jiuWenClient.batchDeleteMemories(memoryRepoId, userId, reqBody);
            return new DeleteLongTermMemoriesResponseBody().setResult(true);
        } catch (Exception e) {
            log.error("Failed to batch-delete memories in repo {}: {}", memoryRepoId, e.getMessage());
            return new DeleteLongTermMemoriesResponseBody().setResult(false);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListLongTermMemoriesResponseBody listLongTermMemories(String projectId, ListLongTermMemoriesQo qo) {
        String userId = RequestContextUtils.getRequestUserId();
        try {
            // Convert offset/limit pagination to page_num/page_size
            int pageSize = qo.getLimit() != null ? qo.getLimit() : 10;
            int pageNum = (qo.getOffset() != null && qo.getOffset() > 0)
                ? (qo.getOffset() / pageSize) + 1 : 1;
            ResponseEntity<Object> resp = jiuWenClient.listUserMemories(
                qo.getMemoryRepoId(), userId, pageSize, pageNum, qo.getMemoryType());
            Map<String, Object> body = (Map<String, Object>) resp.getBody();
            if (body == null) {
                return new ListLongTermMemoriesResponseBody().setTotal(0).setMemories(Collections.emptyList());
            }
            int total = body.get("total") instanceof Number n ? n.intValue() : 0;
            List<Map<String, Object>> rawMemories = (List<Map<String, Object>>) body.getOrDefault("memories", Collections.emptyList());
            List<Memory> memories = new ArrayList<>();
            for (Map<String, Object> raw : rawMemories) {
                Memory m = new Memory();
                m.setMemoryId((String) raw.get("memory_id"));
                m.setContent((String) raw.get("content"));
                m.setType((String) raw.get("type"));
                Object ts = raw.get("last_update_time");
                if (ts instanceof Number n) {
                    m.setLastUpdateTime(n.longValue());
                }
                memories.add(m);
            }
            return new ListLongTermMemoriesResponseBody().setTotal(total).setMemories(memories);
        } catch (Exception e) {
            log.error("Failed to list memories in repo {}: {}", qo.getMemoryRepoId(), e.getMessage());
            return new ListLongTermMemoriesResponseBody().setTotal(0).setMemories(Collections.emptyList());
        }
    }

    @Override
    public UpdateLongTermMemoriesResponseBody updateLongTermMemories(String projectId, String memoryRepoId,
        UpdateLongTermMemoriesRequestBody body) {
        List<MemoryUpdate> updates = body.getMemories();
        if (updates == null || updates.isEmpty()) {
            return new UpdateLongTermMemoriesResponseBody().setResult(true);
        }
        boolean allOk = true;
        for (MemoryUpdate update : updates) {
            try {
                Map<String, Object> reqBody = Map.of(
                    "user_id", RequestContextUtils.getRequestUserId(),
                    "content", update.getContent() != null ? update.getContent() : ""
                );
                jiuWenClient.updateMemory(memoryRepoId, update.getMemoryId(), reqBody);
            } catch (Exception e) {
                log.error("Failed to update memory {} in repo {}: {}", update.getMemoryId(), memoryRepoId, e.getMessage());
                allOk = false;
            }
        }
        return new UpdateLongTermMemoriesResponseBody().setResult(allOk);
    }
}
