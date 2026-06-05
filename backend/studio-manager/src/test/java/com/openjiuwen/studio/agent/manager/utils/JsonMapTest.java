/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.openjiuwen.studio.agent.manager.entity.JsonMap;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class JsonMapTest {

    @Test
    void testFromMap() {
        // 准备测试数据
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("key1", "value1");
        originalMap.put("key2", 42);

        // 创建 JsonMap
        JsonMap jsonMap = JsonMap.from(originalMap);

        // 验证内容正确性
        assertThat(jsonMap.toMap())
                .containsEntry("key1", "value1")
                .containsEntry("key2", 42)
                .hasSize(2);

        // 验证原始 Map 修改不影响 JsonMap
        originalMap.put("key3", "new");
        assertThat(jsonMap.toMap()).hasSize(2);
    }

    @Test
    void testEmpty() {
        JsonMap jsonMap = JsonMap.empty();
        assertThat(jsonMap.toMap()).isEmpty();
    }

    @Test
    void testFromJson() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("name", "Test");
        inputMap.put("active", true);

        JsonMap jsonMap = JsonMap.fromJson(inputMap);
        assertThat(jsonMap.toMap())
                .containsEntry("name", "Test")
                .containsEntry("active", true)
                .hasSize(2);
    }

    @Test
    void testToMapUnmodifiable() {
        JsonMap jsonMap = JsonMap.empty().put("test", 123);
        Map<String, Object> map = jsonMap.toMap();

        // 验证不可修改
        assertThatThrownBy(() -> map.put("newKey", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testGetAndPut() {
        JsonMap jsonMap = JsonMap.empty()
                .put("string", "text")
                .put("integer", 100)
                .put("nullValue", null);

        // 验证 get 方法
        assertThat(jsonMap.get("string")).isEqualTo("text");
        assertThat(jsonMap.get("integer")).isEqualTo(100);
        assertThat(jsonMap.get("nullValue")).isNull();
        assertThat(jsonMap.get("nonExisting")).isNull();

        // 验证 put 覆盖
        jsonMap.put("integer", 200);
        assertThat(jsonMap.get("integer")).isEqualTo(200);
    }

    @Test
    void testChainPut() {
        // 链式调用测试
        JsonMap jsonMap = JsonMap.empty()
                .put("a", 1)
                .put("b", 2)
                .put("c", 3);

        assertThat(jsonMap.toMap())
                .containsEntry("a", 1)
                .containsEntry("b", 2)
                .containsEntry("c", 3);
    }

    @Test
    void testNullHandling() {
        // 测试构造器处理 null
        JsonMap fromNull = JsonMap.from(null);
        assertThat(fromNull.toMap()).isEmpty();

        // 测试 put null 值
        JsonMap jsonMap = JsonMap.empty().put("nullKey", null);
        assertThat(jsonMap.toMap()).containsKey("nullKey");
        assertThat(jsonMap.get("nullKey")).isNull();
    }
}
