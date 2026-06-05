/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.manager.dto.McpFailReasonDetailDto;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * MCP 错误文案工厂 (Spring I18N 版 - 适配标准资源文件)
 * Key 格式: AgentBuilder.0240xxxx
 */
@Slf4j
@Component
public class McpErrorFactory {

    private static MessageSource messageSource;

    @Resource(name = "messageSource")
    private MessageSource injectedMessageSource;

    @PostConstruct
    public void init() {
        McpErrorFactory.messageSource = this.injectedMessageSource;
    }

    /**
     * 核心方法：根据简码 (1161) 读取中英两套文案
     */
    public static McpFailReasonDetailDto buildFullDetail(String shortCode, String debugInfo) {
        McpFailReasonDetailDto result = new McpFailReasonDetailDto();
        result.setDebugInfo(debugInfo);

        try {
            // 1. 构造标准 Key (例如 AgentBuilder.02401161)
            String key = "openjiuwen.0240" + shortCode;

            // 2. 验证 Key 是否存在 (用英文版验证)
            String enTitle = getMsg(key, Locale.US);
            if (enTitle == null) {
                return buildFallback(shortCode, debugInfo);
            }

            // 3. 填入 Code
            result.setCode(key);

            // 4. 读取中文文案
            result.setMessageCn(getMsg(key, Locale.CHINA));               // Title
            result.setReasonCn(getMsg(key + ".reason", Locale.CHINA));     // Reason
            result.setSuggestionCn(getMsg(key + ".suggestion", Locale.CHINA)); // Suggestion

            // 5. 读取英文文案
            result.setMessageEn(enTitle);                                  // Title (复用前面读到的)
            result.setReasonEn(getMsg(key + ".reason", Locale.US));        // Reason
            result.setSuggestionEn(getMsg(key + ".suggestion", Locale.US)); // Suggestion

        } catch (Exception e) {
            log.error("Failed to load i18n messages for code: {}", shortCode, e);
            return buildFallback(shortCode, debugInfo);
        }

        return result;
    }

    private static String getMsg(String key, Locale locale) {
        if (messageSource == null) return null;
        try {
            return messageSource.getMessage(key, null, null, locale);
        } catch (Exception e) {
            return null;
        }
    }

    private static McpFailReasonDetailDto buildFallback(String shortCode, String debugInfo) {
        McpFailReasonDetailDto dto = new McpFailReasonDetailDto();
        dto.setCode("openjiuwen.Unknown");
        dto.setDebugInfo(debugInfo);

        dto.setMessageCn("未知错误 (" + shortCode + ")");
        dto.setReasonCn("发生了一个未定义的部署错误。");
        dto.setSuggestionCn("请查看 Debug Info 获取详细堆栈。");

        dto.setMessageEn("Unknown Error (" + shortCode + ")");
        dto.setReasonEn("An undefined deployment error occurred.");
        dto.setSuggestionEn("Please check Debug Info for details.");

        return dto;
    }
}
