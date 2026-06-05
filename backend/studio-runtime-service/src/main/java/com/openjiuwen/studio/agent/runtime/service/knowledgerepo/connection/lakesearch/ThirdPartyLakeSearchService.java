/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.lakesearch;


import com.openjiuwen.studio.agent.common.dto.knowledge.LakeSearchChatReferenceInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.LakeSearchQueryScopeEnum;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.request.SearchTextRequestBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response.SearchTextResponseBody;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.KnowledgeRepo;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.runtime.enums.QueryModeEnum;

import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseConnectionMapper;
import com.openjiuwen.studio.agent.runtime.rce.model.ChatReferenceInfo;
import com.openjiuwen.studio.agent.runtime.rce.model.SearchTextResp;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider.AbstractThirdPartyKnowledgeService;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service("MyOutsideLakeSearch")
public class ThirdPartyLakeSearchService extends AbstractThirdPartyKnowledgeService {

    // 检索指定知识库的文档
    private final static String RETRIEVE_REPO_URI
        = "/v1/1ed40ceefc8d40f8b884edb6a84e7768/applications/fb9731ab-7085-474fb6c7-64473586f0f3/uni-search/experience/searchtext";

    public static final String PASSWORD = "PASSWORD";

    private final LakeSearchAuthHandler lakeSearchAuthHandler;


    private final OkHttpUtils okHttpUtils;

    private static final Map<QueryModeEnum, LakeSearchQueryScopeEnum> LAKE_SEARCH_QUERY_SCOPE_ENUM_MAP
        = new HashMap<>() {
        {
            put(QueryModeEnum.DOC, LakeSearchQueryScopeEnum.DOC);
            put(QueryModeEnum.FAQ, LakeSearchQueryScopeEnum.FAQ);
            put(QueryModeEnum.MIX, LakeSearchQueryScopeEnum.MIX);
            put(QueryModeEnum.KEYWORD, LakeSearchQueryScopeEnum.KEYWORD);
        }
    };

    public ThirdPartyLakeSearchService(KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper,
        LakeSearchAuthHandler lakeSearchAuthHandler,  OkHttpUtils okHttpUtils) {
        super(knowledgeBaseConnectionMapper);
        this.lakeSearchAuthHandler = lakeSearchAuthHandler;
        this.okHttpUtils = okHttpUtils;
    }

    @Override
    public ConnectorTypeEnum type() {
        return ConnectorTypeEnum.LAKE_SEARCH;
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, Float recallThreshold, Integer pageNum, Integer pageSize) {
        SearchTextRequestBody searchTextRequestBody = new SearchTextRequestBody();
        searchTextRequestBody.setContent(query);
        searchTextRequestBody.setRepoId(knowledgeRepos.get(0).getKnowledgeRepoId());
        searchTextRequestBody.setExtraRepoIds(knowledgeRepos.stream().map(KnowledgeRepo::getKnowledgeRepoId).toList());
        searchTextRequestBody.setFilterString(buildFilterStrForTags(tags));
        searchTextRequestBody.setScope(
            LAKE_SEARCH_QUERY_SCOPE_ENUM_MAP.getOrDefault(QueryModeEnum.valueOf(searchMode.toUpperCase(Locale.ROOT)),
                LakeSearchQueryScopeEnum.DOC).getName());
        searchTextRequestBody.setPageNum(pageNum);
        searchTextRequestBody.setPageSize(pageSize);
        List<KnowledgeBaseConnectionParam> connectorParamDefinitions = getConnectionDefinition(
            knowledgeRepos.get(0).getId());
        String host = lakeSearchAuthHandler.getHost(connectorParamDefinitions);
        Headers httpHeaders = lakeSearchAuthHandler.generateAuthRequest(connectorParamDefinitions);

        Request.Builder builder = new Request.Builder();
        RequestBody body = RequestBody.create(JsonUtils.encode(searchTextRequestBody), OkHttpUtils.MEDIA_TYPE_JSON);
        String url = host + RETRIEVE_REPO_URI;
        Request httpRequest = builder.url(url).post(body).headers(httpHeaders).build();
        SearchTextResponseBody searchTextResponseBody;
        OkHttpClient httpClient = okHttpUtils.getHttpClient();
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String rspBodyStr = response.body() == null ? "" : response.body().string();
            if (response.code() >= 300) {
                log.error("call lakesearch error. status:{} url:{}, rsp:{}", response.code(), url, rspBodyStr);
                throw new AgentStudioException("call lakesearch error.");
            }
            searchTextResponseBody = JsonUtils.decode(rspBodyStr, SearchTextResponseBody.class);
        } catch (IOException e) {
            log.error("call lakesearch error. url:{}", url);
            throw new AgentStudioException("call lakesearch error.");
        }
        return getSearchTextResp(searchTextResponseBody);
    }

    @Override
    public List<KnowledgeBaseConnectionParam> EncryptParams(List<KnowledgeBaseConnectionParam> params) {
        List<KnowledgeBaseConnectionParam> paramList = new ArrayList<>();
        params.forEach(item -> {
            if (PASSWORD.equals(item.getCode())) {
                KnowledgeBaseConnectionParam knowledgeBaseConnectionParam = new KnowledgeBaseConnectionParam();
                knowledgeBaseConnectionParam.setCode(item.getCode());
                knowledgeBaseConnectionParam.setValue(CryptoUtils.encrypt(item.getValue()));
                paramList.add(knowledgeBaseConnectionParam);
            } else {
                paramList.add(item);
            }
        });
        return paramList;
    }

    private SearchTextResp getSearchTextResp(SearchTextResponseBody response) {
        List<LakeSearchChatReferenceInfo> searchResultList;
        List<ChatReferenceInfo> list = new ArrayList<>();
        if (response != null) {
            searchResultList = response.getDocList();
            searchResultList.forEach(searchResult -> {
                ChatReferenceInfo chatReferenceInfo = new ChatReferenceInfo();
                chatReferenceInfo.setContent(searchResult.getContent());
                chatReferenceInfo.setRepoId(searchResult.getRepoId());
                chatReferenceInfo.setFileId(searchResult.getFileId());
                chatReferenceInfo.setChunkId(searchResult.getChunkId());
                chatReferenceInfo.setContent(searchResult.getContent());
                chatReferenceInfo.setScore(searchResult.getScore());
                chatReferenceInfo.setTitle(searchResult.getTitle());
                list.add(chatReferenceInfo);
            });
        }
        SearchTextResp searchTextResp = new SearchTextResp();
        searchTextResp.setDocList(list);
        return searchTextResp;
    }

    /**
     * 根据标签列表生成过滤条件
     *
     */
    private String buildFilterStrForTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        String tagStr = String.join(" OR ", tags);
        return "tags:(" + tagStr + ")";
    }
}
