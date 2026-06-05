/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.i18n;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

@ExtendWith(MockitoExtension.class)
public class AgentBaseI18NWebmvcLocaleResolverTest {
    @Test
    public void testResolveLocale() {
        AgentBaseI18nWebmvcLocaleResolver agentBaseI18NWebmvcLocaleResolver = new AgentBaseI18nWebmvcLocaleResolver();
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Language", "zh-cn");
        Locale locale = agentBaseI18NWebmvcLocaleResolver.resolveLocale(httpRequest);
        Assertions.assertEquals("zh", locale.getLanguage());
    }

    @Test
    public void testResolveLocaleEn() {
        AgentBaseI18nWebmvcLocaleResolver agentBaseI18NWebmvcLocaleResolver = new AgentBaseI18nWebmvcLocaleResolver();
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Language", "en_US");
        Locale locale = agentBaseI18NWebmvcLocaleResolver.resolveLocale(httpRequest);
        Assertions.assertEquals("en", locale.getLanguage());
    }
}
