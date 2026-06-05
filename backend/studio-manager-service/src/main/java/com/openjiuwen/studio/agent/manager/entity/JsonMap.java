/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JsonMap {
    private final Map<String, Object> innerMap;

    // 私有构造器
    private JsonMap(Map<String, Object> map) {
        this.innerMap = map != null ? new HashMap<>(map) : new HashMap<>();
    }

    // 从Map创建JsonMap
    public static JsonMap from(Map<String, Object> map) {
        return new JsonMap(map);
    }

    // 创建空JsonMap
    public static JsonMap empty() {
        return new JsonMap(Collections.emptyMap());
    }

    // 序列化时使用
    @JsonValue
    public Map<String, Object> toMap() {
        return Collections.unmodifiableMap(innerMap);
    }

    // 反序列化时使用
    @JsonCreator
    public static JsonMap fromJson(Map<String, Object> map) {
        return new JsonMap(map);
    }

    // 提供Map操作API（可选）
    public Object get(String key) {
        return innerMap.get(key);
    }

    public JsonMap put(String key, Object value) {
        innerMap.put(key, value);
        return this;
    }
}

