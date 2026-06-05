/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.handler;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 列表数据存库转换
 */
@MappedTypes(value = {List.class})
@MappedJdbcTypes(value = {JdbcType.VARBINARY}, includeNullJdbcType = true)
public class ArrayListTypeHandler<T> extends BaseTypeHandler<List<T>> {
    private final Class<List<T>> clazz;

    public ArrayListTypeHandler(Class<List<T>> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Type argument is illegal!");
        }
        this.clazz = clazz;
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<T> ts, JdbcType jdbcType)
        throws SQLException {
        preparedStatement.setString(i, toJson(ts));
    }

    @Override
    public List<T> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return toObject(resultSet.getString(s));
    }

    @Override
    public List<T> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return toObject(resultSet.getString(i));
    }

    @Override
    public List<T> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return toObject(callableStatement.getString(i));
    }

    private String toJson(List<T> obj) {
        return JSON.toJSONString(obj);
    }

    private List<T> toObject(String json) {
        if (json != null && !json.isEmpty()) {
            return JSON.parseObject(json, new TypeReference<>() {});
        } else {
            return null;
        }
    }
}
