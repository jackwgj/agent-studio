/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.lakeserach;

import com.google.common.collect.Maps;
import com.openjiuwen.studio.agent.common.dto.knowledge.LakeSearchChatReferenceInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.LakeSearchQueryScopeEnum;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.request.RequestParamConstants;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.request.SearchTextRequestBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.KnowledgeBaseTagsResponse;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.KnowledgeRepoBasicInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.LakeSearchTagInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.ListKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.SearchTextResponseBody;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对接LakeSearch的连接实现
 * LakeSearch接口中，项目Id和应用ID都是常量，因此直接定义到uri上
 *
 * @since 2025-08-22
 */
public class LakeSearchConnector extends AbstractKnowledgeBaseConnector {

    // 查询LakeSearch中当前已经创建的知识库
    private final static String LIST_KNOWLEDGE_REPO_URI
        = "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/knowledge-repo";

    // 检索指定知识库的文档
    private final static String RETRIEVE_REPO_URI
        = "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/experience/searchtext";

    // 查询知识库标签
    private final static String LIST_TAGS_URI =
        "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/{"
            + RequestParamConstants.REPO_ID + "}/labels";

    // LakeSearch系统的图片查看地址
    private final static String IMAGE_PATH_URI
        = "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/img/";

    // LakeSearch切片中图片id字符串的规则
    private static final String IMAGE_ID_PATTERN_STR = "\\{(img-[a-z0-9-]+)}";

    private static final Pattern IMAGE_ID_PATTERN = Pattern.compile(IMAGE_ID_PATTERN_STR, Pattern.CASE_INSENSITIVE);

    private static final int MAX_TAG_QUERY_COUNT = 65535;

    private static final Map<QueryModeEnum, LakeSearchQueryScopeEnum> LAKE_SEARCH_QUERY_SCOPE_ENUM_MAP
        = new HashMap<>() {
        {
            put(QueryModeEnum.DOC, LakeSearchQueryScopeEnum.DOC);
            put(QueryModeEnum.FAQ, LakeSearchQueryScopeEnum.FAQ);
            put(QueryModeEnum.MIX, LakeSearchQueryScopeEnum.MIX);
            put(QueryModeEnum.KEYWORD, LakeSearchQueryScopeEnum.KEYWORD);
        }
    };

    public LakeSearchConnector(ConnectorClient connectorClient) {
        super(connectorClient, new LakeSearchAuthHandler());
    }

    @Override
    public PageResult<ExternalKnowledgeBaseInfo> listKnowledgeBase(ConnectorDefinition connectorDefinition, String name,
        Integer offset, Integer limit) {
        Map<String, Object> queryParam = Maps.newHashMap();
        queryParam.put(RequestParamConstants.NAME, name);
        int pageNum = offset / limit + 1;
        int pageSize = limit;
        queryParam.put(RequestParamConstants.PAGE, pageNum);
        queryParam.put(RequestParamConstants.PAGE_SIZE, pageSize);

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
        searchTextRequestBody.setScope(
            LAKE_SEARCH_QUERY_SCOPE_ENUM_MAP.getOrDefault(request.getQueryMode(), LakeSearchQueryScopeEnum.DOC)
                .getName());
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
            ObjectUtils.firstNonNull(result.getDocList(), Collections.<LakeSearchChatReferenceInfo>emptyList())
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
                        // LakeSearch的图片访问地址不能做到在浏览器直接访问，此接口本质上是下载一个二进制流，没办法给用户使用，作为第三方知识库时先忽略图片
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
        Map<String, Object> queryParam = Maps.newHashMap();
        queryParam.put(RequestParamConstants.PAGE, 1);
        queryParam.put(RequestParamConstants.PAGE_SIZE, MAX_TAG_QUERY_COUNT);
        Map<String, Object> pathVariable = Maps.newHashMap();
        pathVariable.put(RequestParamConstants.REPO_ID, knowledgeBaseId);

        RequestEntity requestEntity = RequestEntity.builder()
            .requestParams(queryParam)
            .pathVariables(pathVariable)
            .build();
        BasicRequest basicRequest = BasicRequest.builder()
            .uri(LIST_TAGS_URI)
            .method(HttpMethod.GET)
            .requestEntity(requestEntity)
            .build();
        HttpEntity<Object> httpEntity = generateAuthRequest(basicRequest, connectorDefinition);
        KnowledgeBaseTagsResponse result = this.connectorClient.doRequest(basicRequest, httpEntity,
            KnowledgeBaseTagsResponse.class);

        return ObjectUtils.firstNonNull(result.getLabelList(), Collections.<LakeSearchTagInfo>emptyList())
            .stream()
            .map(item -> {
                KnowledgeBaseTagInfo tagInfo = new KnowledgeBaseTagInfo();
                tagInfo.setId(item.getId());
                tagInfo.setName(item.getName());
                return tagInfo;
            })
            .toList();
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
