/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.memory;

import com.openjiuwen.studio.agent.agentbase.client.CssUniSearchClient;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.ListMemoryResponseBody;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.MemoryInfo;
import com.openjiuwen.studio.agent.agentbase.entity.memory.koosearchentity.SearchMemoryRequestBody;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.BatchDeleteMemoryItemRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryItemResponseBody;
import com.openjiuwen.studio.agent.manager.dto.SearchMemoryItemRequestBody;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class MemoryItemManagementServiceTest {
    @Mock
    private CssUniSearchClient cssUniSearchClient;

    @InjectMocks
    private MemoryItemManagementService memoryItemManagementService;

    MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils;

    @BeforeEach
    void setUp() {
        mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS);
        mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("user_id");

        ReflectionTestUtils.setField(memoryItemManagementService, "lakeSearchEndpoint", "https://search.example.com");
        ReflectionTestUtils.setField(memoryItemManagementService, "applicationId", "app-1");
        ReflectionTestUtils.setField(memoryItemManagementService, "lakeSearchProjectId", "proj-1");
    }

    @AfterEach
    void tearDown() {
        mockedStaticRequestContextUtils.close();
    }

    @Test
    void test_listMemoryItems_returns_mapped_items() {
        // Given
        MemoryInfo info = new MemoryInfo();
        info.setId("mem-1");
        info.setMemory("test content");
        info.setScore(0.95f);
        info.setUserId("user-1");
        info.setAgentId("agent-1");

        ListMemoryResponseBody lsResult = new ListMemoryResponseBody();
        lsResult.setData(List.of(info));

        when(cssUniSearchClient.listMemory(any(URI.class), any(HttpHeaders.class),
            anyString(), anyString(), eq("repo-1"), eq(1), eq(10), any()))
            .thenReturn(lsResult);

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("mem-1", result.getItems().get(0).getId());
        assertEquals("test content", result.getItems().get(0).getContent());
        assertEquals(0.95f, result.getItems().get(0).getScore());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
    }

    @Test
    void test_listMemoryItems_null_response_returns_empty() {
        // Given
        when(cssUniSearchClient.listMemory(any(URI.class), any(HttpHeaders.class),
            anyString(), anyString(), anyString(), any(), any(), any()))
            .thenReturn(null);

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(null, result.getTotal());
    }

    @Test
    void test_deleteMemoryItem_delegates_to_client() {
        // When
        memoryItemManagementService.deleteMemoryItem("project-1", "repo-1", "mem-1");

        // Then
        verify(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
            eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-1"));
    }

    @Test
    void test_batchDeleteMemoryItems_deletes_each_id() {
        // Given
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(List.of("mem-1", "mem-2", "mem-3"));

        // When
        memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body);

        // Then
        verify(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
            eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-1"));
        verify(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
            eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-2"));
        verify(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
            eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-3"));
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
    void test_batchDeleteMemoryItems_partial_failure_continues() {
        // Given — mem-2 删除失败，但 mem-1 和 mem-3 应继续删除
        BatchDeleteMemoryItemRequestBody body = new BatchDeleteMemoryItemRequestBody();
        body.setMemoryIds(List.of("mem-1", "mem-2", "mem-3"));

        doThrow(new RuntimeException("delete failed"))
            .when(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
                eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-2"));

        // When — 不应抛异常
        memoryItemManagementService.batchDeleteMemoryItems("project-1", "repo-1", body);

        // Then — 三条都尝试了删除
        verify(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
            eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-1"));
        verify(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
            eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-2"));
        verify(cssUniSearchClient).deleteMemory(any(URI.class), any(HttpHeaders.class),
            eq("proj-1"), eq("app-1"), eq("repo-1"), eq("mem-3"));
    }

    @Test
    void test_searchMemoryItems_maps_results() {
        // Given
        MemoryInfo info = new MemoryInfo();
        info.setId("mem-s1");
        info.setMemory("search result content");
        info.setScore(0.88f);

        when(cssUniSearchClient.searchMemory(any(URI.class), any(HttpHeaders.class),
            anyString(), anyString(), eq("repo-1"), any(SearchMemoryRequestBody.class)))
            .thenReturn(List.of(info));

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
        assertEquals(0.88f, result.getItems().get(0).getScore());
    }

    @Test
    void test_searchMemoryItems_null_results_returns_empty() {
        // Given
        when(cssUniSearchClient.searchMemory(any(URI.class), any(HttpHeaders.class),
            anyString(), anyString(), anyString(), any(SearchMemoryRequestBody.class)))
            .thenReturn(null);

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
    void test_getUri_throws_when_endpoint_not_configured() {
        // Given
        ReflectionTestUtils.setField(memoryItemManagementService, "lakeSearchEndpoint", "");

        // When / Then
        assertThrows(AgentStudioException.class,
            () -> memoryItemManagementService.listMemoryItems("project-1", "repo-1", 1, 10));
    }

    @Test
    void test_searchMemoryItems_threshold_double_to_float_conversion() {
        // Given -- verify Double threshold is converted to Float for CssUniSearchClient
        when(cssUniSearchClient.searchMemory(any(URI.class), any(HttpHeaders.class),
            anyString(), anyString(), eq("repo-1"), any(SearchMemoryRequestBody.class)))
            .thenReturn(Collections.emptyList());

        SearchMemoryItemRequestBody body = new SearchMemoryItemRequestBody();
        body.setQuery("test");
        body.setThreshold(0.75);

        // When
        ListMemoryItemResponseBody result = memoryItemManagementService.searchMemoryItems("project-1", "repo-1", body);

        // Then -- no exception from type conversion
        assertNotNull(result);
    }
}
