/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.connection.general;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.KnowledgeBaseInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.ListKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.RetrieveQueryResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.SearchChunkInfo;
import com.openjiuwen.studio.agent.foundation.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.foundation.connection.constants.QueryModeEnum;
import com.openjiuwen.studio.agent.foundation.connection.general.GeneralConnector;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalRetrieveResultInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.agent.foundation.connection.model.RetrieveKnowledgeBaseReq;
import com.openjiuwen.studio.agent.foundation.connection.model.knowledgeBaseRetrieval;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
public class GeneralConnectorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectorClient connectorClient;

    @InjectMocks
    private GeneralConnector generalConnector;

    @Mock
    private IRestAuthHandler restAuthHandler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(generalConnector, "authHandler", restAuthHandler);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_listKnowledgeBase_should_return_not_null_when_condition() throws Exception {
        // 准备模拟返回的 ListKnowledgeRepoResponseBody
        ListKnowledgeRepoResponseBody responseBody = new ListKnowledgeRepoResponseBody();
        List<KnowledgeBaseInfo> dataList = new ArrayList<>();
        KnowledgeBaseInfo knowledgeBaseInfo1 = new KnowledgeBaseInfo();
        knowledgeBaseInfo1.setKnowledgeBaseId("repo1");
        knowledgeBaseInfo1.setName("Test Repo");
        knowledgeBaseInfo1.setDescription("Test Description");
        dataList.add(knowledgeBaseInfo1);
        responseBody.setKnowledgeBaseList(dataList);
        responseBody.setTotal(1L);

        when(connectorClient.doRequest(any(), any(), eq(ListKnowledgeRepoResponseBody.class))).thenReturn(responseBody);
        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // When
        PageResult<ExternalKnowledgeBaseInfo> result = generalConnector.listKnowledgeBase(null, "test", 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals("repo1", result.getItems().get(0).getKnowledgeBaseId());
        assertEquals("Test Repo", result.getItems().get(0).getKnowledgeBaseName());
        assertEquals("Test Description", result.getItems().get(0).getDescription());
    }

    @Test
    void test_listKnowledgeBase_should_return_empty_result_when_no_data() throws Exception {
        // 准备空的返回数据
        ListKnowledgeRepoResponseBody responseBody = new ListKnowledgeRepoResponseBody();
        responseBody.setKnowledgeBaseList(null);
        responseBody.setTotal(0L);

        when(connectorClient.doRequest(any(), any(), eq(ListKnowledgeRepoResponseBody.class))).thenReturn(responseBody);
        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // When
        PageResult<ExternalKnowledgeBaseInfo> result = generalConnector.listKnowledgeBase(null, "test", 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        assertEquals(0L, result.getTotalCount());
    }

    @Test
    void test_listKnowledgeBase_should_throws_null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            when(connectorClient.doRequest(any(BasicRequest.class), any(HttpEntity.class),
                eq(ListKnowledgeRepoResponseBody.class))).thenReturn(null);
            // When
            PageResult<ExternalKnowledgeBaseInfo> result = generalConnector.listKnowledgeBase(null, null,
                (Integer) null, (Integer) null);
        });
    }

    @Test
    void test_retrieveKnowledgeBase_should_return_not_null_when_condition() throws Exception {
        // 准备模拟返回的 RetrieveQueryResponseBody
        RetrieveQueryResponseBody result = new RetrieveQueryResponseBody();
        result.setTotal(0L);
        List<SearchChunkInfo> searchResultList = new ArrayList<>();
        SearchChunkInfo searchChunkInfo1 = new SearchChunkInfo();
        searchResultList.add(searchChunkInfo1);
        result.setSearchResultList(searchResultList);

        when(connectorClient.doRequest(any(), any(), eq(RetrieveQueryResponseBody.class))).thenReturn(result);
        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // 准备测试请求
        RetrieveKnowledgeBaseReq request = new RetrieveKnowledgeBaseReq();
        request.setQuery("test query");
        request.setQueryMode(QueryModeEnum.DOC);
        List<String> knowledgeBaseIds = new ArrayList<>();
        knowledgeBaseIds.add("test_id");
        List<knowledgeBaseRetrieval> knowledgeBaseRetrievals = new ArrayList<>();
        knowledgeBaseRetrieval knowledgeBaseRetrieval = new knowledgeBaseRetrieval();
        knowledgeBaseRetrieval.setKnowledgeBaseId("string");
        knowledgeBaseRetrieval.setTagName(new ArrayList<>());
        knowledgeBaseRetrievals.add(knowledgeBaseRetrieval);
        request.setKnowledgeBaseRetrievals(knowledgeBaseRetrievals);

        // When
        PageResult<ExternalRetrieveResultInfo> result1 = generalConnector.retrieveKnowledgeBase(null, request, 0, 10);

        // Then
        assertNotNull(result1);
    }

    @Test
    void test_retrieveKnowledgeBase_should_return_valid_result_when_valid_request() throws Exception {
        // 准备模拟返回的 RetrieveQueryResponseBody
        RetrieveQueryResponseBody mockResponseBody = new RetrieveQueryResponseBody();
        mockResponseBody.setTotal(1L); // 设置总数为1

        // 准备模拟搜索结果列表
        List<SearchChunkInfo> mockSearchResultList = new ArrayList<>();
        SearchChunkInfo mockSearchChunk = new SearchChunkInfo();
        mockSearchChunk.setKnowledgeBaseId("repo1");
        mockSearchChunk.setFileId("file1");
        mockSearchChunk.setChunkId("chunk1");
        mockSearchChunk.setContent("Test Content");
        mockSearchChunk.setScore(0.9F);
        mockSearchChunk.setTitle("Test Title");
        mockSearchResultList.add(mockSearchChunk);
        mockResponseBody.setSearchResultList(mockSearchResultList);

        // 模拟 connectorClient.doRequest 方法返回 mockResponseBody
        when(connectorClient.doRequest(any(), any(), eq(RetrieveQueryResponseBody.class))).thenReturn(mockResponseBody);

        // 模拟 restAuthHandler.generateAuthRequest 方法返回 null
        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // 准备测试请求
        RetrieveKnowledgeBaseReq request = new RetrieveKnowledgeBaseReq();
        request.setQuery("test query");
        request.setQueryMode(QueryModeEnum.DOC);
        List<String> knowledgeBaseIds = new ArrayList<>();
        knowledgeBaseIds.add("mock_id");
        List<knowledgeBaseRetrieval> knowledgeBaseRetrievals = new ArrayList<>();
        knowledgeBaseRetrieval knowledgeBaseRetrieval = new knowledgeBaseRetrieval();
        knowledgeBaseRetrieval.setKnowledgeBaseId("string");
        knowledgeBaseRetrieval.setTagName(new ArrayList<>());
        knowledgeBaseRetrievals.add(knowledgeBaseRetrieval);
        request.setKnowledgeBaseRetrievals(knowledgeBaseRetrievals);

        // 调用被测试方法
        PageResult<ExternalRetrieveResultInfo> result = generalConnector.retrieveKnowledgeBase(null, request, 0, 10);

        // 验证结果不为 null
        assertNotNull(result);

        // 验证 totalCount 是否正确
        assertEquals(1L, result.getTotalCount());

        // 验证 items 是否正确
        assertEquals(1, result.getItems().size());
        ExternalRetrieveResultInfo item = result.getItems().get(0);
        assertEquals("repo1", item.getKnowledgeBaseId());
        assertEquals("file1", item.getFileId());
        assertEquals("chunk1", item.getChunkId());
        assertEquals("Test Content", item.getContent());
        assertEquals(0.9F, item.getScore());
        assertEquals("Test Title", item.getTitle());
    }

    @Test
    void test_retrieveKnowledgeBase_should_throws_null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            when(connectorClient.doRequest(any(BasicRequest.class), any(HttpEntity.class),
                eq(RetrieveQueryResponseBody.class))).thenReturn(null);

            // 准备测试请求
            RetrieveKnowledgeBaseReq request = new RetrieveKnowledgeBaseReq();
            request.setQuery("test query");
            request.setQueryMode(QueryModeEnum.DOC);
            List<String> knowledgeBaseIds = new ArrayList<>();
            knowledgeBaseIds.add("mock_id");
            List<knowledgeBaseRetrieval> knowledgeBaseRetrievals = new ArrayList<>();
            knowledgeBaseRetrieval knowledgeBaseRetrieval = new knowledgeBaseRetrieval();
            knowledgeBaseRetrieval.setKnowledgeBaseId("string");
            knowledgeBaseRetrieval.setTagName(new ArrayList<>());
            knowledgeBaseRetrievals.add(knowledgeBaseRetrieval);
            request.setKnowledgeBaseRetrievals(knowledgeBaseRetrievals);

            // When
            PageResult<ExternalRetrieveResultInfo> result = generalConnector.retrieveKnowledgeBase(null, request,
                (Integer) null, (Integer) null);
        });
    }

    @Test
    void test_listKnowledgeBaseTags_should_return_empty_list() throws Exception {
        // When
        List<?> result = generalConnector.listKnowledgeBaseTags(null, "test_id");

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}