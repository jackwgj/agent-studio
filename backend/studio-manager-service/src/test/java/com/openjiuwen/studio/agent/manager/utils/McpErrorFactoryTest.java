/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.dto.McpFailReasonDetailDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

@MockitoSettings(strictness = Strictness.LENIENT)
class McpErrorFactoryTest {

    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        ReflectionTestUtils.setField(McpErrorFactory.class, "messageSource", messageSource);
    }

    @Test
    void testBuildFullDetail_WithValidCode() {
        String key = "openjiuwen.02401161";
        when(messageSource.getMessage(eq(key), any(), eq(null), eq(Locale.US))).thenReturn("Deploy Failed");
        when(messageSource.getMessage(eq(key), any(), eq(null), eq(Locale.CHINA))).thenReturn("部署失败");
        when(messageSource.getMessage(eq(key + ".reason"), any(), eq(null), eq(Locale.CHINA))).thenReturn("原因");
        when(messageSource.getMessage(eq(key + ".suggestion"), any(), eq(null), eq(Locale.CHINA))).thenReturn("建议");
        when(messageSource.getMessage(eq(key + ".reason"), any(), eq(null), eq(Locale.US))).thenReturn("Reason");
        when(messageSource.getMessage(eq(key + ".suggestion"), any(), eq(null), eq(Locale.US))).thenReturn("Suggestion");

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("1161", "debug info");

        assertNotNull(result);
        assertEquals(key, result.getCode());
        assertEquals("debug info", result.getDebugInfo());
        assertEquals("Deploy Failed", result.getMessageEn());
        assertEquals("部署失败", result.getMessageCn());
        assertEquals("Reason", result.getReasonEn());
        assertEquals("原因", result.getReasonCn());
        assertEquals("Suggestion", result.getSuggestionEn());
        assertEquals("建议", result.getSuggestionCn());
    }

    @Test
    void testBuildFullDetail_WithUnknownCode() {
        when(messageSource.getMessage(any(), any(), eq(null), eq(Locale.US))).thenReturn(null);

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("9999", "some debug");

        assertNotNull(result);
        assertEquals("openjiuwen.Unknown", result.getCode());
        assertEquals("some debug", result.getDebugInfo());
        assertEquals("Unknown Error (9999)", result.getMessageEn());
        assertEquals("未知错误 (9999)", result.getMessageCn());
    }

    @Test
    void testBuildFullDetail_WithNullMessageSource() {
        ReflectionTestUtils.setField(McpErrorFactory.class, "messageSource", null);

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("1161", "debug");

        assertNotNull(result);
        assertEquals("openjiuwen.Unknown", result.getCode());
        assertEquals("Unknown Error (1161)", result.getMessageEn());
    }

    @Test
    void testBuildFullDetail_WithExceptionFromMessageSource() {
        when(messageSource.getMessage(any(), any(), eq(null), eq(Locale.US))).thenThrow(new RuntimeException("error"));

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("1161", "debug");

        assertNotNull(result);
        assertEquals("openjiuwen.Unknown", result.getCode());
    }

    @Test
    void testBuildFullDetail_WithNullDebugInfo() {
        when(messageSource.getMessage(any(), any(), eq(null), eq(Locale.US))).thenReturn(null);

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("1161", null);

        assertNotNull(result);
        assertNull(result.getDebugInfo());
    }

    @Test
    void testBuildFullDetail_WithEmptyShortCode() {
        String key = "openjiuwen.0240";
        when(messageSource.getMessage(eq(key), any(), eq(null), eq(Locale.US))).thenReturn("Title");
        when(messageSource.getMessage(eq(key), any(), eq(null), eq(Locale.CHINA))).thenReturn("标题");
        when(messageSource.getMessage(eq(key + ".reason"), any(), eq(null), eq(Locale.CHINA))).thenReturn("原因");
        when(messageSource.getMessage(eq(key + ".suggestion"), any(), eq(null), eq(Locale.CHINA))).thenReturn("建议");
        when(messageSource.getMessage(eq(key + ".reason"), any(), eq(null), eq(Locale.US))).thenReturn("Reason");
        when(messageSource.getMessage(eq(key + ".suggestion"), any(), eq(null), eq(Locale.US))).thenReturn("Suggestion");

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("", "debug");

        assertNotNull(result);
        assertEquals(key, result.getCode());
    }

    @Test
    void testInit() {
        McpErrorFactory factory = new McpErrorFactory();
        MessageSource injected = mock(MessageSource.class);
        ReflectionTestUtils.setField(factory, "injectedMessageSource", injected);
        factory.init();

        when(injected.getMessage(eq("test.key"), any(), eq(null), eq(Locale.US))).thenReturn("Test");
        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("test", "debug");
        assertNotNull(result);
    }
}
