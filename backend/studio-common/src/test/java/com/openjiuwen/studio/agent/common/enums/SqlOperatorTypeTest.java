/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SqlOperatorTypeTest {

    @Test
    void testValues() {
        assertEquals(12, SqlOperatorType.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(SqlOperatorType.EQUAL, SqlOperatorType.valueOf("EQUAL"));
        assertEquals(SqlOperatorType.NOT_EQUAL, SqlOperatorType.valueOf("NOT_EQUAL"));
        assertEquals(SqlOperatorType.IN, SqlOperatorType.valueOf("IN"));
    }

    @Test
    void testGetEiType() {
        assertEquals("EQUAL", SqlOperatorType.EQUAL.getEiType());
        assertEquals("NOT_EQUAL", SqlOperatorType.NOT_EQUAL.getEiType());
        assertEquals("IS_NULL", SqlOperatorType.IS_NULL.getEiType());
    }

    @Test
    void testGetTemplate() {
        assertEquals("(%s = ?)", SqlOperatorType.EQUAL.getTemplate());
        assertEquals("(%s != ?)", SqlOperatorType.NOT_EQUAL.getTemplate());
        assertEquals("(%s IS NULL)", SqlOperatorType.IS_NULL.getTemplate());
        assertEquals("(%s IS NOT NULL)", SqlOperatorType.IS_NOT_NULL.getTemplate());
        assertEquals("(%s > ?)", SqlOperatorType.LONGER_THAN.getTemplate());
        assertEquals("(%s >= ?)", SqlOperatorType.LONGER_THAN_OR_EQUAL.getTemplate());
        assertEquals("(%s < ?)", SqlOperatorType.SHORTER_THAN.getTemplate());
        assertEquals("(%s <= ?)", SqlOperatorType.SHORTER_THAN_OR_EQUAL.getTemplate());
        assertEquals("(%s IN %s)", SqlOperatorType.IN.getTemplate());
        assertEquals("(%s NOT IN %s)", SqlOperatorType.NOT_IN.getTemplate());
        assertEquals("(%s LIKE ?)", SqlOperatorType.LIKE.getTemplate());
        assertEquals("(%s NOT LIKE ?)", SqlOperatorType.NOT_LIKE.getTemplate());
    }

    @Test
    void testAllValuesHaveNonNullFields() {
        for (SqlOperatorType type : SqlOperatorType.values()) {
            assertNotNull(type.getEiType());
            assertNotNull(type.getTemplate());
        }
    }
}
