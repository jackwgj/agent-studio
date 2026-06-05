/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.utils.TestUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 功能描述
 *
 */
public class HookTriggerServiceTest extends BaseTest {
    @MockitoBean
    RedissonClient redissonClientMock;

    @Autowired
    private HookTriggerService hookTriggerService;

    @MockitoBean
    private ObsService obsService;

    @MockitoBean
    private AgentRuntimeService agentRuntimeService;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(ConstantTest.TEST_TOKEN, ConstantTest.TEST_PROJECT_ID);
    }

    @Test
    void testRunHookTrigger() {
        Map<String, String> agentIr = new HashMap<>();
        agentIr.put(Constant.Obs.MD5, ConstantTest.IR_MD5);
        agentIr.put(Constant.Obs.CONTENT, TestUtil.getStringFromFile("classpath:response/agent_ir.json"));
        when(obsService.getObject(anyString(), anyString())).thenReturn(agentIr);
        SseEmitter sseEmitter = new SseEmitter();
        AgentExecuteParams agentExecuteParams = AgentExecuteParams.builder()
            .projectId(ConstantTest.TEST_PROJECT_ID)
            .agentId(ConstantTest.TEST_AGENT_ID)
            .query("test_query")
            .conversationId(UUID.randomUUID().toString())
            .build();
        when(agentRuntimeService.runAgentStream(agentExecuteParams)).thenReturn(sseEmitter);
        Map<String, String> body = new HashMap<>();
        body.put("location", "地点");
        hookTriggerService.runHookTrigger(ConstantTest.TEST_PROJECT_ID, ConstantTest.TEST_AGENT_ID,
            ConstantTest.TEST_TRIGGER_ID, body);
    }
}
