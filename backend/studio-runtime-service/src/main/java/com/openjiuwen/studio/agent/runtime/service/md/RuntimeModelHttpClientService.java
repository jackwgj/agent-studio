/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

@Slf4j
@Service
public class RuntimeModelHttpClientService implements InitializingBean {
    private CloseableHttpClient httpClient;

    private CloseableHttpClient proxyHttpClient;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${model.request.timeout:900000}")
    private int modelRequestTimeout;

    @Value("${model.request.connection-timeout:10000}")
    private int connectionTimeOut;

    @Value("${model.request.maxTotal:200}")
    private int maxTotal;

    @Value("${model.request.defaultMaxPerRoute:50}")
    private int defaultMaxPerRoute;

    @Value("${model.request.proxy.open:false}")
    private boolean proxyOpen;

    @Value("${model.request.proxy.user:}")
    private String proxyUser;

    @Value("${model.request.proxy.password:}")
    private String proxyPassword;

    @Value("${model.request.proxy.port:8080}")
    private int proxyPort;

    @Value("${model.request.proxy.host:}")
    private String proxyHost;

    @Value("#{'${model.request.proxy.no-host:127.0.0.1}'.split(',')}")
    private Set<String> noProxyHost;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // SSL 配置
            SSLContext sslContext = SSLContextBuilder.create()
                .setSecureRandom(SecureRandom.getInstanceStrong())
                .loadTrustMaterial(null, (x509Certificates, s) -> true)
                .build();
            // 创建TLS策略
            final DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext,
                new String[] {"TLSv1.2", "TLSv1.3"}, // 推荐包含 TLSv1.3
                null, SSLBufferMode.STATIC, NoopHostnameVerifier.INSTANCE);
            // 配置连接池
            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                    .setConnectTimeout(connectionTimeOut, TimeUnit.SECONDS)
                    .setSocketTimeout(connectionTimeOut, TimeUnit.SECONDS)
                    .build())
                .build();
            // 设置最大连接数
            connectionManager.setMaxTotal(maxTotal);
            // 设置每个路由的最大连接数
            connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
            // 创建 HttpClient 实例
            HttpClientBuilder builder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .disableRedirectHandling()
                .setDefaultRequestConfig(getRequestConfig())
                .evictIdleConnections(TimeValue.ofSeconds(20));
            httpClient = builder.build();

            if (isRequestProxyOpen()) {
                HttpHost proxy = new HttpHost("http", proxyHost, proxyPort);
                // 凭据提供器
                BasicCredentialsProvider credentialsProvider = null;
                if (!StringUtils.isAnyBlank(proxyUser, proxyPassword)) {
                    credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(new AuthScope(proxy),
                        new UsernamePasswordCredentials(proxyUser, proxyPassword.toCharArray()));
                }
                // 路由规划器 + 凭据
                DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
                builder.setRoutePlanner(routePlanner);
                HttpClientBuilder pxBuilder = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(getRequestConfig())
                    .setProxy(proxy)
                    .disableRedirectHandling()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .evictIdleConnections(TimeValue.ofSeconds(20));
                proxyHttpClient = pxBuilder.build();
            }
        } catch (Exception e) {
            if (e instanceof AgentStudioException) {
                throw e;
            }
            log.error("Fail create http client.", e);
        }
    }

    private boolean isRequestProxyOpen() {
        if (!proxyOpen) {
            log.info("Model request proxy is close.");
            return false;
        }
        if (StringUtils.isAnyBlank(proxyHost)) {
            log.warn("Model request proxy config is empty. proxy can not use.");
            proxyOpen = false;
            return false;
        }
        return true;
    }

    public RequestConfig getRequestConfig() {
        return RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofSeconds(connectionTimeOut)) // 请求连接超时时间
            .build();
    }

    public CloseableHttpClient getModelRequestHttpClient(String host) {
        if (!proxyOpen) {
            return httpClient;
        }
        for (String pattern : noProxyHost) {
            if (antPathMatcher.match(pattern.trim(), host)) {
                return httpClient;
            }
        }
        return proxyHttpClient;
    }

    public Map<String, String> header2Map(Header[] allHeaders) {
        HashMap<String, String> headersMap = new HashMap<>();
        for (Header header : allHeaders) {
            if (!headersMap.containsKey(header.getName())) {
                headersMap.put(header.getName(), header.getValue());
            }
        }
        return headersMap;
    }

    public Map<String, String> header2Map(Headers okHeaders) {
        HashMap<String, String> headersMap = new HashMap<>();
        if (okHeaders == null) {
            return headersMap;
        }
        for (int i = 0; i < okHeaders.size(); i++) {
            String name = okHeaders.name(i);
            String value = okHeaders.value(i);
            if (!headersMap.containsKey(name)) {
                headersMap.put(name, value);
            }
        }
        return headersMap;
    }
}
