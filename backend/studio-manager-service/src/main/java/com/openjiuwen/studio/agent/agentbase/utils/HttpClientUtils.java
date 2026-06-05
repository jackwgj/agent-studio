package com.openjiuwen.studio.agent.agentbase.utils;/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import com.openjiuwen.studio.agent.foundation.base.http.CustomHostnameVerify;

import lombok.Getter;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.util.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 获取HttpClient
 *
 * @since 2025-06-14
 */
public class HttpClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    // 总最大连接数
    private static final int MAX_TOTAL_CONNECTIONS = 200;

    // 单路由默认最大连接数
    private static final int DEFAULT_MAX_PER_ROUTE = 50;

    // socket发送缓冲区大小，lakeSearch kerberos认证的上传文件场景要求使用
    private static final int SND_BUF_SIZE = 50000000;

    // 获取 HttpClient 实例（单例模式）
    @Getter
    private static CloseableHttpClient httpClient;

    @Value("${hostname.verify.filter:}")
    private static final List<String> hostnames = new ArrayList<>();

    private static final List<String> SAFE_TLS_VERSIONS;

    private static final List<String> SAFE_TLS_CIPHERS;

    public static String[] getSafeTlsVersion() {

        return SAFE_TLS_VERSIONS.toArray(String[]::new);
    }

    public static String[] getSafeTlsCiphers() {

        return SAFE_TLS_CIPHERS.toArray(String[]::new);
    }

    private HttpClientUtils() {
    }

    static {
        SAFE_TLS_VERSIONS = Arrays.asList("TLSv1.2", "TLSv1.3");
        SAFE_TLS_CIPHERS = Arrays.asList("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
        try {
            init();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("Fail to init httpClient for invoking lakeSearch with kerberos, [{}]", e.getMessage(), e);
        }
    }

    private static void init() throws NoSuchAlgorithmException, KeyManagementException {
        logger.info("start to init httpClient for invoking lakeSearch with kerberos");
        BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
        basicCredentialsProvider.setCredentials(new AuthScope(null, -1),
            new UsernamePasswordCredentials("", "".toCharArray()));
        SSLContext sslContext = createIgnoreVerifySsl();
        CustomHostnameVerify customHostnameVerify = new CustomHostnameVerify();
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
            .setMaxConnPerRoute(DEFAULT_MAX_PER_ROUTE)
            .setDefaultSocketConfig(SocketConfig.custom().setSndBufSize(SND_BUF_SIZE).build())
            .setTlsSocketStrategy(
                new DefaultClientTlsStrategy(sslContext, getSafeTlsVersion(), getSafeTlsCiphers(), SSLBufferMode.STATIC,
                    customHostnameVerify.CustomVerifyHostname(hostnames)))
            .build();
        httpClient = HttpClients.custom()
            .setDefaultCredentialsProvider(basicCredentialsProvider)
            .setConnectionManager(connectionManager)
            .evictIdleConnections(TimeValue.of(30, TimeUnit.MINUTES))
            .useSystemProperties()
            .disableRedirectHandling()
            .build();
    }

    public static SSLContext createIgnoreVerifySsl() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, new TrustManager[] {new IgnoreVerifyTrustManager()}, SecureRandom.getInstanceStrong());
        return sc;
    }

    static class IgnoreVerifyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
            String paramString) {
            if (paramArrayOfX509Certificate == null || paramArrayOfX509Certificate.length == 0) {
                throw new IllegalArgumentException("Empty certificate chain");
            }
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
            String paramString) {
            if (paramArrayOfX509Certificate == null || paramArrayOfX509Certificate.length == 0) {
                throw new IllegalArgumentException("Empty certificate chain");
            }
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }
    }
}
