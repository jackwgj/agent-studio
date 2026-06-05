/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;

import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;

/**
 * KnowledgeRetrievePolicy JsonTypeHandler
 *
 */
@MappedTypes(KnowledgeRetrievePolicy.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
@Slf4j
public class JsonHandlerKnowledgeRetrievePolicy extends AbstractJsonHandler<KnowledgeRetrievePolicy> implements
    AttributeConverter<KnowledgeRetrievePolicy, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<KnowledgeRetrievePolicy> TYPE_REF = new TypeReference<>() {};

    @Override
    protected TypeReference<KnowledgeRetrievePolicy> getTypeReference() {
        return TYPE_REF;
    }

    @Override
    public String convertToDatabaseColumn(KnowledgeRetrievePolicy knowledgeRetrievePolicy) {
        try {
            return objectMapper.writeValueAsString(knowledgeRetrievePolicy);
        } catch (JsonProcessingException e) {
            log.error("Could not convert object to JSON string", e);
            throw new AgentStudioException(StudioError.COULD_NOT_CONVERT_OBJECT_TO_JSON);
        }
    }

    @Override
    public KnowledgeRetrievePolicy convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(dbData, KnowledgeRetrievePolicy.class);
        } catch (IOException e) {
            log.error("Could not convert JSON string to object", e);
            throw new AgentStudioException(StudioError.COULD_NOT_CONVERT_JSON_STRING_TO_OBJECT);
        }
    }
}
