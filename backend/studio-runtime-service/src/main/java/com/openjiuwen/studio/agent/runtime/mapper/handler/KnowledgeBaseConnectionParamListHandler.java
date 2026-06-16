/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mapper.handler;


import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.runtime.entity.KnowledgeBaseConnectionParam;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.springframework.stereotype.Component;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 功能描述
 *
 */
@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
@Component
public class KnowledgeBaseConnectionParamListHandler extends BaseTypeHandler<List<KnowledgeBaseConnectionParam>> {

    private static final TypeReference<List<KnowledgeBaseConnectionParam>> TYPE_REF = new TypeReference<>() { };

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<KnowledgeBaseConnectionParam> parameter,
                                    JdbcType jdbcType) throws SQLException {
        ps.setString(i, JsonUtils.toJson(parameter));
    }

    @Override
    public List<KnowledgeBaseConnectionParam> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (sqlJson != null) {
            return JsonUtils.json2Obj(sqlJson, TYPE_REF);
        }
        return null;
    }

    @Override
    public List<KnowledgeBaseConnectionParam> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (sqlJson != null) {
            return JsonUtils.json2Obj(sqlJson, TYPE_REF);
        }
        return null;
    }

    @Override
    public List<KnowledgeBaseConnectionParam> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String sqlJson = cs.getNString(columnIndex);
        if (sqlJson != null) {
            return JsonUtils.json2Obj(sqlJson, TYPE_REF);
        }
        return null;
    }
}