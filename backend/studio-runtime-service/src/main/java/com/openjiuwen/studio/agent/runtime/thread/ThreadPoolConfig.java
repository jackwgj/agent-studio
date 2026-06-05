/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.thread;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池相关配置
 *
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    @Value("${agent-ops.core-pool-size:8}")
    private int corePoolSize;

    @Value("${agent-ops.max-pool-size:32}")
    private int maxPoolSize;

    @Value("${agent-ops.pool-keep-alive:60}")
    private int keepAliveSeconds;

    @Bean("traceTaskExecutor")
    public ThreadPoolTaskExecutor traceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 设置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        // 设置队列容量
        executor.setQueueCapacity(500);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 设置默认线程名称
        executor.setThreadNamePrefix("OpsTaskExecutor-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new TraceRejectHandler());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }

    private static class TraceRejectHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.info("Trace task executor did not accept task.");
        }
    }
}
