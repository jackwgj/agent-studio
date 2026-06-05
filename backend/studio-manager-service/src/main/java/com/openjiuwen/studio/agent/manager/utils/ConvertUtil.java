/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * 结构体转换工具
 *
 */
@Slf4j
public class ConvertUtil {

    public static String convertFileToBase64String(MultipartFile file) {
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (Exception exception) {
            log.error("Failed to convert file to base64 string. Exception: {}", exception.getMessage());
            throw new AgentStudioException(StudioError.BASE_CONVERT_FAILED);
        }
    }
}
