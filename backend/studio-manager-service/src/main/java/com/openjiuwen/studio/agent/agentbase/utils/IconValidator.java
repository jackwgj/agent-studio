/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.utils;


import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 图标校验器：确保上传的 Base64 图片是合法、安全、符合规格的。
 */
@Slf4j
public final class IconValidator {

    private static final String KNOWLEDGE_ICON = "icon";

    /**
     * 校验图标 Base64 数据是否合法、安全、符合要求。
     *
     * @param base64 图标 Base64 字符串（可含 data:image/...;base64, 前缀）
     * @param maxImageSize 图标大小（KB）
     * @throws AgentBaseException 当校验失败时抛出
     */
    public static void validate(String base64, long maxImageSize, String allowedFormatsString) {
        // 空值校验
        if (base64 == null || base64.trim().isEmpty()) {
            log.error("Input Base64 is null or empty");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "icon");
        }
        // 提取纯 Base64 数据
        String cleanBase64 = extractBase64(base64);
        // Base64 解码
        byte[] imageBytes = decodeBase64(cleanBase64);
        String fileType = getFileTypeFromDataUri(base64);
        if (fileType == null) {
            log.error("Failed to extract file type from data URI");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "icon");
        }
        Set<String> formats = new HashSet<>();
        if (allowedFormatsString != null && !allowedFormatsString.trim().isEmpty()) {
            Arrays.stream(allowedFormatsString.trim().split(","))
                .map(s -> s.trim().toLowerCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .forEach(formats::add);
        } else {
            log.error("allowedFormats is null or empty");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "icon");
        }
        if (!formats.contains(fileType.toLowerCase(Locale.ROOT))) {
            log.error("invalid icon type");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "icon");
        }
    }

    /**
     * 获取图片类型
     *
     * @param input
     * @return
     */
    private static String getFileTypeFromDataUri(String input) {
        if (input == null) {
            return null;
        }
        if (!input.startsWith("data:")) {
            return null;
        }
        int slashIndex = input.indexOf('/');
        int semicolonIndex = input.indexOf(';');
        if (slashIndex < 0 || semicolonIndex < 0 || slashIndex >= semicolonIndex) {
            return null;
        }
        return input.substring(slashIndex + 1, semicolonIndex);
    }

    /**
     * 从可能包含 data: 前缀的字符串中提取 Base64 数据。
     */
    private static String extractBase64(String input) {
        input = input.trim();
        if (input.startsWith("data:")) {
            int commaIndex = input.indexOf(',');
            if (commaIndex == -1) {
                log.error("Invalid Base64 format: missing comma after 'data:'");
                throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "icon");
            }
            return input.substring(commaIndex + 1);
        }
        return input;
    }

    /**
     * Base64 解码，失败则抛异常。
     */
    private static byte[] decodeBase64(String base64) {
        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 data");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "icon");
        }
    }
}
