/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.koosearch;

import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.request.SearchTextRequestBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.KooSearchChatReferenceInfo;
import com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response.SearchTextResponseBody;
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

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class ThirdPartyKoosearchService extends AbstractThirdPartyKnowledgeService {

    public static final String APP_CODE = "AppCode";

    private final KooSearchAuthHandler kooSearchAuthHandler;

    private final OkHttpUtils okHttpUtils;

    // 检索指定知识库的文档
    private final static String RETRIEVE_REPO_URI
        = "/{application_id}/v1/{project_id}/applications/{application_id}/uni-search/experience/searchtext";

    public static final String PROJECT_ID = "project_id";

    public static final String APPLICATION_ID = "application_id";

    private static final Map<QueryModeEnum, String> KOO_SEARCH_QUERY_SCOPE_ENUM_MAP = Map.of(QueryModeEnum.DOC, "doc",
        QueryModeEnum.FAQ, "faq", QueryModeEnum.MIX, "mix", QueryModeEnum.KEYWORD, "keyword");

    public ThirdPartyKoosearchService(KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper,
        KooSearchAuthHandler kooSearchAuthHandler, OkHttpUtils okHttpUtils) {
        super(knowledgeBaseConnectionMapper);
        this.kooSearchAuthHandler = kooSearchAuthHandler;
        this.okHttpUtils = okHttpUtils;
    }

    @Override
    public ConnectorTypeEnum type() {
        return ConnectorTypeEnum.KOO_SEARCH;
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
            KOO_SEARCH_QUERY_SCOPE_ENUM_MAP.getOrDefault(QueryModeEnum.valueOf(searchMode.toUpperCase(Locale.ROOT)),
                "doc"));
        searchTextRequestBody.setPageNum(pageNum);
        searchTextRequestBody.setPageSize(pageSize);

        List<KnowledgeBaseConnectionParam> connectorParamDefinitions = getConnectionDefinition(
            knowledgeRepos.get(0).getId());

        Headers httpHeaders = kooSearchAuthHandler.generateAuthRequest(connectorParamDefinitions);
        String host = kooSearchAuthHandler.getHost(connectorParamDefinitions);
        Request.Builder builder = new Request.Builder();
        RequestBody body = RequestBody.create(JsonUtils.encode(searchTextRequestBody), OkHttpUtils.MEDIA_TYPE_JSON);
        HashMap<String, String> map = new HashMap<>();
        map.put(PROJECT_ID, queryValue(PROJECT_ID, connectorParamDefinitions));
        map.put(APPLICATION_ID, queryValue(APPLICATION_ID, connectorParamDefinitions));
        String url = host + replacePathParameters(RETRIEVE_REPO_URI, map);
        Request httpRequest = builder.url(url).post(body).headers(httpHeaders).build();
        SearchTextResponseBody searchTextResponseBody;
        OkHttpClient httpClient = okHttpUtils.getHttpClient();
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String rspBodyStr = response.body() == null ? "" : response.body().string();
            if (response.code() >= 300) {
                log.error("call koosearch error. status:{} url:{}, rsp:{}", response.code(), url, rspBodyStr);
                throw new AgentStudioException("call koosearch error.");
            }
            searchTextResponseBody = JsonUtils.decode(rspBodyStr, SearchTextResponseBody.class);
        } catch (IOException e) {
            log.error("call koosearch error. url:{}", url);
            throw new AgentStudioException("call koosearch error.");
        }
        return getSearchTextResp(searchTextResponseBody);
    }

    @Override
    public List<KnowledgeBaseConnectionParam> EncryptParams(List<KnowledgeBaseConnectionParam> params) {
        List<KnowledgeBaseConnectionParam> paramList = new ArrayList<>();
        params.forEach(item -> {
            if (APP_CODE.equals(item.getCode())) {
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

    private static @NonNull SearchTextResp getSearchTextResp(SearchTextResponseBody response) {
        List<KooSearchChatReferenceInfo> searchResultList;
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

    private String queryValue(String pathParam, List<KnowledgeBaseConnectionParam> connectorParamDefinitions) {
        for (KnowledgeBaseConnectionParam paramDefinition : connectorParamDefinitions) {
            if (paramDefinition.getCode().equals(pathParam)) {

                return paramDefinition.getValue();
            }
        }
        return null;
    }

    public static String replacePathParameters(String urlTemplate, Map<String, String> params) {
        String url = urlTemplate;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!ObjectUtils.isEmpty(entry.getValue())) {
                url = url.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return url;
    }

    private String buildFilterStrForTags(List<String> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return null;
        }
        String tagStr = String.join(" OR ", tags);
        return "tags:(" + tagStr + ")";
    }

}
