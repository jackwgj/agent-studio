/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

public class RuntimeRequestAdapterFactoryTest {
    private OpenApiRequestAdapter adapter;

    private RuntimeRequestAdapterFactory factory;

    @BeforeEach
    public void beforeEach() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(1);
        // 设置最大线程数
        executor.setMaxPoolSize(1);
        // 设置队列容量
        executor.setQueueCapacity(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(100);
        // 设置默认线程名称
        executor.setThreadNamePrefix("test");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        adapter = new OpenApiRequestAdapter();
        ReflectionTestUtils.setField(adapter, "executor", executor);
        ReflectionTestUtils.setField(adapter, "apiLogService", new RuntimeModelApiLogService());
        BaiChuanRequestAdapter adapter1 = new BaiChuanRequestAdapter();

        // 创建包含两个适配器的列表
        List<RequestAdapter> adapters = Arrays.asList(adapter, adapter1);

        factory = new RuntimeRequestAdapterFactory(adapters, adapter);
    }

    @Test
    public void getAdapterTest() {
        RequestAdapter ap = factory.getAdapter("openai");
        assertInstanceOf(OpenApiRequestAdapter.class, ap);

        RequestAdapter bc = factory.getAdapter("baichuan");
        assertInstanceOf(BaiChuanRequestAdapter.class, bc);

        ap = factory.getAdapter("no_exist");
        assertEquals(adapter, ap);
    }
}
