/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.alibaba.fastjson.JSONArray;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;

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
public class AuthKeyInfoListHandler extends BaseTypeHandler<List<AuthKeyInfo>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<AuthKeyInfo> parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, JsonUtils.toJson(parameter));
    }

    @Override
    public List<AuthKeyInfo> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, AuthKeyInfo.class);
        }
        return null;
    }

    @Override
    public List<AuthKeyInfo> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, AuthKeyInfo.class);
        }
        return null;
    }

    @Override
    public List<AuthKeyInfo> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String sqlJson = cs.getNString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, AuthKeyInfo.class);
        }
        return null;
    }
}
