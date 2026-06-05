/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.filter;

import java.util.Locale;

public class LocalContextHolder {
    private static final ThreadLocal<Locale> LOCALE_HOLDER = new ThreadLocal<>();

    public static void setLocaleHolder(Locale locale) {
        LOCALE_HOLDER.set(locale);
    }

    public static Locale getLocale() {
        Locale locale = LOCALE_HOLDER.get();
        return locale != null ? locale : Locale.SIMPLIFIED_CHINESE;
    }

    public static void clearLocale() {
        LOCALE_HOLDER.remove();
    }

}
