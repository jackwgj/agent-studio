/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符处理公共类
 *
 */
public class AgentStringUtils {
    public static final String MASK_STR = "***";

    private static final int KEEP = 1;

    public static String mask(String source) {
        if (StringUtils.isEmpty(source)) {
            return source;
        }
        int len = source.length();
        if (source.length() <= 2 * KEEP) {
            return MASK_STR;
        }
        return source.charAt(0) + MASK_STR + source.substring(len - KEEP, len);
    }

    /**
     * mask
     *
     * @param source source
     * @param keep keep
     * @return String
     */
    public static String mask(String source, int keep) {
        if (StringUtils.isBlank(source)) {
            return source;
        }

        int len = source.length();
        if (source.length() <= 2 * keep) {
            return MASK_STR;
        }

        return source.substring(0, keep) + MASK_STR + source.substring(len - keep, len);
    }

    /**
     * 数据库模糊查询时特殊字符转义
     *
     * @param before 转换前数据
     * @return String
     */
    public static String sqlEscapeChar(String before) {
        if (StringUtils.isNotBlank(before)) {
            before = before.replaceAll("\\\\", "\\\\\\\\").replace("_", "\\_").replace("%", "\\%").replace("*", "\\*");
        }
        return before;
    }
}
