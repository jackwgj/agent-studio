/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.VoiceInteraction;

import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * VoiceInteraction类的Handler
 *
 */
@Slf4j
public class VoiceInteractionHandler extends BaseTypeHandler<VoiceInteraction> implements
    AttributeConverter<VoiceInteraction, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, VoiceInteraction parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, JSONObject.toJSONString(parameter));
    }

    @Override
    public VoiceInteraction getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String result = rs.getString(columnName);
        if (result != null) {
            return JSONObject.parseObject(result, VoiceInteraction.class);
        }
        return null;
    }

    @Override
    public VoiceInteraction getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String result = rs.getString(columnIndex);
        if (result != null) {
            return JSONObject.parseObject(result, VoiceInteraction.class);
        }
        return null;
    }

    @Override
    public VoiceInteraction getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String result = cs.getString(columnIndex);
        if (result != null) {
            return JSONObject.parseObject(result, VoiceInteraction.class);
        }
        return null;
    }

    @Override
    public String convertToDatabaseColumn(VoiceInteraction voiceInteraction) {
        try {
            return objectMapper.writeValueAsString(voiceInteraction);
        } catch (JsonProcessingException e) {
            log.error("Could not convert object to JSON string", e);
            throw new AgentStudioException(StudioError.COULD_NOT_CONVERT_OBJECT_TO_JSON);
        }
    }

    @Override
    public VoiceInteraction convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(dbData, VoiceInteraction.class);
        } catch (IOException e) {
            log.error("Could not convert JSON string to object", e);
            throw new AgentStudioException(StudioError.COULD_NOT_CONVERT_JSON_STRING_TO_OBJECT);
        }
    }
}
