/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.i18n;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.Locale;
import java.util.Map;

/**
 * i18n解析器
 */
@Component("localeResolver")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class AgentBaseI18nWebfluxLocaleResolver implements LocaleContextResolver {

    private Map<String, Locale> supportedLangs;

    public AgentBaseI18nWebfluxLocaleResolver() {
        this.supportedLangs = AgentBaseI18nWebmvcLocaleResolver.createDefaultLocale();
    }

    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        Locale resolvedLocal = this.supportedLangs.getOrDefault(
            exchange.getRequest().getHeaders().getFirst("X-Language"), Locale.getDefault());
        return new SimpleLocaleContext(resolvedLocal);
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {

    }
}
