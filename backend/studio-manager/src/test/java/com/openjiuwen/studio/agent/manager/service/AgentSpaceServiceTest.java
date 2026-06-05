/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.entity.ReleaseChannel;
import com.openjiuwen.studio.agent.manager.rce.client.AgentSpaceClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

@MockitoSettings(strictness = Strictness.LENIENT)
public class AgentSpaceServiceTest {
    @InjectMocks
    private AgentSpaceService agentSpaceService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentSpaceClient agentSpaceClient;

    private static MockedStatic<RequestContextUtils> mockedStatic;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(agentSpaceService, "agentSpaceClient", agentSpaceClient);
        JSONObject responseBody1 = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("publish_status", true);
        responseBody1.put("data", data);
        when(agentSpaceClient.publish(any(), any(), any())).thenReturn(ResponseEntity.ok(responseBody1));
        JSONObject responseBody2 = new JSONObject();
        responseBody2.put("data", "success");
        when(agentSpaceClient.publish(any(), any())).thenReturn(ResponseEntity.ok(responseBody2));
        when(agentSpaceClient.unpublish(any(), any(), any())).thenReturn(ResponseEntity.ok(responseBody2));

    }

    @Test
    void testPublishQuery() {
        ReleaseChannel result = agentSpaceService.publishQuery("workspaceId", "applicationId");
        assertEquals("applicationId", result.getId());
    }

    @Test
    void testPublish() {
        ReleaseChannel body = new ReleaseChannel();
        body.setId("id");
        agentSpaceService.publish(body);
    }

    @Test
    void testUnpublish() {
        agentSpaceService.unpublish("workspaceId", "applicationId");
    }

    @Test
    void testDelete() {
        agentSpaceService.delete("workspaceId", "applicationId");
    }
}
