/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StrUtilsTest {

    @Test
    public void testGenerateShortUuid_NotNull() {
        String uuid = StrUtils.generateShortUuid();
        assertNotNull(uuid);
    }

    @Test
    public void testGenerateShortUuid_Length8() {
        String uuid = StrUtils.generateShortUuid();
        assertEquals(8, uuid.length());
    }

    @Test
    public void testGenerateShortUuid_Unique() {
        String uuid1 = StrUtils.generateShortUuid();
        String uuid2 = StrUtils.generateShortUuid();
        assertNotNull(uuid1);
        assertNotNull(uuid2);
    }

    @Test
    public void testToObjByType_NullInput() {
        assertNull(StrUtils.toObjByType(null, "string"));
    }

    @Test
    public void testToObjByType_Integer() {
        Object result = StrUtils.toObjByType("42", "integer");
        assertEquals(42, result);
    }

    @Test
    public void testToObjByType_Number() {
        Object result = StrUtils.toObjByType("3.14", "number");
        assertEquals(3.14, result);
    }

    @Test
    public void testToObjByType_Boolean() {
        Object result = StrUtils.toObjByType("true", "boolean");
        assertEquals(true, result);
    }

    @Test
    public void testToObjByType_BooleanFalse() {
        Object result = StrUtils.toObjByType("false", "boolean");
        assertEquals(false, result);
    }

    @Test
    public void testToObjByType_String() {
        Object result = StrUtils.toObjByType("hello", "string");
        assertEquals("hello", result);
    }

    @Test
    public void testToObjByType_Object() {
        Object result = StrUtils.toObjByType("{\"key\":\"value\"}", "object");
        assertNotNull(result);
    }

    @Test
    public void testToObjByType_Array() {
        Object result = StrUtils.toObjByType("[1,2,3]", "array");
        assertNotNull(result);
    }

    @Test
    public void testToObjByType_InvalidType() {
        assertThrows(IllegalArgumentException.class, () -> StrUtils.toObjByType("test", "invalid"));
    }

    @Test
    public void testTruncateText_NullInput() {
        assertNull(StrUtils.truncateText(null, 10));
    }

    @Test
    public void testTruncateText_EmptyString() {
        assertEquals("", StrUtils.truncateText("", 10));
    }

    @Test
    public void testTruncateText_ShorterThanMax() {
        assertEquals("hello", StrUtils.truncateText("hello", 10));
    }

    @Test
    public void testTruncateText_ExactlyMax() {
        assertEquals("hello", StrUtils.truncateText("hello", 5));
    }

    @Test
    public void testTruncateText_LongerThanMax() {
        assertEquals("hel", StrUtils.truncateText("hello", 3));
    }

    @Test
    public void testTruncateText_ZeroMax() {
        assertEquals("", StrUtils.truncateText("hello", 0));
    }

    @Test
    public void testFilterSpecialWords_NullInput() {
        assertNull(StrUtils.filterSpecialWords(null));
    }

    @Test
    public void testFilterSpecialWords_EmptyString() {
        assertEquals("", StrUtils.filterSpecialWords(""));
    }

    @Test
    public void testFilterSpecialWords_NoSpecialChars() {
        assertEquals("hello world", StrUtils.filterSpecialWords("hello world"));
    }

    @Test
    public void testFilterSpecialWords_WithExclamation() {
        assertEquals("hello!!", StrUtils.filterSpecialWords("hello!"));
    }

    @Test
    public void testFilterSpecialWords_WithPercent() {
        assertEquals("hello!%", StrUtils.filterSpecialWords("hello%"));
    }

    @Test
    public void testFilterSpecialWords_WithUnderscore() {
        assertEquals("hello!_", StrUtils.filterSpecialWords("hello_"));
    }

    @Test
    public void testFilterSpecialWords_MultipleSpecialChars() {
        assertEquals("a!!b!%c!_", StrUtils.filterSpecialWords("a!b%c_"));
    }
}
