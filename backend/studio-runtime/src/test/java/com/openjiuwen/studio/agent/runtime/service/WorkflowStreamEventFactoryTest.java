/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.JsonNode;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunStreamRsp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowStreamEventFactoryTest {
    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    public void test_exception_method() throws Exception {
        // Given
        JsonNode mockJsonNode = mock(JsonNode.class);

        // When
        WorkflowRunStreamRsp result = WorkflowStreamEventFactory.exception(mockJsonNode);

        // Then
        assertEquals("exception", result.getEvent());
        assertEquals(mockJsonNode, result.getData());
    }

    @Test
    void test_exception_should_return_not_null() throws Exception {
        // Given
        JsonNode exceptionEvent = mock(JsonNode.class, Answers.RETURNS_DEEP_STUBS);

        // When
        WorkflowRunStreamRsp result = WorkflowStreamEventFactory.exception(exceptionEvent);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_exception_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            WorkflowRunStreamRsp result = WorkflowStreamEventFactory.exception(null);
        });
    }

    @Test
    void test_exception_should_return_not_null1() throws Exception {

        // When
        WorkflowRunStreamRsp result = WorkflowStreamEventFactory.exception(null);

        // Then
        assertNotNull(result);
    }
}
