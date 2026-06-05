/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.constant.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * 判断请求语言
 *
 */
public class LanguageUtils {

    /**
     * 获取请求语言的语言代码
     */
    public static String getLanguage() {
        String language = RequestHeaderHolderUtils.getRequestLanguage().trim().toLowerCase(Locale.ROOT);
        return StringUtils.isNotBlank(language) ? language : Constants.ZH_CN;
    }

    /**
     * 判断请求语言是否为中文
     */
    public static boolean isChinese() {
        String language = RequestHeaderHolderUtils.getRequestLanguage().trim().toLowerCase(Locale.ROOT);
        return StringUtils.isEmpty(language) || Constants.ZH_CN.equalsIgnoreCase(language);
    }

    /**
     * 获取语言的Locale对象，如果语言标签不符合规范，返回默认语言的Locale
     */
    public static Locale getLanguageLocale() {
        String language = getLanguage();
        try {
            return Locale.forLanguageTag(language);
        } catch (IllegalArgumentException e) {
            // 如果语言标签不符合规范，返回默认语言的Locale
            return Locale.forLanguageTag(Constants.ZH_CN);
        }
    }

}
