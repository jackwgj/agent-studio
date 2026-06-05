/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.openjiuwen.studio.agent.common.utils.DateUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.Date;

/**
 * Clouddragon web Config
 */
@Configuration

public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Realize multiple date format deserialization (processing body parameter)
     *
     * @return Module
     */
    @Bean(value = "customizerModule")
    public Module customizerModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new DateDeserializers.DateDeserializer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Date deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
                if (parser.hasToken(JsonToken.VALUE_STRING)) {
                    String str = parser.getText().trim();
                    return DateUtils.parse(str);
                }
                return super.deserialize(parser, ctxt);
            }
        });
        return module;
    }

    /**
     * Function description: Realize the deserialization
     * of multiple date formats (processing query parameters)
     *
     * @param registry registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, Date.class, DateUtils::parse);
        registry.addConverterFactory(new EnumConverterFactory());
    }

}
