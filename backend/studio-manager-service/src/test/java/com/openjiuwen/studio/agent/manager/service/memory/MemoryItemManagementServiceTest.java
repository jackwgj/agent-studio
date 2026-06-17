/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryItemResponseBody;
import com.openjiuwen.studio.agent.manager.dto.SearchMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.rce.client.AgentRuntimeClient;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class MemoryItemManagementServiceTest {
    @Mock
    private AgentRuntimeClient agentRuntimeClient;

    @InjectMocks
    private MemoryItemManagementService memoryItemManagementService;

    MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils;

    @BeforeEach
    void setUp() {
        mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("test-user");
    }

    @AfterEach
    void tearDown() {
        mockedStaticRequestContextUtils.close();
    }

    // ── listMemoryItems tests ──

    @Test
    void test_listMemoryItems_returns_mapped_items() {
        // Given
        JSONObject memory = new JSONObject();
        memory.put("memory_id", "mem-1");
        memory.put("content", "test content");
        memory.put("type", "summary");

        JSONArray memoriesArray = new JSONArray();
        memoriesArray.add(memory);

        JSONObject responseBody = new JSONObject();
        responseBody.put("total", 1);
        responseBody.put("memories", memoriesArray);

        when(agentRuntimeClient.listMemories(eq("repo-1"), eq("test-user"), eq(10), eq(1)))
            .thenReturn(ResponseEntity.ok(responseBody));

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("mem-1", result.getItems().get(0).getId());
        assertEquals("test content", result.getItems().get(0).getContent());
        assertEquals("test-user", result.getItems().get(0).getUserId());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
    }

    @Test
    void test_listMemoryItems_null_response_returns_empty() {
        // Given
        when(agentRuntimeClient.listMemories(any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(null));

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void test_listMemoryItems_empty_memories_array() {
        // Given
        JSONObject responseBody = new JSONObject();
        responseBody.put("total", 0);
        responseBody.put("memories", new JSONArray());

        when(agentRuntimeClient.listMemories(any(), any(), any(), any()))
            .thenReturn(ResponseEntity.ok(responseBody));

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void test_listMemoryItems_user_id_lowercased() {
        // Given — userId from context is "Test-User"
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("Test-User");

        JSONObject responseBody = new JSONObject();
        responseBody.put("total", 0);
        responseBody.put("memories", new JSONArray());

        when(agentRuntimeClient.listMemories(eq("repo-1"), eq("test-user"), any(), any()))
            .thenReturn(ResponseEntity.ok(responseBody));

        // When
        memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10);

        // Then — verify lowercase userId was passed
        verify(agentRuntimeClient).listMemories(eq("repo-1"), eq("test-user"), eq(10), eq(1));
    }

    @Test
    void test_listMemoryItems_no_user_id_throws() {
        // Given
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(null);

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10));
    }

    @Test
    void test_listMemoryItems_runtime_error_throws() {
        // Given
        when(agentRuntimeClient.listMemories(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("connection failed"));

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10));
    }

    // ── deleteMemoryItem tests ──

    @Test
    void test_deleteMemoryItem_delegates_to_batch_delete() {
        // Given
        JSONObject responseBody = new JSONObject();
        responseBody.put("status", "ok");

        when(agentRuntimeClient.batchDeleteMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(responseBody));

        // When
        memoryItemManagementService.deleteMemoryItem("project-1", "repo-1", "mem-1");

        // Then — verify batch delete was called with single ID
        verify(agentRuntimeClient).batchDeleteMemories(eq("repo-1"), eq("test-user"), any(Map.class));
    }

    // ── batchDeleteMemoryItems tests ──

    @Test
    void test_batchDeleteMemoryItems_success() {
        // Given
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(List.of("mem-1", "mem-2", "mem-3"));

        JSONObject responseBody = new JSONObject();
        responseBody.put("status", "ok");

        when(agentRuntimeClient.batchDeleteMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(responseBody));

        // When
        memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body);

        // Then
        verify(agentRuntimeClient).batchDeleteMemories(eq("repo-1"), eq("test-user"), any(Map.class));
    }

    @Test
    void test_batchDeleteMemoryItems_empty_list_throws() {
        // Given
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(Collections.emptyList());

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body));
    }

    @Test
    void test_batchDeleteMemoryItems_null_list_throws() {
        // Given
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body));
    }

    @Test
    void test_batchDeleteMemoryItems_partial_failure_logged() {
        // Given
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(List.of("mem-1", "mem-2"));

        JSONObject responseBody = new JSONObject();
        responseBody.put("status", "partial");
        JSONArray errors = new JSONArray();
        JSONObject error = new JSONObject();
        error.put("mem_id", "mem-2");
        error.put("error", "not found");
        errors.add(error);
        responseBody.put("errors", errors);

        when(agentRuntimeClient.batchDeleteMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(responseBody));

        // When — should not throw
        memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body);

        // Then — verify call was made
        verify(agentRuntimeClient).batchDeleteMemories(eq("repo-1"), eq("test-user"), any(Map.class));
    }

    @Test
    void test_batchDeleteMemoryItems_no_user_id_throws() {
        // Given
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("");
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(List.of("mem-1"));

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body));
    }

    @Test
    void test_batchDeleteMemoryItems_runtime_error_throws() {
        // Given
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(List.of("mem-1"));

        when(agentRuntimeClient.batchDeleteMemories(any(), any(), anyMap()))
            .thenThrow(new RuntimeException("connection failed"));

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body));
    }

    // ── searchMemoryItems tests ──

    @Test
    void test_searchMemoryItems_returns_results() {
        // Given
        JSONObject memory = new JSONObject();
        memory.put("memory_id", "mem-s1");
        memory.put("content", "search result content");
        memory.put("score", 0.88);

        JSONArray memoriesArray = new JSONArray();
        memoriesArray.add(memory);

        JSONObject responseBody = new JSONObject();
        responseBody.put("total", 1);
        responseBody.put("memories", memoriesArray);

        when(agentRuntimeClient.searchMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(responseBody));

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test query");
        body.setTopK(5);
        body.setThreshold(0.5);

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("mem-s1", result.getItems().get(0).getId());
        assertEquals("search result content", result.getItems().get(0).getContent());
        assertEquals(0.88f, result.getItems().get(0).getScore(), 0.01);
    }

    @Test
    void test_searchMemoryItems_null_response_returns_empty() {
        // Given
        when(agentRuntimeClient.searchMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(null));

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test");

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void test_searchMemoryItems_empty_results() {
        // Given
        JSONObject responseBody = new JSONObject();
        responseBody.put("total", 0);
        responseBody.put("memories", new JSONArray());

        when(agentRuntimeClient.searchMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(responseBody));

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test");

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void test_searchMemoryItems_parameters_passed_correctly() {
        // Given
        JSONObject responseBody = new JSONObject();
        responseBody.put("total", 0);
        responseBody.put("memories", new JSONArray());

        when(agentRuntimeClient.searchMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(responseBody));

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test query");
        body.setTopK(20);
        body.setThreshold(0.75);

        // When
        memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body);

        // Then — verify parameters
        verify(agentRuntimeClient).searchMemories(eq("repo-1"), eq("test-user"), any(Map.class));
    }

    @Test
    void test_searchMemoryItems_no_user_id_throws() {
        // Given
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn(null);

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test");

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body));
    }

    @Test
    void test_searchMemoryItems_runtime_error_throws() {
        // Given
        when(agentRuntimeClient.searchMemories(any(), any(), anyMap()))
            .thenThrow(new RuntimeException("search failed"));

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test");

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body));
    }

    @Test
    void test_searchMemoryItems_default_parameters() {
        // Given — topK and threshold are null, should use defaults
        JSONObject responseBody = new JSONObject();
        responseBody.put("total", 0);
        responseBody.put("memories", new JSONArray());

        when(agentRuntimeClient.searchMemories(any(), any(), anyMap()))
            .thenReturn(ResponseEntity.ok(responseBody));

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test");
        // topK and threshold are null

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body);

        // Then — should not throw, defaults are used
        assertNotNull(result);
    }
}
