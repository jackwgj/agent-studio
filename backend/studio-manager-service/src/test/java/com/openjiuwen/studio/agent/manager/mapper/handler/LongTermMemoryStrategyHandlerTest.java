/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.manager.dto.LongTermMemoryStrategy;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LongTermMemoryStrategyHandlerTest {

    @Mock
    private PreparedStatement ps;

    @Mock
    private ResultSet rs;

    @Mock
    private CallableStatement cs;

    private LongTermMemoryStrategyHandler handler;

    @BeforeEach
    void setUp() {
        handler = new LongTermMemoryStrategyHandler();
    }

    @Test
    void testGetTypeReference() {
        TypeReference<List<LongTermMemoryStrategy>> typeRef = handler.getTypeReference();
        assertNotNull(typeRef);
    }

    @Test
    void testSetNonNullParameter() throws SQLException {
        List<LongTermMemoryStrategy> strategies = List.of();

        handler.setNonNullParameter(ps, 1, strategies, JdbcType.VARCHAR);

        verify(ps).setString(eq(1), anyString());
    }

    @Test
    void testGetNullableResult_ByColumnName_Null() throws SQLException {
        when(rs.getString("strategy")).thenReturn(null);

        List<LongTermMemoryStrategy> result = handler.getNullableResult(rs, "strategy");

        assertNull(result);
    }

    @Test
    void testGetNullableResult_ByColumnIndex_Null() throws SQLException {
        when(rs.getString(1)).thenReturn(null);

        List<LongTermMemoryStrategy> result = handler.getNullableResult(rs, 1);

        assertNull(result);
    }

    @Test
    void testGetNullableResult_CallableStatement_Null() throws SQLException {
        when(cs.getString(1)).thenReturn(null);

        List<LongTermMemoryStrategy> result = handler.getNullableResult(cs, 1);

        assertNull(result);
    }

    @Test
    void testGetNullableResult_ByColumnName_Blank() throws SQLException {
        when(rs.getString("strategy")).thenReturn("");

        List<LongTermMemoryStrategy> result = handler.getNullableResult(rs, "strategy");

        assertNull(result);
    }
}
