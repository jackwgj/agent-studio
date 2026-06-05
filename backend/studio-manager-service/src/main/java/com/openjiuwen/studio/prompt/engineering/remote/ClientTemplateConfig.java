/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.remote;

import lombok.extern.slf4j.Slf4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;

/**
 * 功能描述 各模块定制化clientTemplate
 *
 */
@Configuration
@Slf4j
public class ClientTemplateConfig {
    /**
     * 远端服务client
     *
     * @param factory
     * @return
     */
    @Bean(name = "remoteClientTemplate")
    public ClientTemplate getManagerClient(ClientHttpRequestFactory factory) {
        return new ClientTemplate(factory, new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(@NotNull HttpStatusCode statusCode) {
                return statusCode.is5xxServerError() || statusCode.value() == 429;
            }
        });
    }
}
