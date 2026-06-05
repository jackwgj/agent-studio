/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.remote;

import com.openjiuwen.studio.prompt.engineering.constant.CommonConstant;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;

/**
 * 功能描述 封装RestTemplate
 *
 */
@Slf4j
public class ClientTemplate {
    private final RestTemplate restTemplate;

    /**
     * 构造函数
     *
     * @param factory
     * @param responseErrorHandler
     * @param interceptors
     */
    public ClientTemplate(ClientHttpRequestFactory factory, ResponseErrorHandler responseErrorHandler,
        ClientHttpRequestInterceptor... interceptors) {
        this.restTemplate = new RestTemplate(factory);
        this.restTemplate.setInterceptors(Arrays.asList(interceptors));
        if (responseErrorHandler != null) {
            this.restTemplate.setErrorHandler(responseErrorHandler);
        }
    }

    /**
     * post请求
     *
     * @param url
     * @param token
     * @param body
     * @param responseType
     * @param <T>
     * @return
     */
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 2), maxAttempts = 2)
    public <T> ResponseEntity<T> postForEntity(String url, String token, String body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        if (Objects.nonNull(token)) {
            headers.set(CommonConstant.X_AUTH_TOKEN, token);
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        log.info("post url={}", url);
        return restTemplate.postForEntity(url, httpEntity, responseType);
    }

    @Retryable(backoff = @Backoff(value = 1000, multiplier = 2), maxAttempts = 2)
    public <T> ResponseEntity<T> postForEntity(String url, HttpHeaders headers, String body, Class<T> responseType) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, headers);
        log.info("post url={}", url);
        return restTemplate.postForEntity(url, httpEntity, responseType);
    }

    /**
     * get请求
     *
     * @param url
     * @param token
     * @param responseType
     * @param <T>
     * @return
     */
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 2), maxAttempts = 2)
    public <T> ResponseEntity<T> getForEntity(String url, String token, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CommonConstant.X_AUTH_TOKEN, token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        log.info("get url={}", url);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);
    }

    /**
     * 外部传入headers，get请求
     *
     * @param url
     * @param headers
     * @param responseType
     * @param <T>
     * @return
     */
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 2), maxAttempts = 2)
    public <T> ResponseEntity<T> getForEntityWithHeaders(String url, HttpHeaders headers, Class<T> responseType) {
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        log.info("get url={}", url);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);
    }

    /**
     * put请求
     *
     * @param url
     * @param token
     * @param body
     * @param responseType
     * @param <T>
     * @return
     */
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 2), maxAttempts = 2)
    public <T> ResponseEntity<T> putForEntity(String url, String token, String body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(CommonConstant.X_AUTH_TOKEN, token);
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        log.info("put url={}", url);
        return restTemplate.exchange(url, HttpMethod.PUT, httpEntity, responseType);
    }

    /**
     * delete请求
     *
     * @param url
     * @param token
     * @param responseType
     * @param <T>
     * @return
     */
    @Retryable(backoff = @Backoff(value = 1000, multiplier = 2), maxAttempts = 2)
    public <T> ResponseEntity<T> deleteForEntity(String url, String token, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(CommonConstant.X_AUTH_TOKEN, token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        log.info("delete url={}", url);
        return restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, responseType);
    }
}
