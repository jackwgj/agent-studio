/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.connection.koosearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.KnowledgeRepoBasicInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.KooSearchChatReferenceInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.ListKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.SearchTextResponseBody;
import com.openjiuwen.studio.agent.foundation.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.foundation.connection.constants.QueryModeEnum;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.connection.koosearch.KooSearchConnector;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalRetrieveResultInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.agent.foundation.connection.model.KnowledgeBaseRetrieval;
import com.openjiuwen.studio.agent.foundation.connection.model.RetrieveKnowledgeBaseReq;

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
public class KooSearchConnectorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectorClient connectorClient;

    @InjectMocks
    private KooSearchConnector kooSearchConnector;

    @Mock
    private IRestAuthHandler restAuthHandler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(kooSearchConnector, "authHandler", restAuthHandler);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_retrieveKnowledgeBase_should_return_not_null_when_condition() throws Exception {
        SearchTextResponseBody result = new SearchTextResponseBody();
        result.setTotal(0L);
        List<KooSearchChatReferenceInfo> docList = new ArrayList<>();
        KooSearchChatReferenceInfo kooSearchChatReferenceInfo1 = new KooSearchChatReferenceInfo();
        docList.add(kooSearchChatReferenceInfo1);
        result.setDocList(docList);
        when(connectorClient.doRequest(any(), any(), eq(SearchTextResponseBody.class))).thenReturn(result);
        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        RetrieveKnowledgeBaseReq request = new RetrieveKnowledgeBaseReq();
        List<String> knowledgeBaseId = new ArrayList<>();
        knowledgeBaseId.add("string");
        request.setQuery("string");
        request.setQueryMode(QueryModeEnum.DOC);
        List<KnowledgeBaseRetrieval> knowledgeBaseRetrievals = new ArrayList<>();
        KnowledgeBaseRetrieval knowledgeBaseRetrieval = new KnowledgeBaseRetrieval();
        knowledgeBaseRetrieval.setKnowledgeBaseId("string");
        knowledgeBaseRetrieval.setTagName(new ArrayList<>());
        knowledgeBaseRetrievals.add(knowledgeBaseRetrieval);
        request.setKnowledgeBaseRetrievals(knowledgeBaseRetrievals);
        // When
        PageResult<ExternalRetrieveResultInfo> result1 = kooSearchConnector.retrieveKnowledgeBase(null, request, 0, 10);

        // Then
        assertNotNull(result1);
    }

    @Test
    void test_retrieveKnowledgeBase_should_return_valid_result_when_valid_request() throws Exception {
        // 准备模拟返回的 SearchTextResponseBody
        SearchTextResponseBody mockResponseBody = new SearchTextResponseBody();
        mockResponseBody.setTotal(1L); // 设置总数为1

        // 准备模拟文档列表
        List<KooSearchChatReferenceInfo> mockDocList = new ArrayList<>();
        KooSearchChatReferenceInfo mockDoc = new KooSearchChatReferenceInfo();
        mockDoc.setRepoId("repo1");
        mockDoc.setTitle("Test Title");
        mockDoc.setContent("Test Content");
        mockDoc.setScore(0.9F);
        mockDoc.setFileId("file1");
        mockDoc.setChunkId("chunk1");
        mockDocList.add(mockDoc);
        mockResponseBody.setDocList(mockDocList);

        // 模拟 connectorClient.doRequest 方法返回 mockResponseBody
        when(connectorClient.doRequest(any(), any(), eq(SearchTextResponseBody.class))).thenReturn(mockResponseBody);

        // 模拟 restAuthHandler.generateAuthRequest 方法返回 null
        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // 准备测试请求
        RetrieveKnowledgeBaseReq request = new RetrieveKnowledgeBaseReq();
        request.setQuery("string");
        request.setQueryMode(QueryModeEnum.DOC);
        List<KnowledgeBaseRetrieval> knowledgeBaseRetrievals = new ArrayList<>();
        KnowledgeBaseRetrieval knowledgeBaseRetrieval = new KnowledgeBaseRetrieval();
        knowledgeBaseRetrieval.setKnowledgeBaseId("mock_id");
        knowledgeBaseRetrieval.setTagName(new ArrayList<>());
        knowledgeBaseRetrievals.add(knowledgeBaseRetrieval);
        request.setKnowledgeBaseRetrievals(knowledgeBaseRetrievals);

        // 调用被测试方法
        PageResult<ExternalRetrieveResultInfo> result = kooSearchConnector.retrieveKnowledgeBase(null, request, 0, 10);

        // 验证结果不为 null
        assertNotNull(result);

        // 验证 totalCount 是否正确
        assertEquals(1L, result.getTotalCount());

        // 验证 items 是否正确
        assertEquals(1, result.getItems().size());
        ExternalRetrieveResultInfo item = result.getItems().get(0);
        assertEquals("repo1", item.getKnowledgeBaseId());
        assertEquals("Test Title", item.getTitle());
        assertEquals("Test Content", item.getContent());
        assertEquals(0.9F, item.getScore());
        assertEquals("file1", item.getFileId());
        assertEquals("chunk1", item.getChunkId());
    }

    @Test
    void test_listKnowledgeBase_should_return_not_null_when_condition() throws Exception {
        ListKnowledgeRepoResponseBody responseBody = new ListKnowledgeRepoResponseBody();
        List<KnowledgeRepoBasicInfo> dataList = new ArrayList<>();
        KnowledgeRepoBasicInfo knowledgeRepoBasicInfo1 = new KnowledgeRepoBasicInfo();
        dataList.add(knowledgeRepoBasicInfo1);
        responseBody.setDataList(dataList);
        responseBody.setTotal(0L);
        when(connectorClient.doRequest(any(), any(), eq(ListKnowledgeRepoResponseBody.class))).thenReturn(responseBody);

        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // When
        PageResult<ExternalKnowledgeBaseInfo> result = kooSearchConnector.listKnowledgeBase(null, null, 0, 10);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_listKnowledgeBase_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            when(connectorClient.doRequest(any(BasicRequest.class), any(HttpEntity.class),
                eq(ListKnowledgeRepoResponseBody.class))).thenReturn(null);
            // When
            PageResult<ExternalKnowledgeBaseInfo> result = kooSearchConnector.listKnowledgeBase(null, null,
                (Integer) null, (Integer) null);
        });
    }

    @Test
    void test_retrieveKnowledgeBase_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            when(connectorClient.doRequest(any(BasicRequest.class), any(HttpEntity.class),
                eq(SearchTextResponseBody.class))).thenReturn(null);

            // When
            PageResult<ExternalRetrieveResultInfo> result = kooSearchConnector.retrieveKnowledgeBase(null, null,
                (Integer) null, (Integer) null);
        });
    }

    @Test
    void test_extractImageId_should_return_not_null_when_condition() throws Exception {

        // When
        String chunk
            = "{img-e665b264428c402ba12451206a6363f1}\n    相比较而言，离地球第二近的火星似乎是最温和也是最像地球的行星了";
        List<String> result = ReflectionTestUtils.invokeMethod(kooSearchConnector, "extractImageId", chunk);
        // Then
        assertEquals(1, result.size());
    }
}
