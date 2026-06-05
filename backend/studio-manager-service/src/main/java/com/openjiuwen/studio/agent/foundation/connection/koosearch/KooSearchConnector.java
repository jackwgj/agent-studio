/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.koosearch;

import com.google.common.collect.Maps;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.request.SearchTextRequestBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.KnowledgeRepoBasicInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.KooSearchChatReferenceInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.ListKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.SearchTextResponseBody;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对接KooSearch的连接实现
 *
 * @since 2025-12-1
 */
public class KooSearchConnector extends AbstractKnowledgeBaseConnector {

    // 获取知识库列表
    private final static String LIST_KNOWLEDGE_REPO_URI
        = "/{application_id}/v1/{project_id}/applications/{application_id}/uni-search/knowledge-repo";

    // 检索指定知识库的文档
    private final static String RETRIEVE_REPO_URI
        = "/{application_id}/v1/{project_id}/applications/{application_id}/uni-search/experience/searchtext";

    // KooSearch知识库类型：专享
    private static final String EXCLUSIVE = "exclusive";

    // KooSearch知识库状态：开启
    private static final String KOOSEARCH_STATUS_OPEN = "OPEN";

    // KooSearch切片中图片id字符串的规则
    private static final String IMAGE_ID_PATTERN_STR = "\\{(img-[a-z0-9-]+)}";

    private static final Pattern IMAGE_ID_PATTERN = Pattern.compile(IMAGE_ID_PATTERN_STR, Pattern.CASE_INSENSITIVE);

    private static final Map<QueryModeEnum, String> KOO_SEARCH_QUERY_SCOPE_ENUM_MAP = Map.of(QueryModeEnum.DOC, "doc",
        QueryModeEnum.FAQ, "faq", QueryModeEnum.MIX, "mix", QueryModeEnum.KEYWORD, "keyword");

    public KooSearchConnector(ConnectorClient connectorClient) {
        super(connectorClient, new KooSearchAuthHandler());
    }

    // 查询KooSearch的知识库列表
    @Override
    public PageResult<ExternalKnowledgeBaseInfo> listKnowledgeBase(ConnectorDefinition connectorDefinition, String name,
        Integer offset, Integer limit) {
        Map<String, Object> queryParam = Maps.newHashMap();
        queryParam.put("name", name);
        int pageNum = offset / limit + 1;
        int pageSize = limit;
        queryParam.put("page_num", pageNum);
        queryParam.put("page_size", pageSize);
        queryParam.put("repo_type", EXCLUSIVE);
        queryParam.put("status", KOOSEARCH_STATUS_OPEN);

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
            ObjectUtils.firstNonNull(result.getDataList(), Collections.<KnowledgeRepoBasicInfo>emptyList())
                .stream()
                .map(item -> {
                    ExternalKnowledgeBaseInfo externalKnowledgeBaseInfo = new ExternalKnowledgeBaseInfo();
                    externalKnowledgeBaseInfo.setKnowledgeBaseId(item.getId());
                    externalKnowledgeBaseInfo.setKnowledgeBaseName(item.getName());
                    externalKnowledgeBaseInfo.setDescription(item.getDetail());
                    return externalKnowledgeBaseInfo;
                })
                .toList());
        return response;
    }

    @Override
    public PageResult<ExternalRetrieveResultInfo> retrieveKnowledgeBase(ConnectorDefinition connectorDefinition,
        RetrieveKnowledgeBaseReq request, Integer offset, Integer limit) {
        SearchTextRequestBody searchTextRequestBody = new SearchTextRequestBody();
        searchTextRequestBody.setContent(request.getQuery());
        searchTextRequestBody.setRepoId(request.getKnowledgeBaseIds().get(0));
        searchTextRequestBody.setExtraRepoIds(request.getKnowledgeBaseIds());
        searchTextRequestBody.setFilterString(buildFilterStrForTags(request.getAllTags()));
        searchTextRequestBody.setScope(KOO_SEARCH_QUERY_SCOPE_ENUM_MAP.getOrDefault(request.getQueryMode(), "doc"));
        int pageNum = offset / limit + 1;
        int pageSize = limit;
        searchTextRequestBody.setPageNum(pageNum);
        searchTextRequestBody.setPageSize(pageSize);

        RequestEntity requestEntity = RequestEntity.builder().body(JacksonUtils.toJson(searchTextRequestBody)).build();
        BasicRequest basicRequest = BasicRequest.builder()
            .uri(RETRIEVE_REPO_URI)
            .method(HttpMethod.POST)
            .requestEntity(requestEntity)
            .build();
        HttpEntity<Object> httpEntity = generateAuthRequest(basicRequest, connectorDefinition);
        SearchTextResponseBody result = this.connectorClient.doRequest(basicRequest, httpEntity,
            SearchTextResponseBody.class);

        PageResult<ExternalRetrieveResultInfo> response = new PageResult<>();
        response.setTotalCount(result.getTotal());
        response.setItems(
            ObjectUtils.firstNonNull(result.getDocList(), Collections.<KooSearchChatReferenceInfo>emptyList())
                .stream()
                .map(item -> {
                    ExternalRetrieveResultInfo retrieveResultInfo = new ExternalRetrieveResultInfo();
                    retrieveResultInfo.setKnowledgeBaseId(item.getRepoId());
                    retrieveResultInfo.setTitle(item.getTitle());
                    retrieveResultInfo.setContent(item.getContent());
                    retrieveResultInfo.setScore(item.getScore());
                    retrieveResultInfo.setFileId(item.getFileId());
                    retrieveResultInfo.setChunkId(item.getChunkId());
                    List<String> imageIds = extractImageId(item.getContent());
                    if (!CollectionUtils.isEmpty(imageIds)) {
                        retrieveResultInfo.setContent(removeImageId(item.getContent()));
                    }
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

    /**
     * 根据标签列表生成过滤条件
     *
     * @param tags
     * @return
     */
    private String buildFilterStrForTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        String tagStr = String.join(" OR ", tags);
        return "tags:(" + tagStr + ")";
    }

    /**
     * 从切片内容中提取并且移除图片的ID
     * (适用于KooSearch中的切片内容)
     *
     * @param chunk
     * @return
     */
    private List<String> extractImageId(String chunk) {
        List<String> ids = new ArrayList<>();
        if (StringUtils.isEmpty(chunk)) {
            return ids;
        }
        Matcher matcher = IMAGE_ID_PATTERN.matcher(chunk);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    /**
     * 从切片内容中移除图片的ID
     * (适用于KooSearch中的切片内容)
     *
     * @param chunk
     * @return
     */
    private String removeImageId(String chunk) {
        if (StringUtils.isEmpty(chunk)) {
            return chunk;
        }
        return IMAGE_ID_PATTERN.matcher(chunk).replaceAll("");
    }

}
