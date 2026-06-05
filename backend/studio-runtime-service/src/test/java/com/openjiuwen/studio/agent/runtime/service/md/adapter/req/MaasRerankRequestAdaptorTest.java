/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.runtime.dto.md.RankDocumentsRequest;
import com.openjiuwen.studio.agent.runtime.http.HttpResponse;
import com.openjiuwen.studio.agent.runtime.util.TestUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MaasRerankRequestAdaptorTest {
    private MaasRerankRequestAdaptor maasRerankRequestAdaptor;

    @BeforeEach
    public void setUp() {
        maasRerankRequestAdaptor = new MaasRerankRequestAdaptor();
    }

    @Test
    public void should_return_maas_rerank_then_get_name() {
        assertEquals("maas_rerank", maasRerankRequestAdaptor.getName());
    }

    @Test
    public void should_return_null_when_convert_request_using_null_obj() {
        assertNull(maasRerankRequestAdaptor.requestBodyConvert(Collections.emptyMap(), null, false));
    }

    @Test
    public void should_do_nothing_when_convert_request_using_un_rerank_request() {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();

        assertEquals(chatCompletionRequest,
            maasRerankRequestAdaptor.requestBodyConvert(Collections.emptyMap(), chatCompletionRequest, false));
    }

    private static final String TEST_MODEL = "rerank";

    private static final String TEST_QUERY = "query";

    private static final List<String> TEST_DOCS = Collections.singletonList("doc");

    private static final int TEST_TOP_N = 10;

    @Test
    public void should_return_converted_rerank_request_when_convert_using_rerank_document_request() {
        RankDocumentsRequest rankDocumentsRequest = new RankDocumentsRequest(TEST_QUERY, TEST_DOCS, TEST_TOP_N);
        rankDocumentsRequest.setModel(TEST_MODEL);

        Object rerankRequest = maasRerankRequestAdaptor.requestBodyConvert(Collections.emptyMap(), rankDocumentsRequest,
            false);

        assertNotNull(rerankRequest);
        assertInstanceOf(MaasRerankRequestAdaptor.RerankRequest.class, rerankRequest);
        assertEquals(TEST_MODEL, ((MaasRerankRequestAdaptor.RerankRequest) rerankRequest).getModel());
        assertEquals(TEST_QUERY, ((MaasRerankRequestAdaptor.RerankRequest) rerankRequest).getQuery());
        assertEquals(TEST_DOCS, ((MaasRerankRequestAdaptor.RerankRequest) rerankRequest).getDocuments());
    }

    private static final String TEST_URL = "https://xxx.com/xxx/xxx";

    @Test
    public void should_throw_exception_when_convert_null_response_body() {
        HttpResponse<String> response = new HttpResponse<>();
        response.setResponseBody(null);

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, new RankDocumentsRequest()));

        assertEquals(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL, exception.getErrorCode());
    }

    @Test
    public void should_throw_exception_when_convert_internal_error_response() {
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setErrorResponseBody("Internal Server Error");

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, new RankDocumentsRequest()));

        assertEquals(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL, exception.getErrorCode());
    }

    @Test
    public void should_throw_exception_when_convert_bad_request_response() {
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setErrorResponseBody("Internal Server Error");

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, new RankDocumentsRequest()));

        assertEquals(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL, exception.getErrorCode());
    }

    @Test
    public void should_throw_exception_when_response_cannot_be_parsed() {
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setResponseBody("invalid json");

        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, new RankDocumentsRequest()));

        assertEquals(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL, exception.getErrorCode());
    }

    @Test
    public void resBodyConvert_should_handle_empty_results_successfully() {
        String jsonResponse = TestUtil.getStringFromFile("classpath:md/adapter/req/rerank_empty_response.json");
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(200);
        response.setResponseBody(jsonResponse);

        Object result = maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, new RankDocumentsRequest());

        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        JSONObject jsonObject = (JSONObject) result;
        assertEquals(0, jsonObject.getJSONArray("results").size());
    }

    @Test
    public void resBodyConvert_should_sort_results_by_index_ascending_and_apply_topN() {
        RankDocumentsRequest rankDocumentsRequest = new RankDocumentsRequest();
        rankDocumentsRequest.setTopN(2);
        rankDocumentsRequest.setQuery(TEST_QUERY);
        rankDocumentsRequest.setModel(TEST_MODEL);

        String jsonResponse = TestUtil.getStringFromFile("classpath:md/adapter/req/rerank_success_response.json");
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(200);
        response.setResponseBody(jsonResponse);

        Object result = maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, rankDocumentsRequest);

        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        JSONObject jsonObject = (JSONObject) result;
        JSONArray results = jsonObject.getJSONArray("results");

        assertEquals(2, results.size());
        // Results should be sorted by index ascending: 0, 1, 2 -> but we take top 2: 0, 1
        assertEquals(0, results.getJSONObject(0).getInteger("index"));
        assertEquals(1, results.getJSONObject(1).getInteger("index"));
    }

    @Test
    public void resBodyConvert_should_handle_null_index_values_safely() {
        RankDocumentsRequest rankDocumentsRequest = new RankDocumentsRequest();
        rankDocumentsRequest.setTopN(3);
        rankDocumentsRequest.setQuery(TEST_QUERY);
        rankDocumentsRequest.setModel(TEST_MODEL);

        String jsonResponse = TestUtil.getStringFromFile(
            "classpath:md/adapter/req/rerank_response_with_null_index.json");
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(200);
        response.setResponseBody(jsonResponse);

        Object result = maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, rankDocumentsRequest);

        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        JSONObject jsonObject = (JSONObject) result;
        JSONArray results = jsonObject.getJSONArray("results");

        assertEquals(3, results.size());
        // Results should be sorted by index ascending: 0, 1, 2 (null values should be excluded)
        assertEquals(0, results.getJSONObject(0).getInteger("index"));
        assertEquals(1, results.getJSONObject(1).getInteger("index"));
        assertEquals(2, results.getJSONObject(2).getInteger("index"));
    }

    @Test
    public void resBodyConvert_should_use_default_topN_when_ThreadLocal_is_not_set() {
        String jsonResponse = TestUtil.getStringFromFile("classpath:md/adapter/req/rerank_success_response.json");
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(200);
        response.setResponseBody(jsonResponse);

        Object result = maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, new RankDocumentsRequest());

        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        JSONObject jsonObject = (JSONObject) result;
        JSONArray results = jsonObject.getJSONArray("results");

        // Should return all results when no topN is set
        assertEquals(3, results.size());
    }

    @Test
    public void resBodyConvert_should_respect_topN_limit_when_it_is_less_than_total_results() {
        RankDocumentsRequest rankDocumentsRequest = new RankDocumentsRequest();
        rankDocumentsRequest.setTopN(1);
        rankDocumentsRequest.setQuery(TEST_QUERY);
        rankDocumentsRequest.setModel(TEST_MODEL);
        maasRerankRequestAdaptor.requestBodyConvert(Collections.emptyMap(), rankDocumentsRequest, false);

        String jsonResponse = TestUtil.getStringFromFile("classpath:md/adapter/req/rerank_success_response.json");
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(200);
        response.setResponseBody(jsonResponse);

        Object result = maasRerankRequestAdaptor.resBodyConvert(TEST_URL, response, rankDocumentsRequest);

        assertNotNull(result);
        assertInstanceOf(JSONObject.class, result);
        JSONObject jsonObject = (JSONObject) result;
        JSONArray results = jsonObject.getJSONArray("results");

        assertEquals(1, results.size());
        assertEquals(0, results.getJSONObject(0).getInteger("index")); // Should be the smallest index
    }
}
