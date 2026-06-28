/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.connection.lakeserach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.knowledge.LakeSearchChatReferenceInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.KnowledgeBaseTagsResponse;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.KnowledgeRepoBasicInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.LakeSearchTagInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.ListKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.SearchTextResponseBody;
import com.openjiuwen.studio.agent.foundation.connection.IRestAuthHandler;
import com.openjiuwen.studio.agent.foundation.connection.constants.QueryModeEnum;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.connection.lakeserach.LakeSearchConnector;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalRetrieveResultInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.KnowledgeBaseTagInfo;
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
class LakeSearchConnectorTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectorClient connectorClient;

    @InjectMocks
    private LakeSearchConnector lakeSearchConnector;

    @Mock
    private IRestAuthHandler restAuthHandler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(lakeSearchConnector, "authHandler", restAuthHandler);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_listKnowledgeBase_should_return_not_null_when_condition() throws Exception {
        ListKnowledgeRepoResponseBody result = new ListKnowledgeRepoResponseBody();
        List<KnowledgeRepoBasicInfo> dataList = new ArrayList<>();
        KnowledgeRepoBasicInfo knowledgeRepoBasicInfo1 = new KnowledgeRepoBasicInfo();
        dataList.add(knowledgeRepoBasicInfo1);
        result.setDataList(dataList);
        result.setTotal(0L);
        when(connectorClient.doRequest(any(), any(), eq(ListKnowledgeRepoResponseBody.class))).thenReturn(result);

        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // When
        PageResult<ExternalKnowledgeBaseInfo> result1 = lakeSearchConnector.listKnowledgeBase(null, null, 0, 10);

        // Then
        assertNotNull(result1);
    }

    @Test
    void test_listKnowledgeBase_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            when(connectorClient.doRequest(any(BasicRequest.class), any(HttpEntity.class),
                eq(ListKnowledgeRepoResponseBody.class))).thenReturn(null);
            // When
            PageResult<ExternalKnowledgeBaseInfo> result = lakeSearchConnector.listKnowledgeBase(null, null,
                (Integer) null, (Integer) null);
        });
    }

    @Test
    void test_retrieveKnowledgeBase_should_return_not_null_when_condition() throws Exception {
        SearchTextResponseBody result = new SearchTextResponseBody();
        result.setTotal(0L);
        List<LakeSearchChatReferenceInfo> docList = new ArrayList<>();
        LakeSearchChatReferenceInfo lakeSearchChatReferenceInfo1 = new LakeSearchChatReferenceInfo();
        docList.add(lakeSearchChatReferenceInfo1);
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
        PageResult<ExternalRetrieveResultInfo> result1 = lakeSearchConnector.retrieveKnowledgeBase(null, request, 0, 10);

        // Then
        assertNotNull(result1);
    }

    @Test
    void test_retrieveKnowledgeBase_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            when(connectorClient.doRequest(any(BasicRequest.class), any(HttpEntity.class),
                eq(SearchTextResponseBody.class))).thenReturn(null);

            // When
            PageResult<ExternalRetrieveResultInfo> result = lakeSearchConnector.retrieveKnowledgeBase(null, null,
                (Integer) null, (Integer) null);
        });
    }

    @Test
    void test_listKnowledgeBaseTags_should_return_not_null_when_condition() throws Exception {
        KnowledgeBaseTagsResponse result = new KnowledgeBaseTagsResponse();
        List<LakeSearchTagInfo> labelList = new ArrayList<>();
        LakeSearchTagInfo lakeSearchTagInfo1 = new LakeSearchTagInfo();
        labelList.add(lakeSearchTagInfo1);
        result.setLabelList(labelList);
        when(connectorClient.doRequest(any(), any(), eq(KnowledgeBaseTagsResponse.class))).thenReturn(result);
        when(restAuthHandler.generateAuthRequest(any(), any())).thenReturn(null);

        // When
        List<KnowledgeBaseTagInfo> result1 = lakeSearchConnector.listKnowledgeBaseTags(null, "string");

        // Then
        assertNotNull(result1);
    }

    @Test
    void test_listKnowledgeBaseTags_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            when(connectorClient.doRequest(any(BasicRequest.class), any(HttpEntity.class),
                eq(KnowledgeBaseTagsResponse.class))).thenReturn(null);

            // When
            List<KnowledgeBaseTagInfo> result = lakeSearchConnector.listKnowledgeBaseTags(null, null);
        });
    }

    @Test
    void test_buildFilterStrForTags_should_equal_result_when_condition() throws Exception {
        // Given
        List<String> tags = new ArrayList<>();
        tags.add("string1");
        tags.add("string2");

        // When
        String result = ReflectionTestUtils.invokeMethod(lakeSearchConnector, "buildFilterStrForTags", tags);

        // Then
        assertEquals("tags:(string1 OR string2)", result);
    }

    @Test
    void test_extractImageId_should_return_not_null_when_condition() throws Exception {

        // When
        String chunk
            = "{img-e665b264428c402ba12451206a6363f1}\n    相比较而言，离地球第二近的火星似乎是最温和也是最像地球的行星了";
        List<String> result = ReflectionTestUtils.invokeMethod(lakeSearchConnector, "extractImageId", chunk);
        // Then
        assertEquals(1, result.size());
    }

    @Test
    void test_removeImageId_should_equal_result_when_condition() throws Exception {

        String chunk
            = "{img-e665b264428c402ba12451206a6363f1}\n    相比较而言，离地球第二近的火星似乎是最温和也是最像地球的行星了";

        String result = ReflectionTestUtils.invokeMethod(lakeSearchConnector, "removeImageId", chunk);

        // Then
        assertFalse(result.contains("{img-e665b264428c402ba12451206a6363f1}"));
    }
}