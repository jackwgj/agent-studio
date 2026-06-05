/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

import com.alibaba.fastjson.JSON;

import com.openjiuwen.studio.agent.common.utils.LogUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class LogUtilsTest {
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_encodeForLog_should_equal_result() {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticJSON.when(() -> JSON.toJSONString(any(Object.class))).thenReturn("not_empty");

            // When
            String result = LogUtils.encodeForLog("not_empty");

            // Then
            assertEquals("not_empty", result);
        }
    }

    @Test
    void test_encodeForLog_should_equal_result2() {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticJSON.when(() -> JSON.toJSONString(anyInt())).thenReturn("1");

            // When
            String result = LogUtils.encodeForLog(1);

            // Then
            assertEquals("1", result);
        }
    }

    @Test
    void test_encodeForLog_should_equal_result3() throws Exception {
        try (MockedStatic<JSON> mockedStaticJSON = mockStatic(JSON.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticJSON.when(() -> JSON.toJSONString(any(Object.class))).thenReturn(null);

            // When
            String result = LogUtils.encodeForLog(null);

            // Then
            assertEquals("null", result);
        }
    }
}
