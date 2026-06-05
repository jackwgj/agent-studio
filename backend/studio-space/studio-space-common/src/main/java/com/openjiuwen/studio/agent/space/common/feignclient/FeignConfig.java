/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.feignclient;

import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;

import feign.Client;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

@Configuration
@Slf4j
public class FeignConfig {
    /**
     * Feign Client配置，使用server的ssl配置, 并且改写原始execute方法打印之前前后日志
     */
    @Bean
    public Client feignClient(){
        return new Client.Default(sslSocketFactory().getSocketFactory(), (hostname, session) -> true);
    }

    private SSLContext sslSocketFactory() {
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().setSecureRandom(SecureRandom.getInstanceStrong()).loadTrustMaterial(null, (chain, authType) -> true).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            log.error("create Client is error,error message is : {}", e.getMessage());
        }
        if (sslContext == null) {
            log.error("create Client is error");
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
        return sslContext;
    }

    @Bean
    public Retryer retryer() {
        return new FeignRetry(3); // 最多重试3次
    }


    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }
}

