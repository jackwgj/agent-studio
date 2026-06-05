/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.ObsService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;

public class RuntimeModelApiLogServiceTest {
    private RuntimeModelApiLogService service;

    private static ThreadPoolTaskExecutor executor;

    private void createTheadPool() {
        executor = new ThreadPoolTaskExecutor();

        // 设置核心线程数
        executor.setCorePoolSize(2);
        // 设置最大线程数
        executor.setMaxPoolSize(3);
        // 设置队列容量
        executor.setQueueCapacity(10);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(100);
        // 设置默认线程名称
        executor.setThreadNamePrefix("test");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
    }

    @BeforeEach
    public void beforeEach() {
        service = new RuntimeModelApiLogService();

        createTheadPool();

        ReflectionTestUtils.setField(service, "modelContentReport", true);
        ReflectionTestUtils.setField(service, "allReportOpen", true);
        ReflectionTestUtils.setField(service, "prefix", "");
        ReflectionTestUtils.setField(service, "keepNum", 1);
        ReflectionTestUtils.setField(service, "inferLogsPath", ".");
        ReflectionTestUtils.setField(service, "executor", executor);
    }

    @AfterEach
    public void afterAllMethod() {
        executor.shutdown();
    }

    @Test
    public void postConstructTest() {
        service.postConstruct();

        ReflectionTestUtils.setField(service, "modelContentReport", false);
        service.postConstruct();
    }

    @Test
    public void saveModelApiLogTest() {
        ModelApiLog apiLog = new ModelApiLog().setModelId("model").setFreeModel(true);
        service.saveModelApiLog(apiLog);
    }

    @Test
    public void syncLogFileToObsTest() {
        service.postConstruct();
        service.syncLogFileToObs();

        ReflectionTestUtils.setField(service, "counter", 11);
        service.syncLogFileToObs();
    }

    @Test
    public void clearObsFilesTest() {
        service.postConstruct();
        service.clearObsFiles();

        ObsService obsService = Mockito.mock(ObsService.class);
        ReflectionTestUtils.setField(service, "obsService", obsService);

        Mockito.when(obsService.listObjectKeys(Mockito.anyString())).thenReturn(Collections.emptyList());
        service.clearObsFiles();

        Mockito.when(obsService.listObjectKeys(Mockito.anyString())).thenReturn(Arrays.asList("f2_1", "f1_2", "f5_2"));
        Mockito.doNothing().when(obsService).deleteObject(Mockito.anyString());
        service.clearObsFiles();

        service.threadSleep();
        service.closeResource(null);
        service.closeResource(new ByteArrayInputStream(new byte[] {}));
    }

    @Test
    public void saveSecurityCenterLogTest() {
        service.postConstruct();
        ModelApiLog apiLog = new ModelApiLog().setModelId("model").setFreeModel(true);
        service.saveSecurityCenterLog(apiLog);
    }
}
