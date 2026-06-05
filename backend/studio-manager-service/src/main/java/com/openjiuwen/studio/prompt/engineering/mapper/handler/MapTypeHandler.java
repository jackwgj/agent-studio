/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
//package com.openjiuwen.studio.prompt.engineering.mapper.handler;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.apache.ibatis.type.BaseTypeHandler;
//import org.apache.ibatis.type.JdbcType;
//import org.apache.ibatis.type.MappedJdbcTypes;
//import org.apache.ibatis.type.MappedTypes;
//
//import java.sql.CallableStatement;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;
//
//@MappedTypes(Map.class)
//@MappedJdbcTypes(JdbcType.VARCHAR) // 列为CLOB/TEXT时可改为 CLOB
//public class MapTypeHandler extends BaseTypeHandler<Map<String, String>> {
//
//    private static final ObjectMapper mapper = new ObjectMapper();
//
//    @Override
//    public void setNonNullParameter(PreparedStatement ps, int i, Map<String, String> parameter, JdbcType jdbcType) throws SQLException {
//        try {
//            ps.setString(i, mapper.writeValueAsString(parameter));
//        } catch (Exception e) {
//            ps.setString(i, "{}");
//        }
//    }
//
//    @Override
//    public Map<String, String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
//        return parseMap(rs.getString(columnName));
//    }
//
//    @Override
//    public Map<String, String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
//        return parseMap(rs.getString(columnIndex));
//    }
//
//    @Override
//    public Map<String, String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
//        return parseMap(cs.getString(columnIndex));
//    }
//
//    private Map<String, String> parseMap(String json) {
//        if (json == null || json.isEmpty()) {
//            return null;
//        }
//        try {
//            return mapper.readValue(json, new TypeReference<Map<String, String>>() {});
//        } catch (Exception e) {
//            return null;
//        }
//    }
//}
