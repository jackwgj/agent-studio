/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;

/**
 * 功能描述
 *
 */
@Slf4j
public class JsonUtils {
    private static final ObjectMapper JSON_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()
        .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * 解析json对象
     *
     * @param jsonStr json string
     * @param type List或Map等泛型对象类定义
     * @return 对象
     */
    public static <T> T json2Obj(String jsonStr, TypeReference<T> type) {
        try {
            if (StringUtils.isBlank(jsonStr)) {
                return null;
            }
            return JSON_MAPPER.readValue(jsonStr, type);
        } catch (JsonProcessingException e) {
            log.error("can't parse this obj, return null", e);
            return null;
        }
    }

    /**
     * 解析json字符串，转换为对象
     *
     * @param jsonStr json string
     * @param clazz clazz
     * @return 对象
     */
    public static <T> T json2ObjQuietly(String jsonStr, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            return JSON_MAPPER.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            log.error("json2ObjQuietly failed.");
            return null;
        }
    }

    /**
     * object对象解析为指定类型
     *
     * @param obj 对象
     * @return 指定类型
     */
    public static <T> T objectToClass(Object obj) {
        return JSON_MAPPER.convertValue(obj, new TypeReference<>() {});
    }

    /**
     * 解析对象转换为json字符串
     *
     * @param obj 对象
     * @return json字符串
     */
    public static String toJson(Object obj) {
        try {
            return JSON_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("can't parse this obj to String, return null", e);
            return null;
        }
    }
}
