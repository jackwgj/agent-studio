/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * WebClient configuration for reactive HTTP client
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
            HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60))
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create WebClient with insecure SSL context", e);
        }
    }
}