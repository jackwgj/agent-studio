/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.openjiuwen.studio.agent.common.utils.KV;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 功能描述
 *
 */
public class KVTest {
    @InjectMocks
    private KV kV;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(kV, "key", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    public void test_of_method() {
        // Given
        String expectedKey = "testKey";
        Object expectedValue = new Object();

        // When
        KV result = KV.of(expectedKey, expectedValue);

        // Then
        assertNotNull(result, "The result should not be null.");
        assertEquals(expectedKey, result.getKey(), "The key should match the expected value.");
        assertEquals(expectedValue, result.getValue(), "The value should match the expected value.");
    }

    @Test
    void test_of_should_return_not_null() throws Exception {
        // Given
        Object value = new Object();

        // When
        KV result = KV.of("not_empty", value);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_of_should_return_not_null1() throws Exception {

        // When
        KV result = KV.of(null, null);

        // Then
        assertNotNull(result);
    }
}
