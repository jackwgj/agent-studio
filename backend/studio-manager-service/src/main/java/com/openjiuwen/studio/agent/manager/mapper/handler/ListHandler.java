/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * List类型的Handler
 *
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
@Slf4j
public class ListHandler extends BaseTypeHandler<List<?>> implements AttributeConverter<List<String>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<?> parameter, JdbcType jdbcType)
        throws SQLException {
        // 将List转换为字符串
        ps.setString(i, JsonUtils.toJson(parameter));
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String result = rs.getString(columnName);
        return stringToList(result);
    }

    @Override
    public List<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String result = rs.getString(columnIndex);
        return stringToList(result);
    }

    @Override
    public List<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String result = cs.getString(columnIndex);
        return stringToList(result);
    }

    private List<?> stringToList(String input) {
        // 将字符串转换为List
        final String[] strArray = JsonUtils.json2ObjQuietly(input, String[].class);
        if (strArray != null) {
            return Arrays.stream(strArray).toList();
        }
        return null;
    }

    @Override
    public String convertToDatabaseColumn(List<String> triggerConfigs) {
        try {
            return objectMapper.writeValueAsString(triggerConfigs);
        } catch (JsonProcessingException e) {
            log.error("Error converting list to JSON", e);
            throw new AgentStudioException(StudioError.LIST_TO_JSON_ERROR);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() { });
        } catch (IOException e) {
            log.error("Error converting JSON to list", e);
            throw new AgentStudioException(StudioError.JSON_TO_LIST_ERROR);
        }
    }
}
