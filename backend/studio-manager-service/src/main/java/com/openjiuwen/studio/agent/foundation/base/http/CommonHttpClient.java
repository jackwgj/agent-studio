/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.base.http;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplateHandler;

import java.util.Map;
import java.util.Optional;

/**
 * 公用Http请求函数
 *
 * @since 2025-12-22
 */
@Slf4j
@Service
public class CommonHttpClient {

    private final SmartRestTemplate smartRestTemplate;

    private static final UriTemplateHandler URI_TEMPLATE_HANDLER = initUriTemplateHandler();

    public CommonHttpClient(@Qualifier("smartRestTemplate") SmartRestTemplate smartRestTemplate) {
        this.smartRestTemplate = smartRestTemplate;
    }

    /**
     * 执行请求
     *
     * @param requestEntity
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T doRequest(RequestEntity requestEntity, Class<T> clazz) {

        String urlWithQuery = buildCompleteUrl(requestEntity);
        HttpEntity<Object> requestWithHeaderAndBody = new HttpEntity<>(requestEntity.getBody(),
            requestEntity.getHeaders());

        ResponseEntity<T> obj = null;
        try {
            obj = smartRestTemplate.exchange(urlWithQuery, requestEntity.getMethod(), requestWithHeaderAndBody, clazz);
        } catch (RestClientException ex) {
            log.error("[External knowledge base API Connection failed,reason:{}]", ex.getMessage(), ex);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, ex);
        }

        return Optional.ofNullable(obj).map(ResponseEntity::getBody).orElse(null);
    }

    private static UriTemplateHandler getUriTemplateHandler() {
        return URI_TEMPLATE_HANDLER;
    }

    private static DefaultUriBuilderFactory initUriTemplateHandler() {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory();
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.URI_COMPONENT);
        return uriFactory;
    }

    /**
     * 将host、uri、query param和path variable拼接成一个完整的url
     *
     * @param requestEntity
     * @return
     */
    public static String buildCompleteUrl(RequestEntity requestEntity) {
        String urlWithRequestParams = buildUrlWithRequestParam(requestEntity.getHost() + requestEntity.getUri(),
            Optional.of(requestEntity).map(RequestEntity::getRequestParams).orElse(null));

        Optional<Map<String, Object>> pathVariable = Optional.of(requestEntity).map(RequestEntity::getPathVariables);
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
        if (ObjectUtils.isEmpty(params)) {
            return url;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (ObjectUtils.isEmpty(entry.getValue())) {
                // 如果是null或者是空字符串，则不拼接到url上
                continue;
            }
            builder.queryParam(entry.getKey(), entry.getValue().toString());
        }
        return builder.build().toUriString();
    }
}
