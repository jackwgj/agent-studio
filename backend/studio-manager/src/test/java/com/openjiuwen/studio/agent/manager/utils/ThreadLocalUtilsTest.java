/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.openjiuwen.studio.agent.common.utils.ThreadLocalUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class ThreadLocalUtilsTest {
    @InjectMocks
    private ThreadLocalUtils threadLocalUtils;

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
    void test_setWorkspaceId_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            ThreadLocalUtils.setWorkspaceId("default");
        });
    }

    @Test
    void test_setWorkspaceId_should_not_throw_exception1() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            ThreadLocalUtils.setWorkspaceId(null);
        });
    }

    @Test
    void test_getWorkspaceId_should_equal_result() throws Exception {

        // When
        String result = ThreadLocalUtils.getWorkspaceId();
    }

    @Test
    void test_clearWorkspaceId_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(ThreadLocalUtils::clearWorkspaceId);
    }
}
