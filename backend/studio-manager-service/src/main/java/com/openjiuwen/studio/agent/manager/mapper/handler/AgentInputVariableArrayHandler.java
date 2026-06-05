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
import com.openjiuwen.studio.agent.manager.dto.InputVariable;
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
 * 用户输入参数InputVariable object 转 json 字符串
 *
 */
@Slf4j
public class AgentInputVariableArrayHandler extends BaseTypeHandler<List<InputVariable>>
    implements AttributeConverter<List<InputVariable>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void setNonNullParameter(PreparedStatement ps, int i, List<InputVariable> parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, JsonUtils.toJson(parameter));
    }

    @Override
    public List<InputVariable> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String result = rs.getString(columnName);
        if (result != null) {
            return JSONArray.parseArray(result, InputVariable.class);
        }
        return null;
    }

    @Override
    public List<InputVariable> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String result = rs.getString(columnIndex);
        if (result != null) {
            return JSONArray.parseArray(result, InputVariable.class);
        }
        return null;
    }

    @Override
    public List<InputVariable> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String result = cs.getString(columnIndex);
        if (result != null) {
            return JSONArray.parseArray(result, InputVariable.class);
        }
        return null;
    }

    @Override
    public String convertToDatabaseColumn(List<InputVariable> inputVariables) {
        try {
            return objectMapper.writeValueAsString(inputVariables);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON", e);
            throw new AgentStudioException(StudioError.LIST_TO_JSON_ERROR);
        }
    }

    @Override
    public List<InputVariable> convertToEntityAttribute(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<InputVariable>>() { });
        } catch (IOException e) {
            log.error("Error converting JSON to list", e);
            throw new AgentStudioException(StudioError.JSON_TO_LIST_ERROR);
        }
    }
}
