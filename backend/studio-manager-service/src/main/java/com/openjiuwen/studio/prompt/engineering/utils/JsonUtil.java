/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 功能描述 Json转换工具类
 *
 */
@Slf4j
public class JsonUtil<T> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ObjectMapper ROOT_MAPPER = new ObjectMapper();

    static {
        ROOT_MAPPER.enable(SerializationFeature.WRAP_ROOT_VALUE);
        // 忽略未知属性字段
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtil() {
    }

    /**
     * serialized object
     * 序列化对象
     *
     * @param obj Object
     * @return String
     * @throws AgentStudioException
     */
    public static String object2Json(Object obj) {
        if (Objects.isNull(obj)) {
            throw new AgentStudioException(StudioError.NULL_POINT_ERROR);
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("write json error. errorMsg={}", e.getMessage());
            throw new AgentStudioException(StudioError.JSON_ENCODE_ERROR);
        }
    }

    public static <T> T json2Object(String jsonStr, TypeReference<T> type) {
        try {
            if (StringUtils.isEmpty(jsonStr)) {
                return null;
            }
            return MAPPER.readValue(jsonStr, type);
        } catch (JsonProcessingException e) {
            log.error("can't parse this obj, return null", e);
            return null;
        }
    }
}
