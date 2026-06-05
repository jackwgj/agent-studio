/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.InputVariable;
import com.openjiuwen.studio.agent.manager.mapper.handler.AgentInputVariableArrayHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户输入参数InputVariable object 转 json AgentInputVariableArrayHandler 单元测试类
 *
 */
@ExtendWith(MockitoExtension.class)
public class AgentInputVariableArrayHandlerTest {
    @InjectMocks
    private AgentInputVariableArrayHandler agentInputVariableArrayHandler;

    @Test
    public void testConvertToDatabaseColumnSuccess() {
        List<InputVariable> inputVariables = new ArrayList<>();
        InputVariable inputVariable = new InputVariable();
        inputVariable.setInputType("string");
        inputVariable.setVariableKey("input_key1");
        inputVariable.setDefaultValue("1");
        inputVariable.setDescription("input_description");


        InputVariable inputVariable_test = new InputVariable();
        inputVariable.setInputType("integer");
        inputVariable.setVariableKey("input_key2");
        inputVariable.setDefaultValue(1);
        inputVariable.setDescription("input_description");
        String inputJsonStr =
            "[{\"variable_key\":\"input_key2\",\"required\":false,\"description\":\"input_description\",\"default_value\":1,\"input_type\":\"integer\"},{\"variable_key\":null,\"required\":false,\"description\":null,\"default_value\":null,\"input_type\":null}]"; // Expected
                                                                                                                                                                                                                                // JSON
                                                                                                                                                                                                                                // string
        inputVariables.add(inputVariable);
        inputVariables.add(inputVariable_test);
        String result = agentInputVariableArrayHandler.convertToDatabaseColumn(inputVariables);
        assertEquals(inputJsonStr, result);
    }

    @Test
    public void testConvertToEntityAttributeWithBlankJson() {
        String blankJson = "";
        List<InputVariable> result = agentInputVariableArrayHandler.convertToEntityAttribute(blankJson);
        assertTrue(result.isEmpty(), "The result should be an empty list when the input is blank");
    }

    @Test
    public void testConvertToEntityAttributeWithValidJson() throws IOException {
        String validJson =
            "[{\"variable_key\":\"input_key1\",\"description\":\"input_description\",\"default_value\":\"1\",\"input_type\":\"string\"}]";
        List<InputVariable> inputVariables = new ArrayList<>();
        InputVariable inputVariable = new InputVariable();
        inputVariable.setInputType("string");
        inputVariable.setVariableKey("input_key1");
        inputVariable.setDefaultValue("1");
        inputVariable.setDescription("input_description");
        inputVariables.add(inputVariable);

        List<InputVariable> result = agentInputVariableArrayHandler.convertToEntityAttribute(validJson);
        assertEquals(inputVariables, result, "The result should match the expected list of MemoryVariable objects");
    }

    @Test
    public void testConvertToEntityAttributeWithInvalidJson() {
        String invalidJson = "invalid json";
        assertThrows(AgentStudioException.class, () -> {
            agentInputVariableArrayHandler.convertToEntityAttribute(invalidJson);
        }, "The method should throw an AgentStudioException when the input is invalid JSON");
    }
}
