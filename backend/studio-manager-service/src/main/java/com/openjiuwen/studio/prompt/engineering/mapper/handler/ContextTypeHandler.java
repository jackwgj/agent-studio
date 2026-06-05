/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper.handler;

import com.openjiuwen.studio.prompt.engineering.task.domain.Context;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * ContextTypeHandler
 *
 */
public class ContextTypeHandler extends BaseTypeHandler<Context> {
    public ContextTypeHandler() {
        super();
    }
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Context parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, Context.serialize(parameter));
    }

    @Override
    public Context getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Context.deserialize(rs.getString(columnName));
    }

    @Override
    public Context getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Context.deserialize(rs.getString(columnIndex));
    }

    @Override
    public Context getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Context.deserialize(cs.getString(columnIndex));
    }
}

