/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 国际化相关
 *
 */

@Configuration
public class RuntimeI18nConfig {
    @Bean("runtimeMessageResource")
    public MessageSource runtimeMessageResource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/runtime-msg");
        messageSource.setDefaultEncoding("UTF-8");
        // 设置缓存时间 1h
        messageSource.setCacheSeconds(3600);
        return messageSource;
    }
}
