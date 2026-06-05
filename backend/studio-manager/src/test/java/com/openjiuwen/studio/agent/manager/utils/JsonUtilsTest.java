/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.manager.constant.Constants;

import lombok.Data;

import org.junit.jupiter.api.Test;

import java.util.Objects;

/**
 * 功能描述
 *
 */
class JsonUtilsTest {

    @Test
    void json2Obj() {
        TypeReference<Usage> type = new TypeReference<>() {};

        // test valid str
        Usage validResult = JsonUtils.json2Obj(Constants.JSON_STRING, type);
        assertNotNull(validResult);
        assertEquals(1L, validResult.getInputTokens());

        // test invalid str
        Usage invalidResult = JsonUtils.json2Obj("str", type);
        assertNull(invalidResult);
    }

    @Test
    void json2ObjQuietly() {
        // test valid str
        Usage validResult = JsonUtils.json2ObjQuietly(Constants.JSON_STRING, Usage.class);
        assertNotNull(validResult);
        assertEquals(1L, validResult.getInputTokens());

        // test null str
        Usage nullResult = JsonUtils.json2ObjQuietly(null, Usage.class);
        assertNull(nullResult);

        // test invalid str
        Usage invalidResult = JsonUtils.json2ObjQuietly("str", Usage.class);
        assertNull(invalidResult);
    }

    @Test
    void toJson() {
        // test valid obj
        Usage usage = new Usage();
        usage.setInputTokens(1L);
        usage.setOutputTokens(2L);
        usage.setCost(3L);
        assertTrue(Objects.requireNonNull(JsonUtils.toJson(usage)).contains("input_tokens"));
        assertTrue(Objects.requireNonNull(JsonUtils.toJson(usage)).contains("output_tokens"));
        assertTrue(Objects.requireNonNull(JsonUtils.toJson(usage)).contains("cost"));
    }

    @Data
    private static class Usage {
        @JsonProperty("input_tokens")
        private Long inputTokens;

        @JsonProperty("output_tokens")
        private Long outputTokens;

        private Long cost;
    }
}
