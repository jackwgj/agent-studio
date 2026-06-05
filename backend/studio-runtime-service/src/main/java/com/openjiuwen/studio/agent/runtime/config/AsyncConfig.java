/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 功能描述
 *
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {
    @Value("${spring.pools.asyncExecutor.corePoolSize}")
    private int corePoolSize;

    @Value("${spring.pools.asyncExecutor.maxPoolSize}")
    private int maxPoolSize;

    @Value("${spring.pools.asyncExecutor.queueCapacity}")
    private int queueCapacity;

    @Value("${spring.pools.asyncExecutor.threadNamePrefix}")
    private String threadNamePrefix;

    @Value("${spring.pools.asyncExecutor.keepAliveSeconds}")
    private int keepAliveSeconds;

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setThreadNamePrefix(threadNamePrefix);
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        taskExecutor.initialize();
        return taskExecutor;
    }
}
