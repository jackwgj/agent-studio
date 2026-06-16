/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableEnumTest {

    @Test
    void testValues() {
        assertEquals(11, VariableEnum.values().length);
    }

    @Test
    void testFromDifyValue_Existing() {
        assertEquals(VariableEnum.TEXT_INPUT, VariableEnum.fromDifyValue("text-input"));
        assertEquals(VariableEnum.PARAGRAPH, VariableEnum.fromDifyValue("paragraph"));
        assertEquals(VariableEnum.SELECT, VariableEnum.fromDifyValue("select"));
        assertEquals(VariableEnum.NUMBER, VariableEnum.fromDifyValue("number"));
        assertEquals(VariableEnum.CHECKBOX, VariableEnum.fromDifyValue("checkbox"));
    }

    @Test
    void testFromDifyValue_CaseInsensitive() {
        assertEquals(VariableEnum.TEXT_INPUT, VariableEnum.fromDifyValue("TEXT-INPUT"));
    }

    @Test
    void testFromDifyValue_NonExisting() {
        assertEquals(VariableEnum.TEXT_INPUT, VariableEnum.fromDifyValue("unknown"));
    }

    @Test
    void testGetDifyVariableType() {
        assertEquals("text-input", VariableEnum.TEXT_INPUT.getDifyVariableType());
        assertEquals("number", VariableEnum.NUMBER.getDifyVariableType());
    }

    @Test
    void testGetDslType() {
        assertEquals("string", VariableEnum.TEXT_INPUT.getDslType());
        assertEquals("number", VariableEnum.NUMBER.getDslType());
        assertEquals("boolean", VariableEnum.CHECKBOX.getDslType());
        assertEquals("array", VariableEnum.ARRAY.getDslType());
    }
}
