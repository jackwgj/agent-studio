/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Locale;

@MockitoSettings(strictness = Strictness.LENIENT)
class LanguageUtilsTest {
    private AutoCloseable mockitoCloseable;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("en-us");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
        mockedHeaderStatic.close();
    }

    @Test
    void test_getLanguage_should_equal_result() throws Exception {

        // When
        String result = LanguageUtils.getLanguage();

        // Then
        assertEquals("en-us", result);
    }

    @Test
    void test_isChinese_should_return_false() throws Exception {

        // When
        boolean result = LanguageUtils.isChinese();

        // Then
        assertFalse(result);
    }

    @Test
    void test_getLanguageLocale_should_equal_result() throws Exception {
        // When
        Locale result = LanguageUtils.getLanguageLocale();
        assertEquals("en", result.getLanguage());

    }


}
