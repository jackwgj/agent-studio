/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.VoiceInteraction;
import com.openjiuwen.studio.agent.manager.mapper.handler.VoiceInteractionHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 功能描述
 *
 */
@ExtendWith(MockitoExtension.class)
public class VoiceInteractionHandlerTest {
    @InjectMocks
    private VoiceInteractionHandler converter;

    @Test
    public void testConvertToDatabaseColumn_Success() {
        // Arrange
        VoiceInteraction voiceInteraction = new VoiceInteraction();
        String expectedJson = "{\"language\":null,\"timbre\":null,\"domain\":null}";

        // Act
        String result = converter.convertToDatabaseColumn(voiceInteraction);

        // Assert
        assertEquals(expectedJson, result);
    }

    @Test
    public void testConvertToEntityAttribute_NullInput_ShouldReturnNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    public void testConvertToEntityAttribute_EmptyInput_ShouldReturnNull() {
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    public void testConvertToEntityAttribute_ValidJson_ShouldReturnObject() {
        String json = "{\"language\":null,\"timbre\":null,\"domain\":null}";
        VoiceInteraction expected = new VoiceInteraction();

        VoiceInteraction result = converter.convertToEntityAttribute(json);
        assertEquals(expected, result);
    }

    @Test
    public void testConvertToEntityAttribute_InvalidJson_ShouldThrowException() {
        String json = "invalid json";
        assertThrows(AgentStudioException.class, () -> converter.convertToEntityAttribute(json));
    }
}
