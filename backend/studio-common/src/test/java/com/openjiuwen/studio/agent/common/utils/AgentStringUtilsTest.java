/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.agent.common.utils.AgentStringUtils;
import org.junit.jupiter.api.Test;

public class AgentStringUtilsTest {

    @Test
    public void testMask_NullInput() {
        assertEquals(null, AgentStringUtils.mask(null), "mask(null) should return null");
    }

    @Test
    public void testMask_EmptyString() {
        assertEquals("", AgentStringUtils.mask(""), "mask(\"\") should return empty string");
    }

    @Test
    public void testMask_ShortString() {
        assertEquals("***", AgentStringUtils.mask("12"), "mask(\"12\") should return \"***\"");
    }

    @Test
    public void testMask_LongString() {
        String input = "123456";
        String expected = "1***6";
        assertEquals(expected, AgentStringUtils.mask(input), "mask(\"123456\") should return \"1***6\"");
    }

    @Test
    public void testMask_OneCharacter() {
        assertEquals("***", AgentStringUtils.mask("1"), "mask(\"1\") should return \"1\"");
    }

    @Test
    public void testMask_TwoCharacters() {
        assertEquals("***", AgentStringUtils.mask("12"), "mask(\"12\") should return \"12\"");
    }

    @Test
    public void testSqlEscapeChar_NullInput() {
        assertEquals(null, AgentStringUtils.sqlEscapeChar(null), "sqlEscapeChar(null) should return null");
    }

    @Test
    public void testSqlEscapeChar_BlankString() {
        assertEquals("", AgentStringUtils.sqlEscapeChar(""), "sqlEscapeChar(\"\") should return empty string");
    }

    @Test
    public void testSqlEscapeChar_NormalString() {
        String input = "test_string%value*";
        String expected = "test\\_string\\%value\\*";
        assertEquals(expected, AgentStringUtils.sqlEscapeChar(input),
            "sqlEscapeChar(\"test_string%value*\") should return \"test\\_string\\%value\\*\"");
    }

    @Test
    public void testSqlEscapeChar_WithBackslash() {
        String input = "test\\\\string";
        String expected = "test\\\\\\\\string";
        assertEquals(expected, AgentStringUtils.sqlEscapeChar(input),
            "sqlEscapeChar(\"test\\\\string\") should return \"test\\\\\\\\string\"");
    }
}
