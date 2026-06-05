/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.general;


import com.openjiuwen.studio.agent.common.dto.knowledge.general.request.RetrieveQueryRequestBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.RetrieveQueryResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.general.response.SearchChunkInfo;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.KnowledgeRepo;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;
import com.openjiuwen.studio.agent.runtime.enums.QueryModeEnum;
import com.openjiuwen.studio.agent.runtime.mapper.KnowledgeBaseConnectionMapper;

import com.openjiuwen.studio.agent.runtime.rce.model.ChatReferenceInfo;
import com.openjiuwen.studio.agent.runtime.rce.model.SearchTextResp;
import com.openjiuwen.studio.agent.runtime.service.knowledgerepo.knowledgesourceprovider.AbstractThirdPartyKnowledgeService;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service("MyOutsideGeneralKnowledge")
public class ThirdPartyGeneralKnowledgeService extends AbstractThirdPartyKnowledgeService {

    // 检索知识库URI
    private final static String RETRIEVE_KNOWLEDGE_REPO_URI = "/knowledge-bases/retrieve";

    private static final Map<QueryModeEnum, String> GENERAL_QUERY_METHOD_ENUM_MAP = Map.of(QueryModeEnum.DOC, "doc",
        QueryModeEnum.FAQ, "faq", QueryModeEnum.MIX, "mix", QueryModeEnum.KEYWORD, "keyword");

    public static final String API_KEY = "apiKey";

    private final OkHttpUtils okHttpUtils;

    private final GeneralAuthHandler generalAuthHandler;

    public ThirdPartyGeneralKnowledgeService(KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper,
        OkHttpUtils okHttpUtils,  GeneralAuthHandler generalAuthHandler) {
        super(knowledgeBaseConnectionMapper);
        this.okHttpUtils = okHttpUtils;
        this.generalAuthHandler = generalAuthHandler;
    }

    @Override
    public ConnectorTypeEnum type() {
        return ConnectorTypeEnum.GENERAL;
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, Float recallThreshold, Integer pageNum, Integer pageSize) {

        RetrieveQueryRequestBody requestBody = new RetrieveQueryRequestBody();
        requestBody.setKnowledgeBaseIds(getKnowledgeBaseIds(knowledgeRepos));
        requestBody.setQuery(query);
        requestBody.setMethod(GENERAL_QUERY_METHOD_ENUM_MAP.getOrDefault(QueryModeEnum.valueOf(searchMode.toUpperCase(
                Locale.ROOT)),
            GENERAL_QUERY_METHOD_ENUM_MAP.get(QueryModeEnum.DOC)));
        requestBody.setOffset((pageNum - 1) * pageSize);
        requestBody.setLimit(pageSize);
        requestBody.setTopK(pageSize);
        requestBody.setSearchThreshold(recallThreshold);

        List<KnowledgeBaseConnectionParam> connectorParamDefinitions = getConnectionDefinition(
            knowledgeRepos.get(0).getId());

        String host = generalAuthHandler.getHost(connectorParamDefinitions);
        Headers httpHeaders = generalAuthHandler.generateAuthRequest(connectorParamDefinitions);
        OkHttpClient httpClient = okHttpUtils.getHttpClient();
        Request.Builder builder = new Request.Builder();
        RequestBody body = RequestBody.create(JsonUtils.encode(requestBody), OkHttpUtils.MEDIA_TYPE_JSON);
        String url = host + RETRIEVE_KNOWLEDGE_REPO_URI;
        Request httpRequest = builder.url(url).post(body).headers(httpHeaders).build();
        RetrieveQueryResponseBody retrieveQueryResponseBody;
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String rspBodyStr = response.body() == null ? "" : response.body().string();
            if (response.code() >= 300) {
                log.error("call general error. status:{} url:{}, rsp:{}", response.code(), url, rspBodyStr);
                throw new AgentStudioException("call general error.");
            }
            retrieveQueryResponseBody = JsonUtils.decode(rspBodyStr, RetrieveQueryResponseBody.class);
        } catch (IOException e) {
            log.error("call general error. url:{}", url);
            throw new AgentStudioException("call general error.");
        }
        return getSearchTextResp(retrieveQueryResponseBody);

    }

    @Override
    public List<KnowledgeBaseConnectionParam> EncryptParams(List<KnowledgeBaseConnectionParam> params) {
        List<KnowledgeBaseConnectionParam> paramList = new ArrayList<>();
        params.forEach(item -> {
            if (API_KEY.equals(item.getCode())) {
                KnowledgeBaseConnectionParam knowledgeBaseConnectionParam = new KnowledgeBaseConnectionParam();
                knowledgeBaseConnectionParam.setCode(item.getCode());
                if (StringUtils.hasLength(item.getValue())) {
                    knowledgeBaseConnectionParam.setValue(CryptoUtils.encrypt(item.getValue()));
                }
                paramList.add(knowledgeBaseConnectionParam);
            } else {
                paramList.add(item);
            }
        });
        return paramList;
    }

    private static @NonNull SearchTextResp getSearchTextResp(RetrieveQueryResponseBody response) {
        List<SearchChunkInfo> searchResultList;
        List<ChatReferenceInfo> list = new ArrayList<>();
        if (response != null) {
            searchResultList = response.getSearchResultList();
            searchResultList.forEach(searchResult -> {
                ChatReferenceInfo chatReferenceInfo = new ChatReferenceInfo();
                chatReferenceInfo.setContent(searchResult.getContent());
                chatReferenceInfo.setRepoId(searchResult.getKnowledgeBaseId());
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

    public List<String> getKnowledgeBaseIds(List<KnowledgeRepo> knowledgeRepos) {
        return knowledgeRepos.stream().map(KnowledgeRepo::getKnowledgeRepoId).toList();
    }

}
