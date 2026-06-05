/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@MockitoSettings(strictness = Strictness.LENIENT)
class ThreadPoolConfigTest {
    @InjectMocks
    private ThreadPoolConfig threadPoolConfig;

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
    void test_traceTaskExecutor_should_return_not_null() throws Exception {

        // When
        ThreadPoolTaskExecutor result = threadPoolConfig.traceTaskExecutor();

        // Then
        assertEquals(0, result.getCorePoolSize());
    }
}
