/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class RuntimeAsyncConfig {
    @Value("${model.request.thread.corePoolSize:50}")
    private int mdPoolSize;

    @Value("${model.request.thread.maxPoolSize:100}")
    private int mdMaxPoolSize;

    @Value("${model.request.thread.queueCapacity:100}")
    private int mdQueueCapacity;

    @Value("${model.request.thread.threadNamePrefix:model-}")
    private String mdThreadName;

    @Value("${model.request.thread.keepAliveSeconds:800}")
    private int mdKeepAliveSeconds;

    @Bean("modelRequestThreadPool")
    public ThreadPoolTaskExecutor modelRequestTaskExecutor() {
        return createThreadPool(mdPoolSize, mdMaxPoolSize, mdQueueCapacity, mdKeepAliveSeconds, mdThreadName);
    }

    @Bean("securityCheckThreadPool")
    public ThreadPoolTaskExecutor securityCheckTaskExecutor() {
        return createThreadPool(mdPoolSize, mdMaxPoolSize, mdQueueCapacity, mdKeepAliveSeconds, "sec-");
    }

    private ThreadPoolTaskExecutor createThreadPool(int coreSize, int maxSize, int queueCapacity, int aliveSeconds,
        String threadName) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数
        executor.setCorePoolSize(coreSize);
        // 设置最大线程数
        executor.setMaxPoolSize(maxSize);
        // 设置队列容量
        executor.setQueueCapacity(queueCapacity);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(aliveSeconds);
        // 设置默认线程名称
        executor.setThreadNamePrefix(threadName);
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
