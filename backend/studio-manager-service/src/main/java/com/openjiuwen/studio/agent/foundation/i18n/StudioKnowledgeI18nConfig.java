/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class StudioKnowledgeI18nConfig {

    @Bean("StudioKnowledgeMessageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("agent-base-i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        // 设置缓存时间 1h
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
}
