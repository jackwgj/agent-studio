/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class I18nConfigTest {

    @Test
    void testMessageSource() {
        I18nConfig config = new I18nConfig();
        MessageSource messageSource = config.messageSource();
        assertNotNull(messageSource);
    }
}
