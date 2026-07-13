/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

/**
 * file工具类
 *
 */
@Slf4j
public class FileCommonUtils {

    public static void validatedFile(MultipartFile file, long fileMaxSize, String validatedFileType) {
        // 文件为空
        if (file == null || file.isEmpty()) {
            log.error("file cannot be empty.");
            throw new AgentStudioException(StudioError.ILLEGAL_FILE);
        }

        // 校验文件大小
        if (file.getSize() > fileMaxSize) {
            log.error("file size exceeds the limit: {}KB", fileMaxSize);
            throw new AgentStudioException(StudioError.FILE_SIZE_EXCEED_LIMIT);
        }

        // 校验文件名
        String fileName = file.getOriginalFilename();
        if (isInvalidFileName(fileName)) {
            log.error("The file name is illegal: {}", fileName);
            throw new AgentStudioException(StudioError.ILLEGAL_FILE_NAME);
        }

        // 校验文件类型
        String fileType = fileName.substring(fileName.lastIndexOf('.'));
        if (StringUtils.isBlank(fileType) || !validatedFileType.equals(fileType.toLowerCase(Locale.ROOT))) {
            log.error("The file type is not supported: {}", fileType);
            throw new AgentStudioException(StudioError.ILLEGAL_FILE_TYPE);
        }
    }

    private static boolean isInvalidFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return true;
        }
        if (fileName.contains("..") || fileName.contains("\\") || fileName.contains("/")) {
            return true;
        }
        return !fileName.contains(".");
    }
}
