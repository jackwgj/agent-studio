/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.Reference;
import com.openjiuwen.studio.agent.manager.mapper.handler.ReferenceHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 功能描述
 *
 */
@ExtendWith(MockitoExtension.class)
public class ReferenceHandlerTest {
    @InjectMocks
    private ReferenceHandler converter;

    @Test
    public void testConvertToDatabaseColumn_Success() {
        // Arrange
        Reference reference = new Reference();
        String expectedJson = "{\"switch\":false,\"json_format\":false}";

        // Act
        String result = converter.convertToDatabaseColumn(reference);

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
        String json = "{\"switch\":false,\"json_format\":false}";
        Reference expected = new Reference();

        Reference result = converter.convertToEntityAttribute(json);
        assertEquals(expected, result);
    }

    @Test
    public void testConvertToEntityAttribute_InvalidJson_ShouldThrowException() {
        String json = "invalid json";
        assertThrows(AgentStudioException.class, () -> converter.convertToEntityAttribute(json));
    }
}
