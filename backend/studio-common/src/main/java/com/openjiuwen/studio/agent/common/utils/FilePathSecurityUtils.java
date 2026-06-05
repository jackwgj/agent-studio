/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import org.apache.commons.lang3.StringUtils;

public class FilePathSecurityUtils {

    /**
     * Check if the file path is secure (no path traversal, etc.).
     *
     * @param filePath the file path to check
     * @return true if the path is safe, false otherwise
     */
    public static boolean isSecurityFileName(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            return false;
        }
        // Check for path traversal
        if (filePath.contains("..")) {
            return false;
        }
        // Check for absolute paths (not allowed)
        if (filePath.startsWith("/") || filePath.matches("^[a-zA-Z]:\\\\.*")) {
            return false;
        }
        // Check for null bytes
        if (filePath.contains("\0")) {
            return false;
        }
        return true;
    }
}