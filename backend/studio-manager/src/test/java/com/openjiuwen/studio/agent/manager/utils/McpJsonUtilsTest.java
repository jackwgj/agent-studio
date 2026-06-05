/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Type;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class McpJsonUtilsTest extends BaseTest {
    @BeforeAll
    public static void init() {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterAll
    static void end() {
    }

    @Test
    void testToJson() {
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        mcpServiceEntity.setName("test");
        McpJsonUtils.toJson(mcpServiceEntity);
    }

    @Test
    void testToList() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "test");
        Assertions.assertThrows(Exception.class,
            () -> McpJsonUtils.toList(jsonObject.toString(), String.class));
    }

    @Test
    void testToMap() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "test");
        Map<String, String> result = McpJsonUtils.toMap(jsonObject.toString(), String.class, String.class);
        Assertions.assertNotNull(result);
    }

    @Test
    void testFromJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "test");
        Map<String, String> result = McpJsonUtils.fromJson(jsonObject.toString(), new TypeReference<Map<String, String>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
        Assertions.assertNotNull(result);
    }

    @Test
    void testToPrettyJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "test");
        String result = McpJsonUtils.toPrettyJson(jsonObject.toString());
        Assertions.assertNotNull(result);
    }

    @Test
    void testStrToJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "test");
        JsonNode result = McpJsonUtils.strToJson(jsonObject.toString());
        Assertions.assertNotNull(result);
    }


}
