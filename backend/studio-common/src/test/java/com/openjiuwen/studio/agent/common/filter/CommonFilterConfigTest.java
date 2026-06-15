/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.filter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.openjiuwen.studio.agent.common.utils.I18nUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;

@MockitoSettings(strictness = Strictness.LENIENT)
class CommonFilterConfigTest {
    @InjectMocks
    private CommonFilterConfig commonFilterConfig;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void test_requestLanguageFilter_should_return_not_null() {
        FilterRegistrationBean<?> result = commonFilterConfig.requestLanguageFilter();
        assertNotNull(result);
        assertNotNull(result.getFilter());
    }

    @Test
    void test_accessLogFilter_should_return_not_null() {
        MessageSource messageSource = mock(MessageSource.class, Answers.RETURNS_DEEP_STUBS);
        I18nUtil i18nUtil = new I18nUtil(messageSource);
        FilterRegistrationBean<?> result = commonFilterConfig.accessLogFilter(i18nUtil);
        assertNotNull(result);
        assertNotNull(result.getFilter());
    }

    @Test
    void test_requestLanguageFilter_order_should_be_min_value() {
        FilterRegistrationBean<?> result = commonFilterConfig.requestLanguageFilter();
        assertNotNull(result);
        assert result.getOrder() == Integer.MIN_VALUE;
    }
}
