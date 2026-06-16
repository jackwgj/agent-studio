/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class IRUtilsTest {

    @Test
    void testAdaptKey_NormalSnakeCase() {
        assertEquals("helloWorld", IRUtils.adaptKey("hello_world"));
    }

    @Test
    void testAdaptKey_MultipleUnderscores() {
        assertEquals("myLongVariableName", IRUtils.adaptKey("my_long_variable_name"));
    }

    @Test
    void testAdaptKey_NoUnderscore() {
        assertEquals("hello", IRUtils.adaptKey("hello"));
    }

    @Test
    void testAdaptKey_BlankString() {
        assertEquals("", IRUtils.adaptKey(""));
    }

    @Test
    void testAdaptKey_NullString() {
        assertEquals("", IRUtils.adaptKey(null));
    }

    @Test
    void testAdaptKey_TrailingUnderscore() {
        assertEquals("hello", IRUtils.adaptKey("hello_"));
    }

    @Test
    void testAdaptKey_LeadingUnderscore() {
        assertEquals("Hello", IRUtils.adaptKey("_hello"));
    }

    @Test
    void testAdaptPreDefinedKey_SimpleKey() {
        assertEquals("helloWorld", IRUtils.adaptPreDefinedKey("hello_world"));
    }

    @Test
    void testAdaptPreDefinedKey_NestedKey() {
        assertEquals("helloWorld.some_nested", IRUtils.adaptPreDefinedKey("hello_world.some_nested"));
    }

    @Test
    void testAdaptPreDefinedKey_BlankString() {
        assertEquals("", IRUtils.adaptPreDefinedKey(""));
    }

    @Test
    void testAdaptPreDefinedKey_NullString() {
        assertEquals("", IRUtils.adaptPreDefinedKey(null));
    }

    @Test
    void testAdaptPreDefinedKey_NoDot() {
        assertEquals("abcDef", IRUtils.adaptPreDefinedKey("abc_def"));
    }

    @Test
    void testAdaptMapConfig_NormalMap() {
        Map<String, Object> input = new HashMap<>();
        input.put("hello_world", "value1");
        input.put("foo_bar", "value2");
        Map<String, Object> result = IRUtils.adaptMapConfig(input);
        assertEquals("value1", result.get("helloWorld"));
        assertEquals("value2", result.get("fooBar"));
    }

    @Test
    void testAdaptMapConfig_EmptyMap() {
        Map<String, Object> result = IRUtils.adaptMapConfig(new HashMap<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testAdaptListConfig_NormalList() {
        List<Map<String, Object>> input = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("key_one", "val1");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("key_two", "val2");
        input.add(map1);
        input.add(map2);

        List<Map<String, Object>> result = IRUtils.adaptListConfig(input);
        assertEquals(2, result.size());
        assertEquals("val1", result.get(0).get("keyOne"));
        assertEquals("val2", result.get(1).get("keyTwo"));
    }

    @Test
    void testAdaptListConfig_EmptyList() {
        List<Map<String, Object>> result = IRUtils.adaptListConfig(new ArrayList<>());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateRandomLetters_CorrectLength() {
        String result = IRUtils.generateRandomLetters(10);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test
    void testGenerateRandomLetters_OnlyLowercaseLetters() {
        String result = IRUtils.generateRandomLetters(100);
        assertTrue(result.matches("[a-z]+"));
    }

    @Test
    void testGenerateRandomLetters_ZeroLength() {
        String result = IRUtils.generateRandomLetters(0);
        assertEquals("", result);
    }

    @Test
    void testToFloat_WithNumberValue() {
        assertEquals(3.14f, IRUtils.toFloat(3.14, 0f), 0.001f);
    }

    @Test
    void testToFloat_WithIntegerValue() {
        assertEquals(42.0f, IRUtils.toFloat(42, 0f), 0.001f);
    }

    @Test
    void testToFloat_WithStringValue() {
        assertEquals(1.5f, IRUtils.toFloat("1.5", 0f), 0.001f);
    }

    @Test
    void testToFloat_WithInvalidString() {
        assertEquals(99.0f, IRUtils.toFloat("not-a-number", 99.0f), 0.001f);
    }

    @Test
    void testToFloat_WithNull() {
        assertEquals(5.0f, IRUtils.toFloat(null, 5.0f), 0.001f);
    }

    @Test
    void testToFloat_WithUnsupportedType() {
        assertEquals(7.0f, IRUtils.toFloat(new Object(), 7.0f), 0.001f);
    }

    @Test
    void testAdaptModelExtension_NullMap() {
        IRUtils.adaptModelExtension(null);
    }

    @Test
    void testAdaptModelExtension_EmptyMap() {
        Map<String, Object> map = new HashMap<>();
        IRUtils.adaptModelExtension(map);
        assertTrue(map.isEmpty());
    }
}
