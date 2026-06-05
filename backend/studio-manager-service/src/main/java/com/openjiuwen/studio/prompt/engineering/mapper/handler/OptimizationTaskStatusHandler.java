/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper.handler;

import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class OptimizationTaskStatusHandler extends BaseTypeHandler<OptimizationTaskStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, OptimizationTaskStatus parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, parameter.getCode());
    }

    @Override
    public OptimizationTaskStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code == null && rs.wasNull() ? null : OptimizationTaskStatus.getByCode(code);
    }

    @Override
    public OptimizationTaskStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code == null && rs.wasNull() ? null : OptimizationTaskStatus.getByCode(code);
    }

    @Override
    public OptimizationTaskStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code == null && cs.wasNull() ? null : OptimizationTaskStatus.getByCode(code);
    }
}
