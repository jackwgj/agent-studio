/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.i18n;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * i18n转换工具类
 */
@Component
public class I18nUtils {

    private static MessageSource messageSource;

    public I18nUtils(@Qualifier("StudioKnowledgeMessageSource") MessageSource messageSource) {
        I18nUtils.messageSource = messageSource;
    }

    /**
     * 获取单个国际化翻译值
     *
     * @param msgCode i18n翻译文件中定义的变量
     * @return
     */
    public static String getMessage(String msgCode) {
        try {
            return messageSource.getMessage(msgCode, null, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return msgCode;
        }
    }

    public static String getMessage(String msgCode, Locale locale) {
        try {
            return messageSource.getMessage(msgCode, null, locale);
        } catch (Exception e) {
            return msgCode;
        }
    }

    /**
     * 获取携带多个参数时国际化的翻译值
     *
     * @param msgCode i18n翻译文件中定义的变量
     * @param params 翻译中需要补全的变量值
     * @return
     */
    public static String getMessage(String msgCode, Object... params) {
        try {
            return messageSource.getMessage(msgCode, params, LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return msgCode;
        }
    }

    public static String getMessage(String msgCode, Locale locale, Object... params) {
        try {
            return messageSource.getMessage(msgCode, params, locale);
        } catch (Exception e) {
            return msgCode;
        }
    }

    /**
     * 获取当前上下文中的语言项
     *
     * @return
     */
    public static Locale getLocaleInContext() {
        return LocaleContextHolder.getLocale();
    }

    public static String getLanguage() {
        return isCn() ? "zh" : "en";
    }

    public static boolean isCn(String language) {
        return "zh".equalsIgnoreCase(language);
    }

    public static boolean isCn() {
        Locale locale = LocaleContextHolder.getLocale();
        return Locale.CHINA.equals(locale) || Locale.SIMPLIFIED_CHINESE.equals(locale) || Locale.CHINESE.equals(locale);
    }
}
