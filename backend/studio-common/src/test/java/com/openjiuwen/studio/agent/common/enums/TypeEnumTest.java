/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeEnumTest {

    @Test
    void testValues() {
        assertEquals(8, TypeEnum.values().length);
    }

    @Test
    void testValueOf() {
        assertEquals(TypeEnum.INTEGER, TypeEnum.valueOf("INTEGER"));
        assertEquals(TypeEnum.STRING, TypeEnum.valueOf("STRING"));
        assertEquals(TypeEnum.BOOLEAN, TypeEnum.valueOf("BOOLEAN"));
    }

    @Test
    void testFromValue_Existing() {
        assertEquals(TypeEnum.INTEGER, TypeEnum.fromValue("integer"));
        assertEquals(TypeEnum.NUMBER, TypeEnum.fromValue("number"));
        assertEquals(TypeEnum.STRING, TypeEnum.fromValue("string"));
        assertEquals(TypeEnum.ARRAY, TypeEnum.fromValue("array"));
        assertEquals(TypeEnum.BOOLEAN, TypeEnum.fromValue("boolean"));
        assertEquals(TypeEnum.OBJECT, TypeEnum.fromValue("object"));
        assertEquals(TypeEnum.TIME, TypeEnum.fromValue("time"));
        assertEquals(TypeEnum.FILE, TypeEnum.fromValue("file"));
    }

    @Test
    void testFromValue_CaseInsensitive() {
        assertEquals(TypeEnum.INTEGER, TypeEnum.fromValue("INTEGER"));
        assertEquals(TypeEnum.STRING, TypeEnum.fromValue("String"));
    }

    @Test
    void testFromValue_NonExisting() {
        assertThrows(IllegalArgumentException.class, () -> TypeEnum.fromValue("unknown"));
    }

    @Test
    void testToString() {
        assertEquals("integer", TypeEnum.INTEGER.toString());
        assertEquals("string", TypeEnum.STRING.toString());
        assertEquals("boolean", TypeEnum.BOOLEAN.toString());
    }
}
