/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.dto.md.RankDocumentsRequest;
import com.openjiuwen.studio.agent.runtime.http.HttpResponse;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.req.mapper.MaasRequestMapper;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MaasRerankRequestAdaptor
 * 针对MAAS-Rerank模型调用的适配器, 完成模型请求参数的转换等;
 *
 */
@Slf4j
@Component
public class MaasRerankRequestAdaptor extends AbstractRequestAdapter {
    private static final Comparator<RerankResult> RERANK_RESULT_COMPARATOR = Comparator.comparing(
        (RerankResult result) -> result.getIndex() == null ? Integer.MAX_VALUE : result.getIndex());

    @Override
    public String getName() {
        return "maas_rerank";
    }

    @Override
    public Object requestBodyConvert(Map<String, String> headers, Object body, boolean stream) {
        if (!(body instanceof RankDocumentsRequest)) {
            log.error("Not a 'RankDocumentRequest', do none conversation.");
            return body;
        }

        RerankRequest request = MaasRequestMapper.INSTANCE.toMaasRerankRequest((RankDocumentsRequest) body);
        log.info("Got convert request: {}.", request);
        return request;
    }

    @Override
    public JSONObject resBodyConvert(String url, HttpResponse<String> response, Object request) {
        if (!(request instanceof RankDocumentsRequest)) {
            throw new AgentStudioException(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL);
        }

        Integer topN = ((RankDocumentsRequest) request).getTopN();

        return resBodyConvertWithTopN(url, response, topN);
    }

    private JSONObject resBodyConvertWithTopN(String url, HttpResponse<String> response, Integer topN) {
        if (response.getResponseBody() == null) {
            log.error("Empty http response! Api path: '{}'", url);
            throw new AgentStudioException(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL);
        }
        if (response.getStatusCode() >= 300) {
            log.error("Invoke model service error. url:{} status:{} response:{}", url, response.getStatusCode(),
                response.getErrorResponseBody());
            throw httpExceptionHandler(response.getStatusCode(), response.getErrorResponseBody());
        }

        RerankResp rerankResp = JsonUtils.json2ObjQuietly(response.getResponseBody(), RerankResp.class);
        if (rerankResp == null) {
            throw new AgentStudioException(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL);
        }

        List<RerankResult> results = rerankResp.getResults();

        int limit = topN == null ? results.size() : Math.min(topN, results.size());
        results = results.stream().sorted(RERANK_RESULT_COMPARATOR).limit(limit).collect(Collectors.toList());
        rerankResp.setResults(results);

        return JSONObject.parseObject(JsonUtils.toJson(rerankResp));
    }

    /**
     * MAAS rerank 模型请求参数
     *
     */
    @Data
    public static final class RerankRequest {
        private String model;

        private String query;

        private List<String> documents;
    }

    /**
     * MAAS rerank 模型请求返回
     *
     */
    @Data
    public static final class RerankResp {
        private String id;

        private String model;

        private RerankUsage rerankUsage;

        private List<RerankResult> results;
    }

    /**
     * RerankResult
     *
     */
    @Data
    public static final class RerankResult {
        private Integer index;

        private Document document;

        @JsonProperty("relevance_score")
        private Double relevanceScore;
    }

    /**
     * Document
     *
     */
    @Data
    public static final class Document {
        private String text;
    }

    /**
     * RerankUsage
     *
     */
    @Data
    public static final class RerankUsage {
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
