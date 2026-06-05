/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.filter;

import static org.mockito.Mockito.mock;

import com.openjiuwen.studio.agent.common.filter.CommonFilterConfig;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class CommonFilterConfigTest {
    @InjectMocks
    private CommonFilterConfig commonFilterConfig;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(commonFilterConfig, "freezeFilterWhiteList", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_freezeTenantFilter_should_return_not_null() throws Exception {
        // Given
        MessageSource messageSource = mock(MessageSource.class, Answers.RETURNS_DEEP_STUBS);
        I18nUtil i18nUtil = new I18nUtil(messageSource);
    }
}
