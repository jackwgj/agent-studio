/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper.handler;

import com.alibaba.fastjson.JSONArray;
import com.openjiuwen.studio.prompt.engineering.entity.PePrompt;
import com.openjiuwen.studio.prompt.engineering.utils.JsonUtil;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * List<Prompt>与json格式互转，用于与数据库交互
 *
 */
public class JsonArrayPromptHandler extends BaseTypeHandler<List<PePrompt>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<PePrompt> parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, JsonUtil.object2Json(parameter));
    }

    @Override
    public List<PePrompt> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String sqlJson = rs.getString(columnName);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, PePrompt.class);
        }
        return null;
    }

    @Override
    public List<PePrompt> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String sqlJson = rs.getString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, PePrompt.class);
        }
        return null;
    }

    @Override
    public List<PePrompt> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String sqlJson = cs.getNString(columnIndex);
        if (sqlJson != null) {
            return JSONArray.parseArray(sqlJson, PePrompt.class);
        }
        return null;
    }
}
