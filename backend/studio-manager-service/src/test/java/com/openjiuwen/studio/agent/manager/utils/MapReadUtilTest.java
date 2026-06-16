/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class MapReadUtilTest {

    @Test
    void testSafeCastToList_ValidList() {
        List<String> input = List.of("a", "b", "c");
        List<String> result = MapReadUtil.safeCastToList(input, String.class);
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void testSafeCastToList_NotAList() {
        assertNull(MapReadUtil.safeCastToList("not a list", String.class));
    }

    @Test
    void testSafeCastToList_WrongElementType() {
        List<Integer> input = List.of(1, 2, 3);
        assertNull(MapReadUtil.safeCastToList(input, String.class));
    }

    @Test
    void testSafeCastToList_NullInput() {
        assertNull(MapReadUtil.safeCastToList(null, String.class));
    }

    @Test
    void testSafeCastToList_EmptyList() {
        List<String> result = MapReadUtil.safeCastToList(new ArrayList<>(), String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSafeCastToListWithMap_ValidList() {
        List<Map<String, Object>> input = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        input.add(map);
        List<Map<String, Object>> result = MapReadUtil.safeCastToListWithMap(input);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSafeCastToListWithMap_NotAList() {
        assertNull(MapReadUtil.safeCastToListWithMap("not a list"));
    }

    @Test
    void testSafeCastToMapWithStringKey_ValidMap() {
        Map<String, Object> input = new HashMap<>();
        input.put("key", "value");
        Map<String, Object> result = MapReadUtil.safeCastToMapWithStringKey(input);
        assertNotNull(result);
        assertEquals("value", result.get("key"));
    }

    @Test
    void testSafeCastToMapWithStringKey_NotAMap() {
        assertNull(MapReadUtil.safeCastToMapWithStringKey("not a map"));
    }

    @Test
    void testSafeCastToMapWithStringKey_NullInput() {
        assertNull(MapReadUtil.safeCastToMapWithStringKey(null));
    }

    @Test
    void testSafeCastToMapWithStringKey_NonStringKeys() {
        Map<Integer, Object> input = new HashMap<>();
        input.put(1, "value");
        assertNull(MapReadUtil.safeCastToMapWithStringKey(input));
    }

    @Test
    void testGetMapDeepValue_SingleLevel() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        String result = MapReadUtil.getMapDeepValue(map, String.class, "name");
        assertEquals("test", result);
    }

    @Test
    void testGetMapDeepValue_MultiLevel() {
        Map<String, Object> inner = new HashMap<>();
        inner.put("city", "Beijing");
        Map<String, Object> outer = new HashMap<>();
        outer.put("address", inner);
        String result = MapReadUtil.getMapDeepValue(outer, String.class, "address", "city");
        assertEquals("Beijing", result);
    }

    @Test
    void testGetMapDeepValue_KeyNotFound() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        assertNull(MapReadUtil.getMapDeepValue(map, String.class, "missing"));
    }

    @Test
    void testGetMapDeepValue_WrongType() {
        Map<String, Object> map = new HashMap<>();
        map.put("count", 42);
        assertNull(MapReadUtil.getMapDeepValue(map, String.class, "count"));
    }

    @Test
    void testGetMapDeepValue_IntermediateNotMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", "not a map");
        assertNull(MapReadUtil.getMapDeepValue(map, String.class, "value", "nested"));
    }

    @Test
    void testGetMapDeepValue_NullMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", null);
        assertNull(MapReadUtil.getMapDeepValue(map, String.class, "key", "nested"));
    }

    record TestRecord(String name, Integer age) {}

    @Test
    void testCastMapToRecord_ValidMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", 30);
        TestRecord result = MapReadUtil.castMapToRecord(map, TestRecord.class);
        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertEquals(30, result.age());
    }

    @Test
    void testCastMapToRecord_MissingField() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        TestRecord result = MapReadUtil.castMapToRecord(map, TestRecord.class);
        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertNull(result.age());
    }

    @Test
    void testCastMapToRecord_WrongType() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", 123);
        map.put("age", "not an integer");
        TestRecord result = MapReadUtil.castMapToRecord(map, TestRecord.class);
        assertNotNull(result);
    }

    record SnakeRecord(String firstName, String lastName) {}

    @Test
    void testCastMapToRecord_SnakeCaseKeys() {
        Map<String, Object> map = new HashMap<>();
        map.put("first_name", "John");
        map.put("last_name", "Doe");
        SnakeRecord result = MapReadUtil.castMapToRecord(map, SnakeRecord.class);
        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
    }
}
