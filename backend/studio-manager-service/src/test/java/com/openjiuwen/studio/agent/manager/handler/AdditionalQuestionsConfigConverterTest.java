/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.AdditionalQuestionsConfig;
import com.openjiuwen.studio.agent.manager.mapper.handler.AdditionalQuestionsConfigHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 功能描述
 *
 */
@ExtendWith(MockitoExtension.class)
public class AdditionalQuestionsConfigConverterTest {
    @InjectMocks
    private AdditionalQuestionsConfigHandler converter;

    @Test
    public void testConvertToDatabaseColumn_Success() throws JsonProcessingException {
        // Arrange
        AdditionalQuestionsConfig additionalQuestionsConfig = new AdditionalQuestionsConfig();
        String expectedJson = "{\"enable\":null,\"rounds\":null,\"prompt\":null}";

        // Act
        String result = converter.convertToDatabaseColumn(additionalQuestionsConfig);

        // Assert
        assertEquals(expectedJson, result);
    }

    @Test
    public void testConvertToDatabaseColumn_JsonProcessingException() throws JsonProcessingException {
        // Arrange
        AdditionalQuestionsConfig additionalQuestionsConfig = new AdditionalQuestionsConfig();
        // Act & Assert
        converter.convertToDatabaseColumn(additionalQuestionsConfig);
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
        String validJson = "{\"prompt\":\"What is your name?\"}";
        AdditionalQuestionsConfig expectedConfig = new AdditionalQuestionsConfig();
        expectedConfig.setPrompt("What is your name?");

        AdditionalQuestionsConfig result = converter.convertToEntityAttribute(validJson);
        assertEquals(expectedConfig, result);
    }

    @Test
    public void testConvertToEntityAttribute_InvalidJson() {
        String invalidJson = "invalid json";
        assertThrows(AgentStudioException.class, () -> converter.convertToEntityAttribute(invalidJson));
    }
}
