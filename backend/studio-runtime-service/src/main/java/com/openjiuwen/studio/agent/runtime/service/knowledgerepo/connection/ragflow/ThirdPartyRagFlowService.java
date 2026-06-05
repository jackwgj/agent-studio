/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.knowledgerepo.connection.ragflow;


import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.openjiuwen.studio.agent.common.dto.knowledge.ragflow.request.RetrieveChunksRequestBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.ragflow.response.RagFlowCommonResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.ragflow.response.RetrieveChunksResponseBody;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.KnowledgeRepo;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;
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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service("MyOutsideRagFlow")
public class ThirdPartyRagFlowService extends AbstractThirdPartyKnowledgeService {

    private final static String RETRIEVE_CHUNKS_URI = "/api/v1/retrieval";

    public static final String DOC_TYPE_KWD_IMAGE = "image";

    public static final String DETAIL_PAGE_PARAM = "knowledge_base_detail_url";

    public static final String EXTERNAL_ID_PLACE_HOLDER = "{{id}}";

    private final static String IMAGE_PATH_URI = "/v1/document/image/";

    public static final String APIKEY = "APIKey";

    private final RagFlowAuthHandler ragFlowAuthHandler;

    private final OkHttpUtils okHttpUtils;

    public ThirdPartyRagFlowService(KnowledgeBaseConnectionMapper knowledgeBaseConnectionMapper,
        RagFlowAuthHandler ragFlowAuthHandler, OkHttpUtils okHttpUtils) {
        super(knowledgeBaseConnectionMapper);
        this.okHttpUtils = okHttpUtils;
        this.ragFlowAuthHandler = ragFlowAuthHandler;
    }

    @Override
    public ConnectorTypeEnum type() {
        return ConnectorTypeEnum.RAG_FLOW;
    }

    @Override
    public SearchTextResp searchText(List<KnowledgeRepo> knowledgeRepos, String query, String searchMode,
        List<String> tags, Float recallThreshold, Integer pageNum, Integer pageSize) {
        RetrieveChunksRequestBody retrieveChunksRequestBody = new RetrieveChunksRequestBody();
        retrieveChunksRequestBody.setQuestion(query);
        retrieveChunksRequestBody.setDatasetIds(
            knowledgeRepos.stream().map(KnowledgeRepo::getKnowledgeRepoId).toList());
        retrieveChunksRequestBody.setPage(pageNum);
        retrieveChunksRequestBody.setPageSize(pageSize);

        List<KnowledgeBaseConnectionParam> connectorParamDefinitions = getConnectionDefinition(
            knowledgeRepos.get(0).getId());
        String host = ragFlowAuthHandler.getHost(connectorParamDefinitions);
        Headers httpHeaders = ragFlowAuthHandler.generateAuthRequest(connectorParamDefinitions);

        Request.Builder builder = new Request.Builder();
        RequestBody body = RequestBody.create(JsonUtils.encode(retrieveChunksRequestBody), OkHttpUtils.MEDIA_TYPE_JSON);
        String url = host + RETRIEVE_CHUNKS_URI;
        Request httpRequest = builder.url(url).post(body).headers(httpHeaders).build();
        RagFlowCommonResponseBody<RetrieveChunksResponseBody> ragFlowCommonResponseBody;
        OkHttpClient httpClient = okHttpUtils.getHttpClient();
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String rspBodyStr = response.body() == null ? "" : response.body().string();
            if (response.code() >= 300) {
                log.error("Request ragflow fail. status:{} url:{}, rsp:{}", response.code(), url, rspBodyStr);
                throw new AgentStudioException("Request ragflow fail.");
            }
            ragFlowCommonResponseBody = JsonUtils.decode(rspBodyStr, new TypeReference<>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        } catch (IOException e) {
            log.error("Request ragflow fail. url:{}", url);
            throw new AgentStudioException("Request ragflow fail.");
        }
        String ragFlowConsoleUrl = getRagFlowConsoleHost(connectorParamDefinitions);
        List<ChatReferenceInfo> list = new ArrayList<>();
        if (ragFlowCommonResponseBody != null) {
            RetrieveChunksResponseBody retrieveChunksResponseBody = ragFlowCommonResponseBody.getData();
            retrieveChunksResponseBody.getChunks().forEach(chunkInfo -> {
                ChatReferenceInfo chatReferenceInfo = new ChatReferenceInfo();
                chatReferenceInfo.setContent(chunkInfo.getContent());
                chatReferenceInfo.setRepoId(chunkInfo.getDatasetId());
                chatReferenceInfo.setFileId(chunkInfo.getDocumentId());
                chatReferenceInfo.setChunkId(chunkInfo.getId());
                chatReferenceInfo.setContent(chunkInfo.getContent());
                chatReferenceInfo.setScore(chunkInfo.getSimilarity());
                chatReferenceInfo.setTitle(chunkInfo.getDocumentKeyword());
                if (DOC_TYPE_KWD_IMAGE.equalsIgnoreCase(chunkInfo.getDocTypeKwd()) && StringUtils.isNotEmpty(
                    chunkInfo.getImageId())) {
                    chatReferenceInfo.setImageIds(Lists.newArrayList(chunkInfo.getImageId()));
                    chatReferenceInfo.setImagePaths(
                        Lists.newArrayList(generateImagePath(ragFlowConsoleUrl, chunkInfo.getImageId())));
                }
                list.add(chatReferenceInfo);
            });
        }
        SearchTextResp searchTextResp = new SearchTextResp();
        searchTextResp.setDocList(list);
        return searchTextResp;
    }

    @Override
    public List<KnowledgeBaseConnectionParam> EncryptParams(List<KnowledgeBaseConnectionParam> params) {
        List<KnowledgeBaseConnectionParam> paramList = new ArrayList<>();
        params.forEach(item -> {
            if (APIKEY.equals(item.getCode())) {
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

    /**
     * 获取RagFlow的console访问地址
     */
    private String getRagFlowConsoleHost(List<KnowledgeBaseConnectionParam> connectorParamDefinitions) {
        Optional<KnowledgeBaseConnectionParam> detailPageUrlOption = connectorParamDefinitions.stream()
            .filter(item -> StringUtils.equals(DETAIL_PAGE_PARAM, item.getCode()))
            .findFirst();
        if (detailPageUrlOption.isEmpty()) {
            return null;
        }
        String detailPageUrl = detailPageUrlOption.get().getValue().replace(EXTERNAL_ID_PLACE_HOLDER, "");
        URI uri = URI.create(detailPageUrl);
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    /**
     * 生成图片的查看路径
     *
     */
    private String generateImagePath(String consoleUrl, String imageId) {
        return consoleUrl + IMAGE_PATH_URI + imageId;
    }
}
