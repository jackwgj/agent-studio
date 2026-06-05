/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

@ExtendWith(MockitoExtension.class)
class JsonUtilsTest {
    @Test
    void testInitializeBaseMapper_Success() {
        // Act
        ObjectMapper mapper = JsonUtils.initializeBaseMapper();

        // Assert
        assertNotNull(mapper);
        // Verify that the mapper has the expected configurations
        assertFalse(mapper.getDeserializationConfig()
            .isEnabled(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertFalse(mapper.getSerializationConfig()
            .isEnabled(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void testToObjectWithComplexObject() {
        // Arrange
        String json = "{\"name\":\"John\",\"birthDate\":\"2020-01-01T10:00:00Z\"}";

        // Act
        TestObject result = JsonUtils.toObject(json, TestObject.class);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getName());
        assertNotNull(result.getBirthDate());
    }

    // Helper class for testing complex object mapping
    public static class TestObject {
        private String name;

        private OffsetDateTime birthDate;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public OffsetDateTime getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(OffsetDateTime birthDate) {
            this.birthDate = birthDate;
        }
    }
}
