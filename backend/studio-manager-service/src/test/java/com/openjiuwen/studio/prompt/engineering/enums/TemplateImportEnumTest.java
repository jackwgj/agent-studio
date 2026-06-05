/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.enums;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.prompt.engineering.enums.TemplateImportEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class TemplateImportEnumTest {
    private TemplateImportEnum templateImportEnum = TemplateImportEnum.TEMPLATE_NAME;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(templateImportEnum, "index", 0);
        ReflectionTestUtils.setField(templateImportEnum, "column", "not_empty");
        ReflectionTestUtils.setField(templateImportEnum, "columnEn", "not_empty");
        ReflectionTestUtils.setField(templateImportEnum, "sampleValue", "not_empty");
        ReflectionTestUtils.setField(templateImportEnum, "sampleValueEn", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getIndex_should_equal_result() throws Exception {

        // When
        int result = templateImportEnum.getIndex();

        // Then
        assertEquals(0, result);
    }

    @Test
    void test_getColumn_should_equal_result() throws Exception {

        // When
        String result = templateImportEnum.getColumn();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getColumnEn_should_equal_result() throws Exception {

        // When
        String result = templateImportEnum.getColumnEn();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getSampleValue_should_equal_result() throws Exception {

        // When
        String result = templateImportEnum.getSampleValue();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getSampleValueEn_should_equal_result() throws Exception {

        // When
        String result = templateImportEnum.getSampleValueEn();

        // Then
        assertEquals("not_empty", result);
    }
}
