/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.memory;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryItemResponseBody;
import com.openjiuwen.studio.agent.manager.dto.SearchMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.rce.client.AgentRuntimeClient;
import com.openjiuwen.studio.agent.manager.service.IMemoryItemManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MemoryItemManagementService implements IMemoryItemManagementService {
    private static final Logger log = LoggerFactory.getLogger(MemoryItemManagementService.class);

    @Autowired
    private AgentRuntimeClient agentRuntimeClient;

    @Override
    public ListMemoryItemResponseBody listMemoryItems(String projectId, String memoryRepoId, Integer pageNum,
        Integer pageSize) {
        String userId = RequestContextUtils.getRequestUserId();
        if (userId == null || userId.isEmpty()) {
            throw new AgentStudioException(StudioError.AUTHENTICATION_ERROR, "User ID not found in request context");
        }

        try {
            ResponseEntity<Object> response = agentRuntimeClient.listMemories(
                memoryRepoId, userId.toLowerCase(), pageSize, pageNum);

            if (response.getBody() == null) {
                return emptyListResponse(pageNum, pageSize);
            }

            // Parse runtime response: {"total": N, "memories": [...]}
            JSONObject body = JSONObject.from(response.getBody());
            JSONArray memoriesArray = body.getJSONArray("memories");
            int total = body.getIntValue("total", 0);

            ListMemoryItemResponseBody result = new ListMemoryItemResponseBody();
            result.setPageNum(pageNum);
            result.setPageSize(pageSize);
            result.setTotal(total);

            if (memoriesArray != null && !memoriesArray.isEmpty()) {
                List<ListMemoryItemResponseBody.MemoryItemInfo> items = memoriesArray.stream()
                    .map(obj -> {
                        JSONObject mem = (JSONObject) obj;
                        ListMemoryItemResponseBody.MemoryItemInfo item = new ListMemoryItemResponseBody.MemoryItemInfo();
                        item.setId(mem.getString("memory_id"));
                        item.setContent(mem.getString("content"));
                        item.setUserId(userId);
                        item.setAgentId(null); // Not available in runtime response
                        item.setScore(null); // Not applicable for list
                        return item;
                    })
                    .collect(Collectors.toList());
                result.setItems(items);
            } else {
                result.setItems(Collections.emptyList());
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to list memories from runtime for repo {}: {}", memoryRepoId, e.getMessage(), e);
            throw new AgentStudioException(StudioError.CSS_UNI_SEARCH_SERVICE_EXCEPTION,
                "Failed to list memories: " + e.getMessage());
        }
    }

    @Override
    public void deleteMemoryItem(String projectId, String memoryRepoId, String memoryId) {
        // Use batch delete with single ID
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(Collections.singletonList(memoryId));
        batchDeleteMemoryItems(projectId, memoryRepoId, body);
    }

    @Override
    public void batchDeleteMemoryItems(String projectId, String memoryRepoId, BatchDeleteMemoryItemRequestBody body) {
        List<String> memoryIds = body.getMemoryIds();
        if (memoryIds == null || memoryIds.isEmpty()) {
            throw new AgentStudioException(StudioError.MEMORY_REPO_NOT_EXIST,
                "memoryIds must not be empty for batch delete");
        }

        String userId = RequestContextUtils.getRequestUserId();
        if (userId == null || userId.isEmpty()) {
            throw new AgentStudioException(StudioError.AUTHENTICATION_ERROR, "User ID not found in request context");
        }

        try {
            Map<String, List<String>> requestBody = new HashMap<>();
            requestBody.put("memory_ids", memoryIds);

            ResponseEntity<Object> response = agentRuntimeClient.batchDeleteMemories(
                memoryRepoId, userId.toLowerCase(), requestBody);

            if (response.getBody() != null) {
                JSONObject result = JSONObject.from(response.getBody());
                String status = result.getString("status");
                if ("partial".equals(status)) {
                    JSONArray errors = result.getJSONArray("errors");
                    log.warn("Batch delete partially failed for repo {}: {}", memoryRepoId, errors);
                }
            }
        } catch (Exception e) {
            log.error("Failed to batch delete memories from runtime for repo {}: {}",
                memoryRepoId, e.getMessage(), e);
            throw new AgentStudioException(StudioError.CSS_UNI_SEARCH_SERVICE_EXCEPTION,
                "Failed to delete memories: " + e.getMessage());
        }
    }

    @Override
    public ListMemoryItemResponseBody searchMemoryItems(String projectId, String memoryRepoId,
        SearchMemoryItemRequestBody body) {
        String userId = RequestContextUtils.getRequestUserId();
        if (userId == null || userId.isEmpty()) {
            throw new AgentStudioException(StudioError.AUTHENTICATION_ERROR, "User ID not found in request context");
        }

        try {
            Map<String, Object> searchBody = new HashMap<>();
            searchBody.put("query", body.getQuery());
            searchBody.put("top_k", body.getTopK() != null ? body.getTopK() : 10);
            searchBody.put("threshold", body.getThreshold() != null ? body.getThreshold().doubleValue() : 0.3);

            ResponseEntity<Object> response = agentRuntimeClient.searchMemories(
                memoryRepoId, userId.toLowerCase(), searchBody);

            if (response.getBody() == null) {
                ListMemoryItemResponseBody result = new ListMemoryItemResponseBody();
                result.setItems(Collections.emptyList());
                result.setTotal(0);
                return result;
            }

            // Parse runtime response: {"total": N, "memories": [...]}
            JSONObject responseBody = JSONObject.from(response.getBody());
            JSONArray memoriesArray = responseBody.getJSONArray("memories");
            int total = responseBody.getIntValue("total", 0);

            ListMemoryItemResponseBody result = new ListMemoryItemResponseBody();
            result.setTotal(total);

            if (memoriesArray != null && !memoriesArray.isEmpty()) {
                List<ListMemoryItemResponseBody.MemoryItemInfo> items = memoriesArray.stream()
                    .map(obj -> {
                        JSONObject mem = (JSONObject) obj;
                        ListMemoryItemResponseBody.MemoryItemInfo item = new ListMemoryItemResponseBody.MemoryItemInfo();
                        item.setId(mem.getString("memory_id"));
                        item.setContent(mem.getString("content"));
                        item.setUserId(userId);
                        item.setAgentId(null);
                        item.setScore(mem.getDouble("score") != null ? mem.getDouble("score").floatValue() : null);
                        return item;
                    })
                    .collect(Collectors.toList());
                result.setItems(items);
            } else {
                result.setItems(Collections.emptyList());
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to search memories from runtime for repo {}: {}", memoryRepoId, e.getMessage(), e);
            throw new AgentStudioException(StudioError.CSS_UNI_SEARCH_SERVICE_EXCEPTION,
                "Failed to search memories: " + e.getMessage());
        }
    }

    private ListMemoryItemResponseBody emptyListResponse(Integer pageNum, Integer pageSize) {
        ListMemoryItemResponseBody response = new ListMemoryItemResponseBody();
        response.setItems(Collections.emptyList());
        response.setTotal(0);
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        return response;
    }
}
