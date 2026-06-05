/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.remote;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * 功能描述
 *
 */
@SuppressWarnings("deprecation")
@Configuration
@Slf4j
public class ClientHttpRequestFactoryConfig {
    /**
     * 最大连接数
     */
    @Value("${http.maxTotal}")
    private Integer maxTotal;

    /**
     * 单个路由最大连接数
     */
    @Value("${http.defaultMaxPerRoute}")
    private Integer defaultMaxPerRoute;

    /**
     * 连接超时时间
     */
    @Value("${http.connectTimeout}")
    private Integer connectTimeout;

    /**
     * 获取连接超时时间
     */
    @Value("${http.connectionRequestTimeout}")
    private Integer connectionRequestTimeout;

    /**
     * 响应超时时间
     */
    @Value("${http.socketTimeout}")
    private Integer socketTimeout;

    /**
     * 最大空间
     */
    @Value("${http.validateAfterInactivity}")
    private Integer validateAfterInactivity;

    /**
     * 最大重试次数
     */
    @Value("${http.retryMaxCount}")
    private Integer retryMaxCount;

    /**
     * 重试间隔
     */
    @Value("${http.retryInterval}")
    private Integer retryInterval;

    @Bean("customRestTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory());
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public HttpClient httpClient() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            // 使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
            builder.loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true);
            Registry<ConnectionSocketFactory> registry = null;
            registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", new SSLConnectionSocketFactory(builder.build(), new String[] {"TLSv1.2"}, null,
                    NoopHostnameVerifier.INSTANCE))
                .build();
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
            connectionManager.setMaxTotal(maxTotal);
            connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionRequestTimeout))
                .build();
            return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryStrategy(httpRequestRetryStrategy())
                .setConnectionManager(connectionManager)
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error("init httpClient error {}", e.getMessage());
            throw new AgentStudioException(StudioError.INIT_HTTP_CLIENT_ERROR);
        }
    }

    @Bean
    public HttpRequestRetryStrategy httpRequestRetryStrategy() {
        return new HttpRequestRetryStrategy() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean retryRequest(HttpRequest request, IOException exception, int execCount,
                HttpContext context) {
                // curRetryCount 每一次都会递增，从1开始
                if (execCount > retryMaxCount) {
                    return false;
                }
                try {
                    // 重试延迟
                    Thread.sleep((long) execCount * retryInterval);
                } catch (InterruptedException e) {
                    log.error("http request retry handler interrupted message is {}", e.getMessage());
                }
                if (exception instanceof ConnectTimeoutException || exception instanceof NoHttpResponseException
                    || exception instanceof ConnectException) {
                    return true;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest req = clientContext.getRequest();
                // //如果请求不是关闭连接的请求，则进行重试操作
                return false;
            }

            @Override
            public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
                int statusCode = response.getCode();
                return
                    (statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR || statusCode == HttpStatus.SC_TOO_MANY_REQUESTS)
                        && execCount <= retryMaxCount;
            }

            @Override
            public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
                return TimeValue.ofMilliseconds(retryInterval);
            }
        };
    }
}
