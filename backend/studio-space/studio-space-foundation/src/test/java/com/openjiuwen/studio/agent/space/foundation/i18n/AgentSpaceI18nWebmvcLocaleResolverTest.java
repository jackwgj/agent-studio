package com.openjiuwen.studio.agent.space.foundation.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class AgentSpaceI18nWebmvcLocaleResolverTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Map<String, Locale> supportedLang;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    public void testResolveLocale() {
        AgentSpaceI18nWebmvcLocaleResolver agentSpaceI18nWebmvcLocaleResolver1
            = new AgentSpaceI18nWebmvcLocaleResolver();
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Language", "zh-cn");
        Locale locale = agentSpaceI18nWebmvcLocaleResolver1.resolveLocale(httpRequest);
        Assertions.assertEquals("zh", locale.getLanguage());
    }

    @Test
    public void testResolveLocaleEn() {
        AgentSpaceI18nWebmvcLocaleResolver agentSpaceI18nWebmvcLocaleResolver1
            = new AgentSpaceI18nWebmvcLocaleResolver();
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Language", "en_US");
        Locale locale = agentSpaceI18nWebmvcLocaleResolver1.resolveLocale(httpRequest);
        Assertions.assertEquals("en", locale.getLanguage());
    }
}