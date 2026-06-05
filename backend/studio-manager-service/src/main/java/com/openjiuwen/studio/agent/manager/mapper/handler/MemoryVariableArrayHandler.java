/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.MemoryVariable;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
@Slf4j
public class MemoryVariableArrayHandler extends BaseTypeHandler<List<MemoryVariable>> implements
    AttributeConverter<List<MemoryVariable>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<MemoryVariable> parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, JsonUtils.toJson(parameter));
    }

    @Override
    public List<MemoryVariable> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, MemoryVariable.class);
        }
        return null;
    }

    @Override
    public List<MemoryVariable> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, MemoryVariable.class);
        }
        return null;
    }

    @Override
    public List<MemoryVariable> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String sqlJson = cs.getNString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, MemoryVariable.class);
        }
        return null;
    }

    @Override
    public String convertToDatabaseColumn(List<MemoryVariable> triggerConfigs) {
        try {
            return objectMapper.writeValueAsString(triggerConfigs);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON", e);
            throw new AgentStudioException(StudioError.LIST_TO_JSON_ERROR);
        }
    }

    @Override
    public List<MemoryVariable> convertToEntityAttribute(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<MemoryVariable>>() { });
        } catch (IOException e) {
            log.error("Error converting JSON to list", e);
            throw new AgentStudioException(StudioError.JSON_TO_LIST_ERROR);
        }
    }
}
