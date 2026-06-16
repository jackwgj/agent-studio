/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SqlKeywordEnumTest {

    @Test
    void testValues() {
        assertEquals(10, SqlKeywordEnum.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(SqlKeywordEnum.WHERE, SqlKeywordEnum.valueOf("WHERE"));
        assertEquals(SqlKeywordEnum.ORDER_BY, SqlKeywordEnum.valueOf("ORDER_BY"));
        assertEquals(SqlKeywordEnum.AND, SqlKeywordEnum.valueOf("AND"));
    }

    @Test
    void testGetValue() {
        assertEquals(" WHERE ", SqlKeywordEnum.WHERE.getValue());
        assertEquals(" ORDER BY ", SqlKeywordEnum.ORDER_BY.getValue());
        assertEquals(" ASC ", SqlKeywordEnum.ASC.getValue());
        assertEquals(" DESC ", SqlKeywordEnum.DESC.getValue());
        assertEquals(" LIMIT ", SqlKeywordEnum.LIMIT.getValue());
        assertEquals(" AND ", SqlKeywordEnum.AND.getValue());
        assertEquals(" OR ", SqlKeywordEnum.OR.getValue());
        assertEquals(" (NULL) ", SqlKeywordEnum.NULL.getValue());
        assertEquals(" OFFSET ", SqlKeywordEnum.OFFSET.getValue());
    }

    @Test
    void testAllValuesNonNull() {
        for (SqlKeywordEnum kw : SqlKeywordEnum.values()) {
            assertNotNull(kw.getValue());
        }
    }
}
