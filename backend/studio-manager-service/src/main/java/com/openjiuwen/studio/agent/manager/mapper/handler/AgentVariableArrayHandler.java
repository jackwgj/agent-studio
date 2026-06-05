/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.AgentVariable;
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

@Slf4j
public class AgentVariableArrayHandler extends BaseTypeHandler<List<AgentVariable>>
    implements AttributeConverter<List<AgentVariable>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<AgentVariable> parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, JsonUtils.toJson(parameter));
    }

    @Override
    public List<AgentVariable> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String result = rs.getString(columnName);
        if (result != null) {
            return JSONArray.parseArray(result, AgentVariable.class);
        }
        return null;
    }

    @Override
    public List<AgentVariable> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String result = rs.getString(columnIndex);
        if (result != null) {
            return JSONArray.parseArray(result, AgentVariable.class);
        }
        return null;
    }

    @Override
    public List<AgentVariable> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String result = cs.getString(columnIndex);
        if (result != null) {
            return JSONArray.parseArray(result, AgentVariable.class);
        }
        return null;
    }

    @Override
    public String convertToDatabaseColumn(List<AgentVariable> agentVariables) {
        try {
            return objectMapper.writeValueAsString(agentVariables);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON", e);
            throw new AgentStudioException(StudioError.LIST_TO_JSON_ERROR);
        }
    }

    @Override
    public List<AgentVariable> convertToEntityAttribute(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<AgentVariable>>() {
            });
        } catch (IOException e) {
            log.error("Error converting JSON to list", e);
            throw new AgentStudioException(StudioError.JSON_TO_LIST_ERROR);
        }
    }
}
