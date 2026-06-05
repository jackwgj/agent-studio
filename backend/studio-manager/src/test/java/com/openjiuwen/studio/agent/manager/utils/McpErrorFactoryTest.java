/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.dto.McpFailReasonDetailDto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class McpErrorFactoryTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private McpErrorFactory mcpErrorFactory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(McpErrorFactory.class, "messageSource", messageSource);
    }

    /**
     * 测试场景：未知的错误码
     * 覆盖路径：buildFullDetail -> if (enTitle == null) -> buildFallback
     */
    @Test
    void testBuildFullDetail_UnknownCode() {
        // 模拟 MessageSource 查不到该 Code
        when(messageSource.getMessage(anyString(), any(), any(), any())).thenReturn(null);

        String unknownCode = "9999";
        String debugInfo = "Some stack trace";

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail(unknownCode, debugInfo);

        // 验证走了 Fallback 逻辑
        Assertions.assertEquals("openjiuwen.Unknown", result.getCode());
        Assertions.assertEquals(debugInfo, result.getDebugInfo());
        Assertions.assertTrue(result.getMessageCn().contains(unknownCode));
        Assertions.assertEquals("An undefined deployment error occurred.", result.getReasonEn());
    }

    /**
     * 测试场景：MessageSource 抛出异常
     * 覆盖路径：buildFullDetail -> catch (Exception e) -> buildFallback
     * 覆盖路径：getMsg -> catch (Exception e)
     */
    @Test
    void testBuildFullDetail_Exception() {
        // 模拟 MessageSource 抛出运行时异常
        when(messageSource.getMessage(anyString(), any(), any(), any()))
                .thenThrow(new RuntimeException("I18n failed"));

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("1161", "Stack");

        // 验证异常被捕获并走了 Fallback
        Assertions.assertEquals("openjiuwen.Unknown", result.getCode());
        Assertions.assertNotNull(result.getMessageCn());
    }

    /**
     * 测试场景：MessageSource 为 null (静态变量未初始化)
     * 覆盖路径：getMsg -> if (messageSource == null)
     */
    @Test
    void testGetMsg_NullSource() {
        // 将静态变量置空
        ReflectionTestUtils.setField(McpErrorFactory.class, "messageSource", null);

        McpFailReasonDetailDto result = McpErrorFactory.buildFullDetail("1161", "Stack");

        // 应该走 Fallback，不会空指针
        Assertions.assertEquals("openjiuwen.Unknown", result.getCode());
    }
}
