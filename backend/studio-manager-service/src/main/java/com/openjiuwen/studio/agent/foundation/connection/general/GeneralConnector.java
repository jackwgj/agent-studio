/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.general;

import com.google.common.collect.Maps;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.request.RetrieveQueryRequestBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.KnowledgeBaseInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.ListKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.RetrieveQueryResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.SearchChunkInfo;
import com.openjiuwen.studio.agent.foundation.base.utils.JacksonUtils;
import com.openjiuwen.studio.agent.foundation.connection.AbstractKnowledgeBaseConnector;
import com.openjiuwen.studio.agent.foundation.connection.ConnectorDefinition;
import com.openjiuwen.studio.agent.foundation.connection.constants.QueryModeEnum;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.BasicRequest;
import com.openjiuwen.studio.agent.foundation.connection.httpclient.ConnectorClient;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalRetrieveResultInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.KnowledgeBaseTagInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.agent.foundation.connection.model.RetrieveKnowledgeBaseReq;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GeneralConnector extends AbstractKnowledgeBaseConnector {

    // 获取知识库列表URI
    private final static String LIST_KNOWLEDGE_REPO_URI = "/knowledge-bases";

    // 检索知识库URI
    private final static String RETRIEVE_KNOWLEDGE_REPO_URI = "/knowledge-bases/retrieve";

    public GeneralConnector(ConnectorClient connectorClient) {
        super(connectorClient, new GeneralAuthHandler());
    }

    private static final Map<QueryModeEnum, String> GENERAL_QUERY_METHOD_ENUM_MAP = Map.of(QueryModeEnum.DOC, "doc",
        QueryModeEnum.FAQ, "faq", QueryModeEnum.MIX, "mix", QueryModeEnum.KEYWORD, "keyword");

    @Override
    public PageResult<ExternalKnowledgeBaseInfo> listKnowledgeBase(ConnectorDefinition connectorDefinition, String name,
        Integer offset, Integer limit) {
        Map<String, Object> queryParam = Maps.newHashMap();
        queryParam.put("name", name);
        queryParam.put("offset", offset);
        queryParam.put("limit", limit);

        RequestEntity requestEntity = RequestEntity.builder().requestParams(queryParam).build();
        BasicRequest basicRequest = BasicRequest.builder()
            .uri(LIST_KNOWLEDGE_REPO_URI)
            .method(HttpMethod.GET)
            .requestEntity(requestEntity)
            .build();
        HttpEntity<Object> request = generateAuthRequest(basicRequest, connectorDefinition);

        ListKnowledgeRepoResponseBody result = this.connectorClient.doRequest(basicRequest, request,
            ListKnowledgeRepoResponseBody.class);

        PageResult<ExternalKnowledgeBaseInfo> response = new PageResult<>();
        response.setTotalCount(result.getTotal());
        response.setItems(
            ObjectUtils.firstNonNull(result.getKnowledgeBaseList(), Collections.<KnowledgeBaseInfo>emptyList())
                .stream()
                .map(item -> {
                    ExternalKnowledgeBaseInfo externalKnowledgeBaseInfo = new ExternalKnowledgeBaseInfo();
                    externalKnowledgeBaseInfo.setKnowledgeBaseId(item.getKnowledgeBaseId());
                    externalKnowledgeBaseInfo.setKnowledgeBaseName(item.getName());
                    externalKnowledgeBaseInfo.setDescription(item.getDescription());
                    return externalKnowledgeBaseInfo;
                })
                .toList());
        return response;
    }

    @Override
    public PageResult<ExternalRetrieveResultInfo> retrieveKnowledgeBase(ConnectorDefinition connectorDefinition,
        RetrieveKnowledgeBaseReq request, Integer offset, Integer limit) {
        RetrieveQueryRequestBody requestBody = new RetrieveQueryRequestBody();
        requestBody.setKnowledgeBaseIds(request.getKnowledgeBaseIds());
        requestBody.setQuery(request.getQuery());
        requestBody.setMethod(GENERAL_QUERY_METHOD_ENUM_MAP.getOrDefault(request.getQueryMode(),
            GENERAL_QUERY_METHOD_ENUM_MAP.get(QueryModeEnum.DOC)));
        requestBody.setOffset(offset);
        requestBody.setLimit(limit);
        Optional.ofNullable(request.getTopK()).ifPresent(requestBody::setTopK);
        Optional.ofNullable(request.getSearchThreshold()).ifPresent(requestBody::setSearchThreshold);

        RequestEntity requestEntity = RequestEntity.builder().body(JacksonUtils.toJson(requestBody)).build();
        BasicRequest basicRequest = BasicRequest.builder()
            .uri(RETRIEVE_KNOWLEDGE_REPO_URI)
            .method(HttpMethod.POST)
            .requestEntity(requestEntity)
            .build();
        HttpEntity<Object> httpEntity = generateAuthRequest(basicRequest, connectorDefinition);

        RetrieveQueryResponseBody result = this.connectorClient.doRequest(basicRequest, httpEntity,
            RetrieveQueryResponseBody.class);

        PageResult<ExternalRetrieveResultInfo> response = new PageResult<>();
        response.setTotalCount(result.getTotal());
        response.setItems(
            ObjectUtils.firstNonNull(result.getSearchResultList(), Collections.<SearchChunkInfo>emptyList())
                .stream()
                .map(item -> {
                    ExternalRetrieveResultInfo retrieveResultInfo = new ExternalRetrieveResultInfo();
                    retrieveResultInfo.setKnowledgeBaseId(item.getKnowledgeBaseId());
                    retrieveResultInfo.setFileId(item.getFileId());
                    retrieveResultInfo.setChunkId(item.getChunkId());
                    retrieveResultInfo.setContent(item.getContent());
                    retrieveResultInfo.setScore(item.getScore());
                    retrieveResultInfo.setTitle(item.getTitle());
                    return retrieveResultInfo;
                })
                .toList());
        return response;
    }

    @Override
    public List<KnowledgeBaseTagInfo> listKnowledgeBaseTags(ConnectorDefinition connectorDefinition,
        String knowledgeBaseId) {
        return List.of();
    }
}
