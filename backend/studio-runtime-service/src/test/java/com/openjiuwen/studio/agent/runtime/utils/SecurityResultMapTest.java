/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

/**
 * 功能描述
 *
 */

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class SecurityResultMapTest {
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
    public void test_getSecurityMap_with_valid_key() throws Exception {
        // Given
        String oriResult = "block";

        // When
        String result = SecurityResultMap.getSecurityMap(oriResult);

        // Then
        assertEquals("REJECT", result, "Expected 'ACCEPT' for 'HC_ACCEPT'");
    }

    @Test
    void test_getSecurityMap_should_equal_result() throws Exception {

        // When
        String result = SecurityResultMap.getSecurityMap("not_empty");

        // Then
        assertEquals(null, result);
    }

}
