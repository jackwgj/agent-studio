/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.MemoryVariable;
import com.openjiuwen.studio.agent.manager.mapper.handler.MemoryVariableArrayHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
@ExtendWith(MockitoExtension.class)
public class MemoryVariableArrayHandlerTest {
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private MemoryVariableArrayHandler converter;

    @Test
    public void testConvertToDatabaseColumnSuccess() {
        List<MemoryVariable> triggerConfigs = new ArrayList<>();
        triggerConfigs.add(new MemoryVariable()); // Add a dummy MemoryVariable object
        String expectedJson = "[{\"name\":null,\"description\":null,\"default_value\":null,\"life_cycle\":null,\"value\":null,\"updated_on\":null}]"; // Expected JSON string

        String result = converter.convertToDatabaseColumn(triggerConfigs);
        assertEquals(expectedJson, result);
    }

    @Test
    public void testConvertToEntityAttributeWithBlankJson() {
        String blankJson = "";
        List<MemoryVariable> result = converter.convertToEntityAttribute(blankJson);
        assertTrue(result.isEmpty(), "The result should be an empty list when the input is blank");
    }

    @Test
    public void testConvertToEntityAttributeWithValidJson() throws IOException {
        String validJson = "[{\"name\":\"aaa\",\"life_cycle\":\"bbb\",\"description\":null,\"default_value\":null,\"value\":null}]";
        List<MemoryVariable> expected = new ArrayList<>();
        MemoryVariable memoryVariable = new MemoryVariable();
        memoryVariable.setName("aaa");
        memoryVariable.setLifeCycle("bbb");
        expected.add(memoryVariable);

        List<MemoryVariable> result = converter.convertToEntityAttribute(validJson);
        assertEquals(expected, result, "The result should match the expected list of MemoryVariable objects");
    }

    @Test
    public void testConvertToEntityAttributeWithInvalidJson() {
        String invalidJson = "invalid json";
        assertThrows(AgentStudioException.class, () -> {
            converter.convertToEntityAttribute(invalidJson);
        }, "The method should throw an AgentStudioException when the input is invalid JSON");
    }

}
