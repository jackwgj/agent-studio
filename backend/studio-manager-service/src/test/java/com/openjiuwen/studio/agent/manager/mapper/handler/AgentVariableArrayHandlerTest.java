/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.AgentVariable;

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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentVariableArrayHandlerTest {

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    private AgentVariableArrayHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AgentVariableArrayHandler();
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        List<AgentVariable> variables = new ArrayList<>();
        AgentVariable var = new AgentVariable();
        var.setVariableKey("testKey");
        var.setDefaultValue("testValue");
        variables.add(var);

        handler.setNonNullParameter(ps, 1, variables, JdbcType.VARCHAR);

        verify(ps).setString(eq(1), anyString());
    }

    @Test
    void testGetNullableResult_ByColumnName() throws SQLException {
        String json = "[{\"key\":\"var1\",\"value\":\"val1\"}]";
        when(rs.getString("variables")).thenReturn(json);

        List<AgentVariable> result = handler.getNullableResult(rs, "variables");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetNullableResult_ByColumnName_Null() throws SQLException {
        when(rs.getString("variables")).thenReturn(null);

        List<AgentVariable> result = handler.getNullableResult(rs, "variables");

        assertNull(result);
    }

    @Test
    void testGetNullableResult_ByColumnIndex() throws SQLException {
        String json = "[{\"key\":\"var1\",\"value\":\"val1\"},{\"key\":\"var2\",\"value\":\"val2\"}]";
        when(rs.getString(1)).thenReturn(json);

        List<AgentVariable> result = handler.getNullableResult(rs, 1);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetNullableResult_ByColumnIndex_Null() throws SQLException {
        when(rs.getString(1)).thenReturn(null);

        List<AgentVariable> result = handler.getNullableResult(rs, 1);

        assertNull(result);
    }

    @Test
    void testGetNullableResult_CallableStatement() throws SQLException {
        String json = "[{\"key\":\"k1\",\"value\":\"v1\"}]";
        when(cs.getString(1)).thenReturn(json);

        List<AgentVariable> result = handler.getNullableResult(cs, 1);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testConvertToDatabaseColumn() {
        List<AgentVariable> variables = new ArrayList<>();
        AgentVariable var = new AgentVariable();
        var.setVariableKey("k1");
        var.setDefaultValue("v1");
        variables.add(var);

        String result = handler.convertToDatabaseColumn(variables);

        assertNotNull(result);
        assertTrue(result.contains("k1"));
    }

    @Test
    void testConvertToEntityAttribute_ValidJson() {
        String json = "[{\"variable_key\":\"k1\",\"default_value\":\"v1\"}]";

        List<AgentVariable> result = handler.convertToEntityAttribute(json);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testConvertToEntityAttribute_BlankJson() {
        List<AgentVariable> result = handler.convertToEntityAttribute("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToEntityAttribute_NullJson() {
        List<AgentVariable> result = handler.convertToEntityAttribute(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testConvertToEntityAttribute_InvalidJson() {
        assertThrows(AgentStudioException.class,
            () -> handler.convertToEntityAttribute("invalid-json"));
    }
}
