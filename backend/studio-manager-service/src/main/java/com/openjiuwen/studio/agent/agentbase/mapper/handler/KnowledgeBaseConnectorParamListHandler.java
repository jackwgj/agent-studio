/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectorParam;
import com.openjiuwen.studio.agent.foundation.base.utils.JacksonUtils;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 功能描述
 *
 */
public class KnowledgeBaseConnectorParamListHandler extends BaseTypeHandler<List<KnowledgeBaseConnectorParam>> {

    private static final TypeReference<List<KnowledgeBaseConnectorParam>> TYPE_REF = new TypeReference<>() { };

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<KnowledgeBaseConnectorParam> parameter,
                                    JdbcType jdbcType) throws SQLException {
        ps.setString(i, JacksonUtils.toJson(parameter));
    }

    @Override
    public List<KnowledgeBaseConnectorParam> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (sqlJson != null) {
            return JacksonUtils.readValue(sqlJson, TYPE_REF);
        }
        return null;
    }

    @Override
    public List<KnowledgeBaseConnectorParam> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (sqlJson != null) {
            return JacksonUtils.readValue(sqlJson, TYPE_REF);
        }
        return null;
    }

    @Override
    public List<KnowledgeBaseConnectorParam> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        String sqlJson = cs.getNString(columnIndex);
        if (sqlJson != null) {
            return JacksonUtils.readValue(sqlJson, TYPE_REF);
        }
        return null;
    }
}