/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class JsonUtilsTest {

    @Test
    public void testEncode_SimpleObject() {
        String result = JsonUtils.encode(Map.of("key", "value"));
        assertNotNull(result);
        assertTrue(result.contains("key"));
        assertTrue(result.contains("value"));
    }

    @Test
    public void testEncode_NullObject() {
        String result = JsonUtils.encode(null);
        assertEquals("null", result);
    }

    @Test
    public void testEncode_EmptyMap() {
        String result = JsonUtils.encode(Map.of());
        assertEquals("{}", result);
    }

    @Test
    public void testDecode_ToClass() {
        String json = "{\"key\":\"value\"}";
        Map result = JsonUtils.decode(json, Map.class);
        assertNotNull(result);
        assertEquals("value", result.get("key"));
    }

    @Test
    public void testDecode_ToTypeReference() {
        String json = "{\"key\":\"value\"}";
        Map<String, String> result = JsonUtils.decode(json, new TypeReference<Map<String, String>>() {});
        assertNotNull(result);
        assertEquals("value", result.get("key"));
    }

    @Test
    public void testDecode_InvalidJson() {
        assertThrows(Exception.class, () -> JsonUtils.decode("invalid", Map.class));
    }

    @Test
    public void testDecode_EmptyJson() {
        Map result = JsonUtils.decode("{}", Map.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testEncode_ListOfStrings() {
        String result = JsonUtils.encode(List.of("a", "b", "c"));
        assertNotNull(result);
        assertTrue(result.contains("a"));
    }

    @Test
    public void testDecode_ToListOfStrings() {
        String json = "[\"a\",\"b\",\"c\"]";
        List<String> result = JsonUtils.decode(json, new TypeReference<List<String>>() {});
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void testGetDefaultMapper_NotNull() {
        ObjectMapper mapper = JsonUtils.getDefaultMapper();
        assertNotNull(mapper);
    }

    @Test
    public void testGetDefaultBuilder_NotNull() {
        assertNotNull(JsonUtils.getDefaultBuilder());
    }

    @Test
    public void testMapper_NotNull() {
        assertNotNull(JsonUtils.MAPPER);
    }

    @Test
    public void testEncode_DecodeRoundTrip() {
        Map<String, Object> original = Map.of("name", "test", "count", 42);
        String json = JsonUtils.encode(original);
        Map<String, Object> decoded = JsonUtils.decode(json, new TypeReference<Map<String, Object>>() {});
        assertEquals("test", decoded.get("name"));
        assertEquals(42, decoded.get("count"));
    }

    @Test
    public void testEncode_PojoWithNullFields() {
        TestPojo pojo = new TestPojo();
        pojo.name = "test";
        pojo.value = null;
        String json = JsonUtils.encode(pojo);
        assertNotNull(json);
        assertTrue(json.contains("test"));
    }

    @Test
    public void testDecode_UnknownProperties() {
        String json = "{\"name\":\"test\",\"unknown\":\"field\"}";
        TestPojo result = JsonUtils.decode(json, TestPojo.class);
        assertNotNull(result);
        assertEquals("test", result.name);
    }

    public static class TestPojo {
        public String name;
        public String value;
    }
}
