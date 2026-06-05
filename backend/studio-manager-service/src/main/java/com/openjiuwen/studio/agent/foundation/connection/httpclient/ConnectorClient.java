/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.httpclient;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.base.http.RequestEntity;
import com.openjiuwen.studio.agent.foundation.base.http.SmartRestTemplate;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * 第三方知识库连接客户端
 *
 * @since 2025-08-23
 */
@Slf4j
@Service
public class ConnectorClient {

    private final SmartRestTemplate smartRestTemplate;

    private static UriTemplateHandler uriTemplateHandler;

    public ConnectorClient(@Qualifier("smartRestTemplate") SmartRestTemplate smartRestTemplate) {
        this.smartRestTemplate = smartRestTemplate;
    }

    /**
     * 执行请求
     *
     * @param basicRequest
     * @param requestEntity
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T doRequest(BasicRequest basicRequest, HttpEntity<Object> requestEntity, Class<T> clazz) {
        return doRequest(buildCompleteUrl(basicRequest), basicRequest.getMethod(), clazz, requestEntity).getBody();
    }

    /**
     * 调用API
     *
     * @param url
     * @param method
     * @param clazz
     * @param requestEntity
     * @param <T>
     * @return
     */
    private <T> ResponseEntity<T> doRequest(String url, HttpMethod method, Class<T> clazz,
        HttpEntity<Object> requestEntity) {
        ResponseEntity<T> obj = null;

        try {
            obj = smartRestTemplate.exchange(url, method, requestEntity, clazz);
        } catch (RestClientException ex) {
            log.error("[External knowledge base API Connection failed,reason:{}]", ex.getMessage(), ex);
            throw new AgentBaseException(ErrorCode.CALL_EXTERNAL_KNOWLEDGE_ERROR);
        }
        return obj;
    }

    private static UriTemplateHandler getUriTemplateHandler() {
        if (uriTemplateHandler == null) {
            uriTemplateHandler = initUriTemplateHandler();
        }
        return uriTemplateHandler;
    }

    private static DefaultUriBuilderFactory initUriTemplateHandler() {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        return uriFactory;
    }

    /**
     * 将query param和path variable拼接成一个完整的url
     *
     * @param basicRequest
     * @return
     */
    public static String buildCompleteUrl(BasicRequest basicRequest) {
        String urlWithRequestParams = buildUrlWithRequestParam(basicRequest.getHost() + basicRequest.getUri(),
            Optional.of(basicRequest)
                .map(BasicRequest::getRequestEntity)
                .map(RequestEntity::getRequestParams)
                .orElse(null));

        Optional<Map<String, Object>> pathVariable = Optional.of(basicRequest)
            .map(BasicRequest::getRequestEntity)
            .map(RequestEntity::getPathVariables);
        if (pathVariable.isPresent()) {
            urlWithRequestParams = getUriTemplateHandler().expand(urlWithRequestParams, pathVariable.get()).toString();
        }
        return urlWithRequestParams;
    }

    /**
     * 将requestParam拼接到url上
     *
     * @param url
     * @param params
     * @return
     */
    private static String buildUrlWithRequestParam(String url, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() == null || StringUtils.isEmpty(entry.getValue().toString())) {
                // 如果是null或者是空字符串，则不拼接到url上
                continue;
            }
            builder.queryParam(entry.getKey(), URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        // 注意在toUriString时不会对url中的特殊字符进行URL编码处理，所以需要在上面拼接param参数时手动对参数进行URL编码
        return builder.build().toUriString();
    }
}
