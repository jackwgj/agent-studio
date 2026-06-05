/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper.handler;

import com.alibaba.fastjson2.JSONArray;
import com.openjiuwen.studio.agent.agentbase.model.KnowledgeBaseConnectionParam;
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
public class KnowledgeBaseConnectionParamListHandler extends BaseTypeHandler<List<KnowledgeBaseConnectionParam>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<KnowledgeBaseConnectionParam> parameter,
        JdbcType jdbcType) throws SQLException {
        ps.setString(i, JacksonUtils.toJson(parameter));
    }

    @Override
    public List<KnowledgeBaseConnectionParam> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, KnowledgeBaseConnectionParam.class);
        }
        return null;
    }

    @Override
    public List<KnowledgeBaseConnectionParam> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, KnowledgeBaseConnectionParam.class);
        }
        return null;
    }

    @Override
    public List<KnowledgeBaseConnectionParam> getNullableResult(CallableStatement cs, int columnIndex)
        throws SQLException {
        String sqlJson = cs.getNString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, KnowledgeBaseConnectionParam.class);
        }
        return null;
    }
}
