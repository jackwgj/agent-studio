/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.handler;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.openjiuwen.studio.agent.manager.dto.AgentMemoryConfig;
import com.openjiuwen.studio.agent.manager.mapper.handler.AgentMemoryConfigHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentMemoryConfigHandlerTest {

    @InjectMocks
    private AgentMemoryConfigHandler agentMemoryConfigHandler;

    @Test
    void test_convertToEntityAttribute_should_return_not_null2() throws Exception {
        // When
        AgentMemoryConfig result = agentMemoryConfigHandler.convertToEntityAttribute(null);

        // Then
        assertNull(result);
    }
}
