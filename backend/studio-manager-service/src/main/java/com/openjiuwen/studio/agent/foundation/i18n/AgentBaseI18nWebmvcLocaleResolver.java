/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * i18n解析器
 */
@Component("localeResolver")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AgentBaseI18nWebmvcLocaleResolver implements LocaleResolver {

    private Map<String, Locale> supportedLang;

    public AgentBaseI18nWebmvcLocaleResolver() {
        this.supportedLang = createDefaultLocale();
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 从header中解析语言
        return this.supportedLang.getOrDefault(Optional.ofNullable(request.getHeader("X-Language"))
            .orElse(Locale.getDefault().toString().toLowerCase(Locale.ROOT))
            .toLowerCase(Locale.ROOT), Locale.getDefault());
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

    }

    public static Map<String, Locale> createDefaultLocale() {
        // 只支持Locale.US和Locale.SIMPLIFIED_CHINESE
        Map<String, Locale> langMap = new HashMap<>();
        // en
        langMap.put(Locale.ENGLISH.toString().toLowerCase(Locale.ROOT), Locale.US);
        // en_us
        langMap.put(Locale.US.toString().toLowerCase(Locale.ROOT), Locale.US);
        // en-us
        langMap.put(Locale.US.toLanguageTag().toLowerCase(Locale.ROOT), Locale.US);
        // zh-cn
        langMap.put(Locale.SIMPLIFIED_CHINESE.toString().toLowerCase(Locale.ROOT), Locale.SIMPLIFIED_CHINESE);
        // zh-cn
        langMap.put(Locale.SIMPLIFIED_CHINESE.toLanguageTag().toLowerCase(Locale.ROOT), Locale.SIMPLIFIED_CHINESE);
        return Collections.unmodifiableMap(langMap);
    }
}
