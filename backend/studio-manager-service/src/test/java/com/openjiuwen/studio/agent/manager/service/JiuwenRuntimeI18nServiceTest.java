/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JiuwenRuntimeI18nServiceTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private I18nUtil i18nUtil;

    @InjectMocks
    private JiuwenRuntimeI18nService jiuwenRuntimeI18nService;

    @Test
    void testCreateRuntimeErrorEvent_TokenUseUpError() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", "Error: openjiuwen.02501051 token use up");

        lenient().when(i18nUtil.getMessage("openjiuwen.02501051")).thenReturn("Token used up");
        lenient().when(i18nUtil.getMessage("openjiuwen.02501051.suggestion")).thenReturn("Buy more tokens");
        lenient().when(i18nUtil.getMessage("openjiuwen.02501051.reason")).thenReturn("Quota exceeded");

        Map<String, Object> result = jiuwenRuntimeI18nService.createRuntimeErrorEvent(eventData);

        assertEquals("openjiuwen.02501051", result.get("error_code"));
        assertEquals("Token used up", result.get("error_msg"));
        assertEquals("Buy more tokens", result.get("error_suggestion"));
        assertEquals("Quota exceeded", result.get("error_reason"));
    }

    @Test
    void testCreateRuntimeErrorEvent_SystemError() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", "some random error");
        eventData.put("code", "500");

        try (MockedStatic<LanguageUtils> mockedStatic = mockStatic(LanguageUtils.class)) {
            mockedStatic.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.ENGLISH);
            lenient().when(messageSource.getMessage(eq("500"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Internal Server Error");
            lenient().when(messageSource.getMessage(eq("500.suggestion"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Try again");
            lenient().when(messageSource.getMessage(eq("500.reason"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Server issue");

            Map<String, Object> result = jiuwenRuntimeI18nService.createRuntimeErrorEvent(eventData);

            assertEquals("openjiuwen.500", result.get("error_code"));
            assertEquals("Internal Server Error", result.get("error_msg"));
        }
    }

    @Test
    void testCreateRuntimeErrorEvent_NullMessage() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("code", "404");

        try (MockedStatic<LanguageUtils> mockedStatic = mockStatic(LanguageUtils.class)) {
            mockedStatic.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.ENGLISH);
            lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenReturn("Not Found");

            Map<String, Object> result = jiuwenRuntimeI18nService.createRuntimeErrorEvent(eventData);

            assertEquals("openjiuwen.404", result.get("error_code"));
        }
    }

    @Test
    void testCreateRuntimeErrorEvent_NullCode() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", "error without code");

        try (MockedStatic<LanguageUtils> mockedStatic = mockStatic(LanguageUtils.class)) {
            mockedStatic.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.ENGLISH);
            lenient().when(messageSource.getMessage(eq("999999"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("System Error");
            lenient().when(messageSource.getMessage(eq("999999.suggestion"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Contact support");
            lenient().when(messageSource.getMessage(eq("999999.reason"), any(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Unknown");

            Map<String, Object> result = jiuwenRuntimeI18nService.createRuntimeErrorEvent(eventData);

            assertEquals("openjiuwen.999999", result.get("error_code"));
        }
    }

    @Test
    void testCreateRuntimeErrorEvent_MessageSourceThrowsException() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", "error");
        eventData.put("code", "123");

        try (MockedStatic<LanguageUtils> mockedStatic = mockStatic(LanguageUtils.class)) {
            mockedStatic.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.ENGLISH);
            lenient().when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class)))
                .thenThrow(new RuntimeException("no message"));

            Map<String, Object> result = jiuwenRuntimeI18nService.createRuntimeErrorEvent(eventData);

            assertEquals("openjiuwen.123", result.get("error_code"));
            assertEquals("", result.get("error_msg"));
            assertEquals("", result.get("error_suggestion"));
            assertEquals("", result.get("error_reason"));
        }
    }
}
