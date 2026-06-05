/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.alibaba.fastjson.JSON;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * JsonSchemaUtils测试类
 *
 */
public class JsonSchemaUtilsTest {
    @Test
    void testCheckJsonSchemaValidWithNull() {
        Assertions.assertTrue(JsonSchemaUtils.isJsonSchemaValid(null, 10, 3, true));
    }

    @Test
    void test_isJsonSchemaValid_should_succeed_when_test_data_combination() {
        String jsonSchema = TestUtil.getStringFromFile("classpath:tool_param/tool_param_normal.json");
        TestUtil.logJson(JSON.parse(jsonSchema));
        Assertions.assertTrue(JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true));
    }

    @Test
    void test_isJsonSchemaValid_should_succeed_when_test_data_combination_with_output_json_schema() {
        String jsonSchema = TestUtil.getStringFromFile("classpath:tool_param/tool_param_normal.json");
        TestUtil.logJson(JSON.parse(jsonSchema));
        Assertions.assertTrue(JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, false));
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_parma_depth_over_limit() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema = TestUtil.getStringFromFile("classpath:tool_param/tool_param_over_depth.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_parma_size_over_limit() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema = TestUtil.getStringFromFile("classpath:tool_param/tool_param_over_size.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }

    @Test
    void test_isJsonSchemaValid_should_success_when_input_json_schema_without_name_cn() {
        String jsonSchema = TestUtil.getStringFromFile("classpath:tool_param/tool_param_input_without_name_cn.json");
        boolean jsonSchemaValid = JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        Assertions.assertTrue(jsonSchemaValid);
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_input_json_schema_without_desc() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema = TestUtil.getStringFromFile("classpath:tool_param/tool_param_input_without_desc.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_input_json_schema_without_location() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema =
                TestUtil.getStringFromFile("classpath:tool_param/tool_param_input_without_location.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_use_wrong_number_default_value() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema =
                TestUtil.getStringFromFile("classpath:tool_param/tool_param_input_with_wrong_number_default.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_use_wrong_integer_default_value() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema =
                TestUtil.getStringFromFile("classpath:tool_param/tool_param_input_with_wrong_integer_default.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_use_wrong_bool_default_value() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema =
                TestUtil.getStringFromFile("classpath:tool_param/tool_param_input_with_wrong_bool_default.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }

    @Test
    void test_isJsonSchemaValid_should_throw_AgentManagerException_when_input_json_schema_with_nested_array() {
        Assertions.assertThrows(AgentStudioException.class, () -> {
            String jsonSchema =
                TestUtil.getStringFromFile("classpath:tool_param/tool_param_input_with_nested_array.json");
            JsonSchemaUtils.isJsonSchemaValid(jsonSchema, 10, 3, true);
        });
    }
}
