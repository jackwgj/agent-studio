/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.ErrorInfo;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.config.RuntimeI18nConfig;
import com.openjiuwen.studio.agent.runtime.dto.ErrorEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenAgentEventData;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

class RuntimeI18nServiceTest {
    private final RuntimeI18nService service = new RuntimeI18nService();

    private final MessageSource messageSource = new RuntimeI18nConfig().runtimeMessageResource();

    @BeforeEach
    public void setup() {
        I18nUtil i18nUtil = Mockito.mock(I18nUtil.class);

        ReflectionTestUtils.setField(service, "messageSource", messageSource);
        ReflectionTestUtils.setField(service, "i18nUtil", i18nUtil);

        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.setMessage("error");
        Mockito.when(i18nUtil.getMessage(Mockito.any(AgentStudioException.class))).thenReturn(errorInfo);
    }

    @Test
    public void createWorkflowErrorEventTest() {
        JiuwenEventData event = new JiuwenEventData().setCode(999999)
            .setMessage("msg")
            .setNodeId("nodeId")
            .setNodeName("nodeName")
            .setNodeType("jiuwen.start");

        ErrorEvent errorEvent = service.createWorkflowErrorEvent("workflow", event);
        Assertions.assertEquals(
            "{\"node_id\":\"nodeId\",\"node_type\":\"Start\",\"node_name\":\"nodeName\",\"code\":\"999999\",\"message\":\"msg node:nodeName nodeType:Start\",\"workflow_id\":\"workflow\",\"error_msg\":\"系统内部错误\",\"error_reason\":\"系统内部错误\",\"error_suggestion\":\"请联系技术支持进行定位\",\"error_code\":\"openjiuwen.999999\"}",
            JsonUtils.encode(errorEvent));

        event = new JiuwenEventData().setCode(0)
            .setMessage("msg")
            .setNodeId("nodeId")
            .setNodeName("nodeName")
            .setNodeType("jiuwen.start");
        errorEvent = service.createWorkflowErrorEvent("workflow", event);
        Assertions.assertEquals(
            "{\"node_id\":\"nodeId\",\"node_type\":\"Start\",\"node_name\":\"nodeName\",\"code\":\"0\",\"message\":\"msg node:nodeName nodeType:Start\",\"workflow_id\":\"workflow\",\"error_msg\":\"\",\"error_reason\":\"\",\"error_suggestion\":\"\",\"error_code\":\"openjiuwen.0\"}",
            JsonUtils.encode(errorEvent));
    }

    @Test
    public void createTokenUsageUpErrorEventTest() {
        JiuwenEventData event = new JiuwenEventData().setCode(999999)
            .setMessage("msg")
            .setNodeId("nodeId")
            .setNodeName("nodeName")
            .setMessage("Model request error: {\\\"error_code\\\":\\\"openjiuwen.02501051\\\",")
            .setNodeType("jiuwen.start");

        ErrorEvent errorEvent = service.createWorkflowErrorEvent("workflow", event);
        Assertions.assertEquals(
            "{\"node_id\":\"nodeId\",\"node_type\":\"Start\",\"node_name\":\"nodeName\",\"code\":\"999999\",\"message\":\"Model request error: {\\\\\\\"error_code\\\\\\\":\\\\\\\"openjiuwen.02501051\\\\\\\", node:nodeName nodeType:Start\",\"workflow_id\":\"workflow\",\"error_code\":\"openjiuwen.02501051\"}",
            JsonUtils.encode(errorEvent));
    }

    @Test
    public void createInputInvalidErrorEventTest() {
        JiuwenEventData event = new JiuwenEventData().setCode(101039)
            .setMessage("msg")
            .setNodeId("nodeId")
            .setNodeName("nodeName")
            .setMessage(
                "执行报错，错误码：101039，错误信息：子工作流输入验证 component execute error, error message=the inputs structure of the components does not match its definition in the IR: {'userFields': ['Incorrect type for key: nn, expected: integer'], 'systemFields': []}，请参考产品文档解决或联系系统管理员。 node:子工作流输入验证 nodeType:Workflow")
            .setNodeType("jiuwen.start");

        ErrorEvent errorEvent = service.createWorkflowErrorEvent("workflow", event);
        Assertions.assertEquals("{\"node_id\":\"nodeId\",\"node_type\":\"Start\",\"node_name\":\"nodeName\","
                + "\"code\":\"101039\",\"message\":\"执行报错，错误码：101039，错误信息：子工作流输入验证 "
                + "component execute error, error message=the inputs structure of the components "
                + "does not match its definition in the IR: {'userFields': ['Incorrect type for key: nn, expected:"
                + " integer'], 'systemFields': []}，请参考产品文档解决或联系系统管理员。 node:子工作流输入验证 "
                + "nodeType:Workflow node:nodeName nodeType:Start\",\"workflow_id\":\"workflow\",\""
                + "error_msg\":\"组件的输入参数结构与定义不匹配\",\"error_reason\":\"组件的输入参数结构与定义不匹配\","
                + "\"error_suggestion\":\"修改组件节点输入参数配置\",\"error_code\":\"openjiuwen.101039\"}",
            JsonUtils.encode(errorEvent));
    }

    @Test
    public void createAgentErrorEventTest() {
        JiuwenAgentEventData event = new JiuwenAgentEventData().setCode(999999)
            .setMessage("msg")
            .setNodeId("nodeId")
            .setNodeName("nodeName")
            .setNodeType("jiuwen.start")
            .setWorkflowId("workflow");

        ErrorEvent errorEvent = service.createErrorEvent(event);
        Assertions.assertEquals(
            "{\"node_id\":\"nodeId\",\"node_type\":\"Start\",\"node_name\":\"nodeName\",\"code\":\"999999\",\"message\":\"msg node:nodeName nodeType:Start\",\"workflow_id\":\"workflow\",\"error_msg\":\"系统内部错误\",\"error_reason\":\"系统内部错误\",\"error_suggestion\":\"请联系技术支持进行定位\",\"error_code\":\"openjiuwen.999999\"}",
            JsonUtils.encode(errorEvent));

        event = new JiuwenAgentEventData().setCode(0)
            .setMessage("msg")
            .setNodeId("nodeId")
            .setNodeName("nodeName")
            .setNodeType("jiuwen.start")
            .setWorkflowId("workflow");
        errorEvent = service.createErrorEvent(event);
        Assertions.assertEquals(
            "{\"node_id\":\"nodeId\",\"node_type\":\"Start\",\"node_name\":\"nodeName\",\"code\":\"0\",\"message\":\"msg node:nodeName nodeType:Start\",\"workflow_id\":\"workflow\",\"error_msg\":\"\",\"error_reason\":\"\",\"error_suggestion\":\"\",\"error_code\":\"openjiuwen.0\"}",
            JsonUtils.encode(errorEvent));
    }

    @Test
    public void createErrorEventTest() {
        Response rsp = new Response.Builder().request(new Request.Builder().url("https://demo.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(400)
            .message("OK")
            .header("test", "aaa")
            .body(ResponseBody.create("{\"code\":\"999999\"}", MediaType.get("application/json")))
            .build();

        ErrorEvent errorEvent = service.createErrorEvent(null, rsp);
        Assertions.assertEquals("openjiuwen.999999", errorEvent.getErrorCode());
        errorEvent = service.createErrorEvent(new AgentStudioException(StudioError.MD_THIRD_MODEL_FAIL), null);
        Assertions.assertEquals("openjiuwen.02501049", errorEvent.getErrorCode());

        errorEvent = service.createErrorEvent(new RuntimeException(""), null);
        Assertions.assertEquals("openjiuwen.02201086", errorEvent.getErrorCode());

        errorEvent = service.createErrorEvent(new java.net.ConnectException(""), null);
        Assertions.assertEquals("openjiuwen.02201086", errorEvent.getErrorCode());

        errorEvent = service.createErrorEvent(new java.net.SocketTimeoutException(""), null);
        Assertions.assertEquals("openjiuwen.02201087", errorEvent.getErrorCode());

        errorEvent = service.createErrorEvent(null, null);
        Assertions.assertEquals("openjiuwen.999999", errorEvent.getErrorCode());
    }
}
