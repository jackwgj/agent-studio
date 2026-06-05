/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.service.K8sResourceService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class RuntimeClusterCpuSchedulerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private K8sResourceService k8SResourceService;

    @InjectMocks
    private RuntimeClusterCpuScheduler runtimeClusterCpuScheduler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_syncLicenseTask_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(k8SResourceService.getAgentResourceCpu()).thenReturn(0.0d);

            // When
            runtimeClusterCpuScheduler.syncLicenseTask();
        });
    }
}
