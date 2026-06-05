/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.mapper.handler.ListHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述
 *
 */
@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ListHandler converter;

    @Test
    public void testConvertToDatabaseColumnWithNonEmptyList() throws JsonProcessingException {
        // Arrange
        List<String> triggerConfigs = Arrays.asList("config1", "config2", "config3");
        String expectedJson = "[\"config1\",\"config2\",\"config3\"]";

        // Act
        String result = converter.convertToDatabaseColumn(triggerConfigs);

        // Assert
        assertEquals(expectedJson, result);
    }

    @Test
    public void testConvertToDatabaseColumnWithEmptyList() {
        // Arrange
        List<String> triggerConfigs = Collections.emptyList();
        String expectedJson = "[]";

        // Act
        String result = converter.convertToDatabaseColumn(triggerConfigs);

        // Assert
        assertEquals(expectedJson, result);
    }

    @Test
    public void testConvertToEntityAttribute_ValidJson() throws IOException {
        String json = "[\"apple\", \"banana\", \"cherry\"]";
        List<String> expected = List.of("apple", "banana", "cherry");

        List<String> result = converter.convertToEntityAttribute(json);

        assertEquals(expected, result);
    }

    @Test
    public void testConvertToEntityAttribute_InvalidJson() {
        String json = "{ \"fruit\": \"apple\" }";
        assertThrows(AgentStudioException.class, () -> converter.convertToEntityAttribute(json));
    }

    @Test
    public void testConvertToEntityAttribute_BlankJson() {
        String json = "   ";
        List<String> result = converter.convertToEntityAttribute(json);
        assertTrue(result.isEmpty());
    }
}
