/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.entity.ReleaseChannel;
import com.openjiuwen.studio.agent.manager.rce.client.AgentSpaceClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
class AgentSpaceServiceTest {

    @Mock
    private AgentSpaceClient agentSpaceClient;

    @InjectMocks
    private AgentSpaceService agentSpaceService;

    @Test
    void testPublishQuery_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            JSONObject data = new JSONObject();
            data.put("publish_status", true);
            data.put("request_url", "http://example.com");
            data.put("created_date", new Date());
            JSONObject body = new JSONObject();
            body.put("data", data);

            ResponseEntity<JSONObject> response = new ResponseEntity<>(body, HttpStatus.OK);
            when(agentSpaceClient.publish(anyString(), anyString(), anyString())).thenReturn(response);

            ReleaseChannel result = agentSpaceService.publishQuery("ws-1", "app-1");
            assertNotNull(result);
            assertEquals("app-1", result.getId());
            assertEquals("released", result.getStatus());
        }
    }

    @Test
    void testPublishQuery_NotPublished() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            JSONObject data = new JSONObject();
            data.put("publish_status", false);
            JSONObject body = new JSONObject();
            body.put("data", data);

            ResponseEntity<JSONObject> response = new ResponseEntity<>(body, HttpStatus.OK);
            when(agentSpaceClient.publish(anyString(), anyString(), anyString())).thenReturn(response);

            ReleaseChannel result = agentSpaceService.publishQuery("ws-1", "app-1");
            assertNull(result);
        }
    }

    @Test
    void testPublishQuery_EmptyResponse() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            ResponseEntity<JSONObject> response = new ResponseEntity<>(new JSONObject(), HttpStatus.OK);
            when(agentSpaceClient.publish(anyString(), anyString(), anyString())).thenReturn(response);

            assertThrows(AgentStudioException.class,
                () -> agentSpaceService.publishQuery("ws-1", "app-1"));
        }
    }

    @Test
    void testPublish_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            JSONObject body = new JSONObject();
            body.put("data", "success");
            ResponseEntity<JSONObject> response = new ResponseEntity<>(body, HttpStatus.OK);
            when(agentSpaceClient.publish(anyString(), any(ReleaseChannel.class))).thenReturn(response);

            ReleaseChannel channel = new ReleaseChannel();
            channel.setId("ch-1");
            agentSpaceService.publish(channel);
        }
    }

    @Test
    void testPublish_Failed() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            JSONObject body = new JSONObject();
            body.put("data", "failed");
            ResponseEntity<JSONObject> response = new ResponseEntity<>(body, HttpStatus.OK);
            when(agentSpaceClient.publish(anyString(), any(ReleaseChannel.class))).thenReturn(response);

            ReleaseChannel channel = new ReleaseChannel();
            channel.setId("ch-1");
            assertThrows(AgentStudioException.class, () -> agentSpaceService.publish(channel));
        }
    }

    @Test
    void testUnpublish_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            JSONObject body = new JSONObject();
            body.put("data", "success");
            ResponseEntity<JSONObject> response = new ResponseEntity<>(body, HttpStatus.OK);
            when(agentSpaceClient.unpublish(anyString(), anyString(), anyString())).thenReturn(response);

            agentSpaceService.unpublish("ws-1", "app-1");
        }
    }

    @Test
    void testUnpublish_Failed() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            JSONObject body = new JSONObject();
            body.put("data", "failed");
            ResponseEntity<JSONObject> response = new ResponseEntity<>(body, HttpStatus.OK);
            when(agentSpaceClient.unpublish(anyString(), anyString(), anyString())).thenReturn(response);

            assertThrows(AgentStudioException.class,
                () -> agentSpaceService.unpublish("ws-1", "app-1"));
        }
    }

    @Test
    void testDelete_NotPublished() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            JSONObject data = new JSONObject();
            data.put("publish_status", false);
            JSONObject body = new JSONObject();
            body.put("data", data);
            ResponseEntity<JSONObject> response = new ResponseEntity<>(body, HttpStatus.OK);
            when(agentSpaceClient.publish(anyString(), anyString(), anyString())).thenReturn(response);

            agentSpaceService.delete("ws-1", "app-1");
        }
    }
}
