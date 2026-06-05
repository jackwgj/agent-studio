/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.dto.AgentVariableRsp;
import com.openjiuwen.studio.agent.runtime.dto.GetAgentVariableQo;
import com.openjiuwen.studio.agent.runtime.dto.UpdateVariableReq;
import com.openjiuwen.studio.agent.runtime.dto.VariableInfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.client.codec.StringCodec;

import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class AgentVariableServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @InjectMocks
    private AgentVariableService agentVariableService;

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
    void test_getAgentVariable_should_return_not_null() throws Exception {
        // Given
        when(redisClient.get(anyString(), eq(StringCodec.INSTANCE))).thenReturn(
            "{\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab\":\"\",\"bbb\":{\"ccc\":\"你好111\"},\"ddd\":[],\"eee\":\"你好\",\"fff\":5}");

        GetAgentVariableQo getAgentVariableQo = new GetAgentVariableQo();

        // When
        AgentVariableRsp result = agentVariableService.getAgentVariable("123", "123", "123", getAgentVariableQo);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_updateAgentVariable_should_return_not_null() throws Exception {
        // Given
        when(redisClient.get(anyString(), eq(StringCodec.INSTANCE))).thenReturn(
            "{\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab\":\"\",\"bbb\":{\"ccc\":\"你好111\"},\"ddd\":[],\"eee\":\"你好\",\"fff\":5}");

        UpdateVariableReq body = new UpdateVariableReq();

        // When
        VariableInfo result = agentVariableService.updateAgentVariable("123", "123", "123", "123", body);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_jsonToVariableInfoList_should_return_not_null() throws Exception {

        // When
        List<VariableInfo> result = agentVariableService.jsonToVariableInfoList("{\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab\":\"\",\"bbb\":{\"ccc\":\"你好111\"},\"ddd\":[],\"eee\":\"你好\",\"fff\":5}");

        // Then
        assertNotNull(result);
    }

    @Test
    void test_updateJsonByVariableInfo_should_return_not_null() throws Exception {
        // Given
        UpdateVariableReq updateVariableReq = new UpdateVariableReq();
        updateVariableReq.setName("fff");
        updateVariableReq.setValue(10);

        // When
        Object result = agentVariableService.updateJsonByVariableInfo("{\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab\":\"\",\"bbb\":{\"ccc\":\"你好111\"},\"ddd\":[],\"eee\":\"你好\",\"fff\":5}", updateVariableReq);

        // Then
        assertNotNull(result);
    }
}
