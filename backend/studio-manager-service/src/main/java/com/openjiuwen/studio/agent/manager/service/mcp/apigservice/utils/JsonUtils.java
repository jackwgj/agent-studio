/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.huaweicloud.sdk.core.http.FormDataFilePart;
import com.huaweicloud.sdk.core.json.FormDataDeserializer;
import com.huaweicloud.sdk.core.json.OffsetDateTimeDeserializer;
import com.huaweicloud.sdk.core.json.StrictBooleanDeserializer;
import com.huaweicloud.sdk.core.json.StrictDoubleDeserializer;
import com.huaweicloud.sdk.core.json.StrictFloatDeserializer;
import com.huaweicloud.sdk.core.json.StrictIntegerDeserializer;
import com.huaweicloud.sdk.core.json.StrictLongDeserializer;
import com.huaweicloud.sdk.core.json.StrictStringDeserializer;
import com.huaweicloud.sdk.core.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

/**
 * @ClassName : JsonUtil
 * @Description :
 * @Date : Created in 2024/3/26 9:50
 * @Version : V1.0
 */
@Slf4j
public class JsonUtils {

    private final static ObjectMapper OBJECT_MAPPER_IGNORE_UNKNOWN = JsonUtils.initializeBaseMapper();

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return StringUtils.isEmpty(json) ? null : OBJECT_MAPPER_IGNORE_UNKNOWN.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
    }

    public static ObjectMapper initializeBaseMapper() {
        ObjectMapper mapper = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module())
            .registerModule(
                (new SimpleModule()).addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer()))
            .registerModule((new SimpleModule()).addDeserializer(Boolean.class, new StrictBooleanDeserializer()))
            .registerModule((new SimpleModule()).addDeserializer(Integer.class, new StrictIntegerDeserializer()))
            .registerModule((new SimpleModule()).addDeserializer(Long.class, new StrictLongDeserializer()))
            .registerModule((new SimpleModule()).addDeserializer(Double.class, new StrictDoubleDeserializer()))
            .registerModule((new SimpleModule()).addDeserializer(Float.class, new StrictFloatDeserializer()))
            .registerModule((new SimpleModule()).addDeserializer(String.class, new StrictStringDeserializer()))
            .registerModule((new SimpleModule()).addDeserializer(FormDataFilePart.class, new FormDataDeserializer()))
            .setFilterProvider((new SimpleFilterProvider()).setFailOnUnknownId(false));

        DeserializationConfig readConfig;
        SerializationConfig writeConfig;
        try {
            readConfig = mapper.getDeserializationConfig().with(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS);
            writeConfig = mapper.getSerializationConfig().with(JsonWriteFeature.ESCAPE_NON_ASCII);
        } catch (NoClassDefFoundError var4) {
            log.error(
                "Cannot find class definitions for JsonReadFeature and JsonWriteFeature due to version conflict, use deprecated JsonParser and JsonGenerator instead. It is recommended that you use v2.10+(currently in use: {}) for better security and compatibility.",
                mapper.version().toFullString());
            throw new AgentStudioException(StudioError.JSON_PARSER_INSTEAD);
        }

        return mapper.setConfig(readConfig).setConfig(writeConfig);
    }

}
