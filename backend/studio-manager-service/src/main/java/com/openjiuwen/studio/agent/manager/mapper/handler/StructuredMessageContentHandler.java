/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.manager.entity.JsonMap;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class StructuredMessageContentHandler extends BaseTypeHandler<JsonMap> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JsonMap parameter, JdbcType jdbcType)
        throws SQLException {
        try {
            // 序列化JsonMap内部数据
            ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter.toMap()));
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting JsonMap to JSON", e);
        }
    }

    @Override
    public JsonMap getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public JsonMap getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public JsonMap getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private JsonMap parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return JsonMap.empty();
        }
        try {
            Map<String, Object> map = OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
            return JsonMap.from(map);
        } catch (JsonProcessingException e) {
            // 根据业务需求决定是返回空还是抛出异常
            return JsonMap.empty();
        }
    }
}
