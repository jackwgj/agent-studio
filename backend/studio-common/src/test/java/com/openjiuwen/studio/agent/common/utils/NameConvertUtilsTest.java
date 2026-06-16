/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NameConvertUtilsTest {

    @Test
    public void testHumpToSerpentine_NullInput() {
        assertEquals("", NameConvertUtils.humpToSerpentine(null));
    }

    @Test
    public void testHumpToSerpentine_EmptyString() {
        assertEquals("", NameConvertUtils.humpToSerpentine(""));
    }

    @Test
    public void testHumpToSerpentine_BlankString() {
        assertEquals("", NameConvertUtils.humpToSerpentine("   "));
    }

    @Test
    public void testHumpToSerpentine_SimpleCamelCase() {
        assertEquals("hello_world", NameConvertUtils.humpToSerpentine("helloWorld"));
    }

    @Test
    public void testHumpToSerpentine_AllLowerCase() {
        assertEquals("hello", NameConvertUtils.humpToSerpentine("hello"));
    }

    @Test
    public void testHumpToSerpentine_MultipleUpperCase() {
        assertEquals("hello_beautiful_world", NameConvertUtils.humpToSerpentine("helloBeautifulWorld"));
    }

    @Test
    public void testHumpToSerpentine_StartsWithUpperCase() {
        assertEquals("_hello_world", NameConvertUtils.humpToSerpentine("HelloWorld"));
    }

    @Test
    public void testHumpToSerpentine_SingleChar() {
        assertEquals("a", NameConvertUtils.humpToSerpentine("a"));
    }

    @Test
    public void testHumpToSerpentine_SingleUpperCase() {
        assertEquals("_a", NameConvertUtils.humpToSerpentine("A"));
    }

    @Test
    public void testHumpToSerpentine_ConsecutiveUpperCase() {
        assertEquals("_h_t_t_p_request", NameConvertUtils.humpToSerpentine("HTTPRequest"));
    }
}
