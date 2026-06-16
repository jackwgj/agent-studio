/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.openjiuwen.studio.agent.manager.entity.JsonMap;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StructuredMessageContentHandlerTest {

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    private StructuredMessageContentHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StructuredMessageContentHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        JsonMap jsonMap = JsonMap.from(Map.of("key", "value"));

        handler.setNonNullParameter(ps, 1, jsonMap, JdbcType.VARCHAR);

        verify(ps).setString(org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.contains("key"));
    }

    @Test
    void testGetNullableResult_ByColumnName() throws SQLException {
        when(rs.getString("content")).thenReturn("{\"name\":\"test\",\"count\":42}");

        JsonMap result = handler.getNullableResult(rs, "content");

        assertNotNull(result);
        assertEquals("test", result.get("name"));
        assertEquals(42, result.get("count"));
    }

    @Test
    void testGetNullableResult_ByColumnIndex() throws SQLException {
        when(rs.getString(1)).thenReturn("{\"active\":true}");

        JsonMap result = handler.getNullableResult(rs, 1);

        assertNotNull(result);
        assertEquals(true, result.get("active"));
    }

    @Test
    void testGetNullableResult_NullJson() throws SQLException {
        when(rs.getString("content")).thenReturn(null);

        JsonMap result = handler.getNullableResult(rs, "content");

        assertNotNull(result);
    }

    @Test
    void testGetNullableResult_EmptyJson() throws SQLException {
        when(rs.getString("content")).thenReturn("  ");

        JsonMap result = handler.getNullableResult(rs, "content");

        assertNotNull(result);
    }

    @Test
    void testGetNullableResult_InvalidJson() throws SQLException {
        when(rs.getString("content")).thenReturn("not-valid-json");

        JsonMap result = handler.getNullableResult(rs, "content");

        assertNotNull(result);
    }

    @Test
    void testGetNullableResult_CallableStatement() throws SQLException {
        when(cs.getString(1)).thenReturn("{\"status\":\"ok\"}");

        JsonMap result = handler.getNullableResult(cs, 1);

        assertNotNull(result);
        assertEquals("ok", result.get("status"));
    }
}
