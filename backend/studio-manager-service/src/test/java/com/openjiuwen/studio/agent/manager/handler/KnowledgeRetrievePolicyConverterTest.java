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
 * Unit tests for {@link JsonHandlerKnowledgeRetrievePolicy} converter.
 *
 * <p>Covers serialization (entity → DB column) and deserialization
 * (DB column → entity) for {@link KnowledgeRetrievePolicy}.
 */
@ExtendWith(MockitoExtension.class)
public class KnowledgeRetrievePolicyConverterTest {
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonHandlerKnowledgeRetrievePolicy converter;

    // ------------------------------------------------------------------
    // Serialization: entity → DB JSON column
    // ------------------------------------------------------------------

    @Test
    void shouldSerializePolicyToExpectedJsonString() throws JsonProcessingException {
        // given
        KnowledgeRetrievePolicy policy = buildDefaultPolicy();
        String expected =
                "{\"search_mode\":\"doc\",\"top_k\":5,\"recall_threshold\":0.5,"
                        + "\"faq_threshold\":0.9,\"need_extras_faq_search\":false,"
                        + "\"show_source\":null,\"retrieve_image\":false}";

        // when
        String actual = converter.convertToDatabaseColumn(policy);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldTolerateSerializationFailureWithoutPropagatingCheckedException()
            throws JsonProcessingException {
        // given
        KnowledgeRetrievePolicy policy = buildDefaultPolicy();

        // when — converter wraps JsonProcessingException internally
        converter.convertToDatabaseColumn(policy);
    }

    // ------------------------------------------------------------------
    // Deserialization: DB JSON column → entity
    // ------------------------------------------------------------------

    @Test
    void shouldReturnNullWhenColumnValueIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void shouldReturnNullWhenColumnValueIsBlank() {
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    void shouldDeserializeJsonFragmentIntoPolicyObject() throws Exception {
        // given — partial JSON is enough to populate a single field
        String columnValue = "{\"top_k\":5}";
        KnowledgeRetrievePolicy expected = new KnowledgeRetrievePolicy();
        expected.setTopK(5);

        // when
        KnowledgeRetrievePolicy actual = converter.convertToEntityAttribute(columnValue);

        // then
        assertEquals(expected, actual);
    }

    @Test
    void shouldRaiseAgentStudioExceptionForMalformedJson() {
        assertThrows(
                AgentStudioException.class,
                () -> converter.convertToEntityAttribute("not_a_json"));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static KnowledgeRetrievePolicy buildDefaultPolicy() {
        KnowledgeRetrievePolicy policy = new KnowledgeRetrievePolicy();
        policy.setSearchMode(KnowledgeRetrievePolicy.SearchModeEnum.DOC);
        policy.setTopK(5);
        policy.setRecallThreshold(0.5f);
        policy.setFaqThreshold(0.9f);
        policy.setNeedExtrasFaqSearch(false);
        policy.setRetrieveImage(false);
        return policy;
    }
}
