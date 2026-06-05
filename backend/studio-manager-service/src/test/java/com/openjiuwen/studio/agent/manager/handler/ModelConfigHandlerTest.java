/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;
import com.openjiuwen.studio.agent.manager.mapper.handler.ModelConfigHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 功能描述
 *
 */
@ExtendWith(MockitoExtension.class)
public class ModelConfigHandlerTest {
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ModelConfigHandler converter;

    @Test
    public void testConvertToDatabaseColumn() throws JsonProcessingException {
        // Arrange
        ModelConfig modelConfig = new ModelConfig(); // 假设这是一个ModelConfig类的实例
        String expectedJson = "{\"top_p\":1.0,\"temperature\":0.0,\"history_size\":20,\"output_format\":\"text\",\"max_tokens\":4096,\"frequency_penalty\":0.0,\"enable_thinking\":true}"; // 假设期望的JSON字符串

        // Act
        String result = converter.convertToDatabaseColumn(modelConfig);

        // Assert
        assertEquals(expectedJson, result);
    }

    @Test
    public void testConvertToEntityAttribute_NullInput() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    public void testConvertToEntityAttribute_EmptyInput() {
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    public void testConvertToEntityAttribute_ValidJson() throws Exception {
        String validJson = "{\"top_p\":1.0,\"temperature\":0.0,\"history_size\":20,\"output_format\":\"text\",\"max_tokens\":4096,\"frequency_penalty\":0.0,\"enable_thinking\":true}"; // 假设期望的JSON字符串
        ModelConfig expectedConfig = new ModelConfig();

        ModelConfig result = converter.convertToEntityAttribute(validJson);
        assertEquals(expectedConfig, result);
    }

    @Test
    public void testConvertToEntityAttribute_InvalidJson() throws JsonProcessingException {
        String invalidJson = "invalid json";
        assertThrows(AgentStudioException.class, () -> converter.convertToEntityAttribute(invalidJson));
    }

}
