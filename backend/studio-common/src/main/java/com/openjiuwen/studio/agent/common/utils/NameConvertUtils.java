/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/**
 * 功能描述
 *
 */
@Slf4j
public class NameConvertUtils {
    /**
     * 驼峰命名转蛇形命名
     *
     * @param humpStr 驼峰命名字符串
     * @return 蛇形命名字符串
     */
    public static String humpToSerpentine(String humpStr) {
        if (StringUtils.isBlank(humpStr)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < humpStr.length(); i++) {
            if (Character.isUpperCase(humpStr.charAt(i))) {
                stringBuilder.append('_').append(Character.toLowerCase(humpStr.charAt(i)));
            } else {
                stringBuilder.append(humpStr.charAt(i));
            }
        }
        return stringBuilder.toString();
    }
}
