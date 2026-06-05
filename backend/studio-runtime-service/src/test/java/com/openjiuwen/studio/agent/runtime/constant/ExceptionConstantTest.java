/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.constant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class ExceptionConstantTest {
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
    void test_getErrorCode_should_equal_result() throws Exception {

        // When
        String result = ExceptionConstant.getErrorCode("not_empty");

        // Then
        assertEquals("AIAE.0100not_empty", result);
    }

    @Test
    void test_getErrorCode_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            String result = ExceptionConstant.getErrorCode(null);
        });
    }

    @Test
    void test_getErrorCode_should_equal_result1() throws Exception {

        // When
        String result = ExceptionConstant.getErrorCode(null);

        // Then
        assertEquals("AIAE.0100null", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("1");

        // Then
        assertEquals("AIAE.01001101", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result1() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("2");

        // Then
        assertEquals("AIAE.01001102", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result2() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("3");

        // Then
        assertEquals("AIAE.01001103", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result3() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("4");

        // Then
        assertEquals("AIAE.01001104", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result4() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("5");

        // Then
        assertEquals("AIAE.01001105", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result5() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("10");

        // Then
        assertEquals("AIAE.01001110", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result6() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("14");

        // Then
        assertEquals("AIAE.01001114", result);
    }

    @Test
    void test_getScasErrorCode_should_equal_result14() throws Exception {

        // When
        String result = ExceptionConstant.getScasErrorCode("not_empty");

        // Then
        assertEquals("AIAE.0100AIAE.01001003", result);
    }

}
