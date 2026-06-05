/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.dto.TriggerConfig;
import com.openjiuwen.studio.agent.manager.mapper.handler.TriggerConfigArrayHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
@ExtendWith(MockitoExtension.class)
public class TriggerConfigArrayHandlerTest {

    @InjectMocks
    private TriggerConfigArrayHandler converter;

    @Test
    public void testConvertToDatabaseColumnSuccess() throws JsonProcessingException {
        List<TriggerConfig> triggerConfigs = new ArrayList<>();
        triggerConfigs.add(new TriggerConfig());
        String expectedJson = "[{\"trigger_id\":null,\"name\":null,\"type\":null,\"cron\":null,\"prompt\":null,\"invocation\":null,\"params\":null,\"hook_url\":null,\"created_on\":null}]";

        String result = converter.convertToDatabaseColumn(triggerConfigs);
        assertEquals(expectedJson, result);
    }
    @Test
    public void testConvertToEntityAttribute_WhenJsonIsNull_ShouldReturnEmptyList() {
        List<TriggerConfig> result = converter.convertToEntityAttribute(null);
        assertTrue(result.isEmpty(), "The result should be an empty list when input is null.");
    }

    @Test
    public void testConvertToEntityAttribute_WhenJsonIsBlank_ShouldReturnEmptyList() {
        List<TriggerConfig> result = converter.convertToEntityAttribute("   ");
        assertTrue(result.isEmpty(), "The result should be an empty list when input is blank.");
    }

    @Test
    public void testConvertToEntityAttribute_WhenJsonIsValid_ShouldReturnList() {
        String json = "[{\"trigger_id\":null,\"name\":null,\"type\":null,\"cron\":null,\"prompt\":null,\"invocation\":null,\"params\":null,\"hook_url\":null,\"created_on\":null}]";
        List<TriggerConfig> expected = new ArrayList<>();
        expected.add(new TriggerConfig());
        List<TriggerConfig> result = converter.convertToEntityAttribute(json);
        assertEquals(expected, result, "The result should match the expected list of TriggerConfig objects.");
    }

    @Test
    public void testConvertToEntityAttribute_WhenJsonIsInvalid_ShouldThrowException() {
        String json = "invalid json";

        assertThrows(AgentStudioException.class, () -> converter.convertToEntityAttribute(json),
            "The method should throw an AgentStudioException when the JSON is invalid.");
    }

}
