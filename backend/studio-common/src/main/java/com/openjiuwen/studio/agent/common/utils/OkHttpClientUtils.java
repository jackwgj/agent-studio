/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 功能描述
 *
 */
@Component
public class OkHttpClientUtils {
    private static final Logger log = LoggerFactory.getLogger(OkHttpClientUtils.class);

    @Value("${okhttp.max-idle-connections}")
    private int maxIdleConnections;

    @Value("${okhttp.keep-alive-duration}")
    private long keepAliveDuration;

    @Value("${okhttp.connect-timeout}")
    private int connectTimeOut;

    @Value("${okhttp.wait-timeout}")
    private int waitTimeOut;

    @Value("${okhttp.read-timeout}")
    private int readTimeOut;

    @Value("${okhttp.max-requests-perhost}")
    private int maxRequestsPerHost;

    @Value("${okhttp.max-requests}")
    private int maxRequests;

    @Value("${okhttp.ignore-cert}")
    private boolean isIgnoreCert;

    @Getter
    private OkHttpClient httpClient = null;

    @PostConstruct
    private void init() throws NoSuchAlgorithmException, KeyManagementException {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(maxRequestsPerHost);
        dispatcher.setMaxRequests(maxRequests);
        if (isIgnoreCert) {
            httpClient = new OkHttpClient.Builder()
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                .connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .writeTimeout(waitTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES))
                .dispatcher(dispatcher)
                .build();
        } else {
            httpClient = new OkHttpClient.Builder().connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .writeTimeout(waitTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MINUTES))
                .dispatcher(dispatcher)
                .build();
        }
    }

    public class SSLSocketClient {
        // 获取这个SSLSocketFactory
        public static SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, getTrustManager(), SecureRandom.getInstanceStrong());
            return sslContext.getSocketFactory();
        }

        // 获取TrustManager
        private static TrustManager[] getTrustManager() {
            return new TrustManager[] {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    if (chain == null || chain.length == 0) {
                        throw new IllegalArgumentException("Empty certificate chain");
                    }
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    if (chain == null || chain.length == 0) {
                        throw new IllegalArgumentException("Empty certificate chain");
                    }
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }
            }};
        }

        // 获取HostnameVerifier
        public static HostnameVerifier getHostnameVerifier() {
            return (hostname, session) -> !"".equals(hostname);
        }

        public static X509TrustManager getX509TrustManager() {
            X509TrustManager trustManager = null;
            try {
                TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException(
                        "Unexpected default trust managers:" + Arrays.toString(trustManagers));
                }
                trustManager = (X509TrustManager) trustManagers[0];
            } catch (Exception e) {
                log.error("trust manager init failed.", e);
            }

            return trustManager;
        }
    }
}
