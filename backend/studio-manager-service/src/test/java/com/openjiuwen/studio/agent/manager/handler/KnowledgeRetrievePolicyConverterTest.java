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
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;
import com.openjiuwen.studio.agent.manager.mapper.handler.JsonHandlerKnowledgeRetrievePolicy;

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
public class KnowledgeRetrievePolicyConverterTest {
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonHandlerKnowledgeRetrievePolicy converter;

    @Test
    public void testConvertToDatabaseColumn_Success() throws JsonProcessingException {
        // Arrange
        KnowledgeRetrievePolicy knowledgeRetrievePolicy = new KnowledgeRetrievePolicy();
        String expectedJson = "{\"search_mode\":\"doc\",\"top_k\":5,\"recall_threshold\":0.5,\"faq_threshold\":0.9,\"need_extras_faq_search\":false,\"show_source\":null,\"retrieve_image\":false,\"extra_params\":null}"    ;
        String result = converter.convertToDatabaseColumn(knowledgeRetrievePolicy);

        // Assert
        assertEquals(expectedJson, result);
    }


    @Test
    public void testConvertToDatabaseColumn_JsonProcessingException() throws JsonProcessingException {
        KnowledgeRetrievePolicy knowledgeRetrievePolicy = new KnowledgeRetrievePolicy();
        converter.convertToDatabaseColumn(knowledgeRetrievePolicy);
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
        String validJson = "{\"top_k\":5}";
        KnowledgeRetrievePolicy expectedPolicy = new KnowledgeRetrievePolicy();
        expectedPolicy.setTopK(5);

        KnowledgeRetrievePolicy result = converter.convertToEntityAttribute(validJson);
        assertEquals(expectedPolicy, result);
    }

    @Test
    public void testConvertToEntityAttribute_InvalidJson() {
        String invalidJson = "not_a_json";
        assertThrows(AgentStudioException.class, () -> converter.convertToEntityAttribute(invalidJson));
    }
}
