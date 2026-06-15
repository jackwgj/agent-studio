/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.memory;

import com.openjiuwen.studio.agent.agentbase.client.CssUniSearchClient;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.ListMemoryRequestBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.ListMemoryResponseBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.MemoryInfo;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.SearchMemoryRequestBody;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryItemResponseBody;
import com.openjiuwen.studio.agent.manager.dto.SearchMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.service.IMemoryItemManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemoryItemManagementService implements IMemoryItemManagementService {
    private static final Logger log = LoggerFactory.getLogger(MemoryItemManagementService.class);

    @Autowired
    private CssUniSearchClient cssUniSearchClient;

    @Value("${memory.lakeSearch.endpoint:}")
    private String lakeSearchEndpoint;

    @Value("${memory.lakeSearch.application-id:}")
    private String applicationId;

    @Value("${memory.lakeSearch.project-id:}")
    private String lakeSearchProjectId;

    @Override
    public ListMemoryItemResponseBody listMemoryItems(String projectId, String memoryRepoId, Integer pageNum,
        Integer pageSize) {
        URI uri = getUri();
        HttpHeaders headers = getHeaders();

        ListMemoryRequestBody listReq = new ListMemoryRequestBody();
        ListMemoryResponseBody result = cssUniSearchClient.listMemory(uri, headers,
            lakeSearchProjectId, applicationId, memoryRepoId, pageNum, pageSize, listReq);

        return convertToListResponse(result, pageNum, pageSize);
    }

    @Override
    public void deleteMemoryItem(String projectId, String memoryRepoId, String memoryId) {
        URI uri = getUri();
        HttpHeaders headers = getHeaders();

        cssUniSearchClient.deleteMemory(uri, headers, lakeSearchProjectId, applicationId, memoryRepoId, memoryId);
    }

    @Override
    public void batchDeleteMemoryItems(String projectId, String memoryRepoId, BatchDeleteMemoryItemRequestBody body) {
        URI uri = getUri();
        HttpHeaders headers = getHeaders();

        List<String> memoryIds = body.getMemoryIds();
        if (memoryIds == null || memoryIds.isEmpty()) {
            throw new AgentStudioException(StudioError.MEMORY_REPO_NOT_EXIST,
                "memoryIds must not be empty for batch delete");
        }

        List<String> failedIds = new java.util.ArrayList<>();
        for (String memoryId : memoryIds) {
            try {
                cssUniSearchClient.deleteMemory(uri, headers, lakeSearchProjectId, applicationId, memoryRepoId, memoryId);
            } catch (Exception e) {
                log.warn("Failed to delete memory item {} from repo {}: {}", memoryId, memoryRepoId, e.getMessage());
                failedIds.add(memoryId);
            }
        }
        if (!failedIds.isEmpty()) {
            log.error("Batch delete memory items partially failed for repo {}: {}/{} failed, ids={}",
                memoryRepoId, failedIds.size(), memoryIds.size(), failedIds);
        }
    }

    @Override
    public ListMemoryItemResponseBody searchMemoryItems(String projectId, String memoryRepoId,
        SearchMemoryItemRequestBody body) {
        URI uri = getUri();
        HttpHeaders headers = getHeaders();

        SearchMemoryRequestBody searchReq = new SearchMemoryRequestBody();
        searchReq.setQuery(body.getQuery());
        searchReq.setTopK(body.getTopK());
        searchReq.setThreshold(body.getThreshold() != null ? body.getThreshold().floatValue() : null);

        List<MemoryInfo> results = cssUniSearchClient.searchMemory(uri, headers,
            lakeSearchProjectId, applicationId, memoryRepoId, searchReq);

        ListMemoryItemResponseBody response = new ListMemoryItemResponseBody();
        if (results != null) {
            response.setItems(results.stream().map(this::convertToItemInfo).collect(Collectors.toList()));
            response.setTotal(results.size());
        } else {
            response.setItems(Collections.emptyList());
            response.setTotal(0);
        }
        return response;
    }

    private URI getUri() {
        if (lakeSearchEndpoint == null || lakeSearchEndpoint.isEmpty()) {
            throw new AgentStudioException(StudioError.CSS_UNI_SEARCH_SERVICE_EXCEPTION,
                "LakeSearch endpoint not configured");
        }
        return URI.create(lakeSearchEndpoint);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", RequestContextUtils.getRequestUserId());
        return headers;
    }

    private ListMemoryItemResponseBody convertToListResponse(ListMemoryResponseBody result, Integer pageNum,
        Integer pageSize) {
        ListMemoryItemResponseBody response = new ListMemoryItemResponseBody();
        if (result != null) {
            if (result.getData() != null) {
                response.setItems(result.getData().stream().map(this::convertToItemInfo).collect(Collectors.toList()));
                response.setTotal(result.getData().size());
            }
            response.setPageNum(pageNum);
            response.setPageSize(pageSize);
        }
        return response;
    }

    private ListMemoryItemResponseBody.MemoryItemInfo convertToItemInfo(MemoryInfo info) {
        ListMemoryItemResponseBody.MemoryItemInfo item = new ListMemoryItemResponseBody.MemoryItemInfo();
        item.setId(info.getId());
        item.setContent(info.getMemory());
        item.setScore(info.getScore());
        item.setUserId(info.getUserId());
        item.setAgentId(info.getAgentId());
        return item;
    }
}
