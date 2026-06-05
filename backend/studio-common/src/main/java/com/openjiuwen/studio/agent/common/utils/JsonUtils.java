/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.concurrent.Callable;

@Slf4j
public class JsonUtils {
    public static final ObjectMapper MAPPER = getDefaultMapper();

    static {
        initialize();
    }

    private static void initialize() {
        SimpleModule module = new SimpleModule();
        MAPPER.findAndRegisterModules();
        MAPPER.registerModule(module);
        MAPPER.registerModule(new JavaTimeModule());
    }

    private static <T> T invoke(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw ExceptionUtils.asRuntimeException(e);
        }
    }

    public static String encode(Object obj) {
        return invoke(() -> MAPPER.writeValueAsString(obj));
    }

    public static <T> T decode(String src, Class<T> valueType) {
        return invoke(() -> MAPPER.readValue(src, valueType));
    }

    public static <T> T decode(String src, TypeReference<T> valueTypeRef) {
        return invoke(() -> MAPPER.readValue(src, valueTypeRef));
    }

    public static JsonMapper.Builder getDefaultBuilder() {
        return JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }

    /**
     * 默认不注册任何module
     **/
    public static ObjectMapper getDefaultMapper() {
        return getDefaultBuilder().build();
    }
}