/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;

public final class JacksonUtils {

    /**
     * objectMapper
     */
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 使用下划线方式输出JSON中的属性
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // to write java.util.Date, Calendar as number (timestamp):
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // to prevent exception when encountering unknown property:
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // to allow coercion of JSON empty String ("") to null Object value:
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        // to allow C/C++ style comments in JSON (non-standard, disabled by default)
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // to enable standard indentation ("pretty-printing"):
        // ignore null value
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // to allow serialization of "empty" POJOs (no properties to serialize)
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // to allow (non-standard) unquoted field names in JSON:
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // to allow use of apostrophes (single quotes), non standard
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.registerModule(new JavaTimeModule());

    }

    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。 (1)转换为普通JavaBean：readValue(json,Student.class)
     * (2)转换为List,如List<Student>,将第二个参数传递为Student [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
     *
     * @param <T> parameter
     * @param jsonStr json对象
     * @param valueType class类型
     * @return 转换对象 t
     */
    public static <T> T readValue(String jsonStr, Class<T> valueType) {
        try {
            return objectMapper.readValue(jsonStr, valueType);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    public static <T> T readValue(byte[] src, Class<T> valueType) {
        try {
            return objectMapper.readValue(src, valueType);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    /**
     * 转换值到目标类型
     *
     * @param fromValue 源值
     * @param toValueType 目标类型
     * @param <T> 目标类型的Class类型
     * @return 目标类型的实例
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        if (fromValue == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    /**
     * 转换值到目标类型
     *
     * @param fromValue 源值
     * @param toValueType 目标类型
     * @param <T> 目标类型的Class类型
     * @return 目标类型的实例
     */
    public static <T> T convertValue(Object fromValue, JavaType toValueType) {
        if (fromValue == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(fromValue, toValueType);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    /**
     * 转换值到目标类型
     *
     * @param fromValue 源值
     * @param toValueTypeRef 目标类型
     * @param <T> 目标类型的Class类型
     * @return 目标类型的实例
     */
    public static <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) {
        if (fromValue == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(fromValue, toValueTypeRef);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    /**
     * 将json字符串转换成JavaBean
     *
     * @param <T> Bean类型
     * @param jsonContent json字符串
     * @param typeReference 类型引用
     * @return JavaBean t
     */
    public static <T> T readValue(String jsonContent, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(jsonContent, typeReference);
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    /**
     * 把JavaBean转换为json字符串
     *
     * @param object JavaBean
     * @return json字符串 string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

    /**
     * 获取json中指定key对应的value
     *
     * @param jsonContent json字符串
     * @param key 指定key
     * @return 该key对应的string string
     */
    public static String readValueByKey(String jsonContent, String key) {
        try {
            JsonNode jsonNode = readValue(jsonContent, JsonNode.class);
            String value = jsonNode.path(key).asText();
            return StringUtils.isEmpty(value) ? StringUtils.EMPTY : value;
        } catch (Exception e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }
}
