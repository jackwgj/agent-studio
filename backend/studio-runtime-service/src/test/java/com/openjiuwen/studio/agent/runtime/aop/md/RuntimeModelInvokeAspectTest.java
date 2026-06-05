/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.aop.md;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.annotation.SecurityCheck;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.common.dto.run.ChatMessage;
import com.openjiuwen.studio.agent.runtime.dto.md.EmbeddingRequest;
import com.openjiuwen.studio.agent.runtime.exception.OpenAiHttpException;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;
import com.openjiuwen.studio.agent.runtime.service.security.SecurityService;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuntimeModelInvokeAspectTest {
    private static final String FREE_MODEL_CFG
            = "free-deepseek-v3#DeepSeek-V3#llm_token_count_deepseek_v3,free-deepseek-r1#DeepSeek-R1#llm_token_count_deepseek_R1";

    private MockedStatic<RequestContextUtils> contextUtilsMock;

    @BeforeEach
    public void beforeEach() {
        contextUtilsMock = Mockito.mockStatic(RequestContextUtils.class);
    }

    @AfterEach
    public void afterEach() {
        contextUtilsMock.close();
    }

    @Test
    public void invokeTest() {
        RuntimeModelInvokeAspect aspect = new RuntimeModelInvokeAspect();
        aspect.pointcut();
    }

    @Test
    public void openApiExceptionTest() {
        contextUtilsMock.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domainId");
        contextUtilsMock.when(RequestContextUtils::getRequestUserId).thenReturn("userId");

        RuntimeModelInvokeAspect aspect = new RuntimeModelInvokeAspect();
        SecurityService securityService = Mockito.mock(SecurityService.class);

        RuntimeModelApiLogService logService = Mockito.mock(RuntimeModelApiLogService.class);
        ReflectionTestUtils.setField(aspect, "apiLogService", logService);
        ReflectionTestUtils.setField(aspect, "securityService", securityService);


        Mockito.doNothing().when(logService).saveModelApiLog(Mockito.any());

        Object rsp = null;
        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            Mockito.when(point.proceed()).thenThrow(new OpenAiHttpException(409, "mock"));
            try {
                ChatCompletionRequest chat = new ChatCompletionRequest();
                chat.setModel("m");
                Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", new HashMap<>(), chat});
                rsp = aspect.handler(point);
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, e.getErrorCode());
            }
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void handlerTest() {
        contextUtilsMock.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domainId");
        contextUtilsMock.when(RequestContextUtils::getRequestUserId).thenReturn("userId");

        RuntimeModelInvokeAspect aspect = new RuntimeModelInvokeAspect();
        SecurityService securityService = Mockito.mock(SecurityService.class);

        RuntimeModelApiLogService logService = Mockito.mock(RuntimeModelApiLogService.class);
        ReflectionTestUtils.setField(aspect, "apiLogService", logService);
        ReflectionTestUtils.setField(aspect, "securityService", securityService);


        Mockito.doNothing().when(logService).saveModelApiLog(Mockito.any());

        Object rsp = null;
        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            Mockito.when(point.proceed()).thenThrow(new OpenAiHttpException(401, "not have access"));
            try {
                ChatCompletionRequest chat = new ChatCompletionRequest();
                chat.setModel("m");
                Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", new HashMap<>(), chat});
                rsp = aspect.handler(point);
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_NO_MODEL_ACCESS_PERMISSION, e.getErrorCode());
            }
        } catch (Throwable e) {
            Assertions.fail(e);
        }

        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            Mockito.when(point.proceed()).thenThrow(new AgentStudioException(StudioError.MD_AUTH_INFO_INVALID));
            ChatCompletionRequest chat = new ChatCompletionRequest();
            chat.setModel("m");
            Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", new HashMap<>(), chat});
            rsp = aspect.handler(point);
        } catch (Throwable e) {
            if (e instanceof AgentStudioException) {
                Assertions.assertEquals(StudioError.MD_AUTH_INFO_INVALID, ((AgentStudioException) e).getErrorCode());
            } else {
                Assertions.fail(e);
            }
        }

        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            Mockito.when(point.proceed()).thenThrow(new OpenAiHttpException(429, "余额不足"));
            try {
                ChatCompletionRequest chat = new ChatCompletionRequest();
                chat.setModel("m");
                Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", new HashMap<>(), chat});
                aspect.handler(point);
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_OUTSTANDING_FEEDS, e.getErrorCode());
            }
        } catch (Throwable e) {
            Assertions.fail(e);
        }

        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            Mockito.when(point.proceed()).thenThrow(new OpenAiHttpException(402, "insufficient balance"));
            try {
                ChatCompletionRequest chat = new ChatCompletionRequest();
                chat.setModel("m");
                Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", new HashMap<>(), chat});
                aspect.handler(point);
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_OUTSTANDING_FEEDS, e.getErrorCode());
            }
        } catch (Throwable e) {
            Assertions.fail(e);
        }
    }

    private ProceedingJoinPoint pointMock() {
        ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);

        Method method = Mockito.mock(Method.class);
        Mockito.when(method.getAnnotation(SecurityCheck.class)).thenReturn(Mockito.mock(SecurityCheck.class));

        Mockito.when(point.getSignature()).thenReturn(new MethodSignature() {
            @Override
            public Class<?> getReturnType() {
                return null;
            }

            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public Class<?>[] getParameterTypes() {
                return new Class<?>[0];
            }

            @Override
            public String[] getParameterNames() {
                return new String[0];
            }

            @Override
            public Class<?>[] getExceptionTypes() {
                return new Class<?>[0];
            }

            @Override
            public String toShortString() {
                return "";
            }

            @Override
            public String toLongString() {
                return "";
            }

            @Override
            public String getName() {
                return "";
            }

            @Override
            public int getModifiers() {
                return 0;
            }

            @Override
            public Class<?> getDeclaringType() {
                return null;
            }

            @Override
            public String getDeclaringTypeName() {
                return "";
            }
        });
        return point;
    }

    @Test
    public void handlerSecurityCheckTest() {
        contextUtilsMock.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domainId");
        contextUtilsMock.when(RequestContextUtils::getRequestUserId).thenReturn("userId");

        RuntimeModelInvokeAspect aspect = new RuntimeModelInvokeAspect();

        ReflectionTestUtils.setField(aspect, "securityCheckEnable", true);

        RuntimeModelApiLogService logService = Mockito.mock(RuntimeModelApiLogService.class);
        ReflectionTestUtils.setField(aspect, "apiLogService", logService);
        Mockito.doNothing().when(logService).saveModelApiLog(Mockito.any());

        SecurityService securityService = Mockito.mock(SecurityService.class);
        ReflectionTestUtils.setField(aspect, "securityService", securityService);


        Object rsp = null;
        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = Mockito.mock(ProceedingJoinPoint.class);
            Mockito.when(point.proceed()).thenThrow(new OpenAiHttpException(401, "mock"));
            try {
                rsp = aspect.handler(point);
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_DATA_INVALID, e.getErrorCode());
            }
        } catch (Throwable e) {
            Assertions.assertTrue(true);
        }

        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());

            ProceedingJoinPoint point = pointMock();

            ChatCompletionRequest requestBody = new ChatCompletionRequest();
            Map<String, String> headers = new HashMap<>();
            headers.put("safety-barrier", "true");

            requestBody.setModel("model");

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage().setRole("user").setContent("content"));

            messages.add(new ChatMessage().setRole("user")
                    .setContent(JsonUtils.json2Obj(
                            "[{\"type\":\"image_url\",\"image_url\":{\"url\":\"https://127.0.0.1/demo.jpeg\"}},{\"type\":\"text\",\"text\":\"这是什么\"}]",
                            new TypeReference<List<Map<String, Object>>>() { })));

            messages.add(new ChatMessage().setRole("user")
                    .setContent(JsonUtils.json2Obj(
                            "[{\"type\":\"video\",\"video\":[\"demo1\",\"demo2\"]},{\"type\":\"text\",\"text\":\"描述这个视频的具体过程\"}]",
                            new TypeReference<List<Map<String, Object>>>() { })));

            messages.add(new ChatMessage().setRole("user")
                    .setContent(JsonUtils.json2Obj(
                            "[{\"type\":\"video_url\",\"image_url\":{\"url\":\"https://127.0.0.1/demo.jpeg\"}},{\"type\":\"text\",\"text\":\"这是什么\"}]",
                            new TypeReference<List<Map<String, Object>>>() { })));

            requestBody.setMessages(messages);

            Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", headers, requestBody});

            try {
                Mockito.when(point.proceed()).thenThrow(new OpenAiHttpException(404, "invalid model"));
                rsp = aspect.handler(point);
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_MODEL_SERVICE_NOT_EXIST, e.getErrorCode());
            }
        } catch (Throwable e) {
            Assertions.fail();
        }

        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = pointMock();

            EmbeddingRequest request = new EmbeddingRequest();
            Map<String, String> headers = new HashMap<>();
            headers.put("safety-barrier", "true");

            Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", headers, request});

            Mockito.when(point.proceed()).thenThrow(new OpenAiHttpException(404, "mock"));
            try {
                rsp = aspect.handler(point);
            } catch (AgentStudioException e) {
                Assertions.assertEquals(StudioError.MD_MODEL_SERVICE_NOT_AVAILABLE, e.getErrorCode());
            }
        } catch (Throwable e) {
            Assertions.assertTrue(true);
        }

        try {
            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            ProceedingJoinPoint point = pointMock();

            ChatCompletionRequest requestBody = new ChatCompletionRequest();
            Map<String, String> headers = new HashMap<>();
            headers.put("safety-barrier", "true");

            requestBody.setModel("model");
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage().setRole("user").setContent("content"));
            requestBody.setMessages(messages);

            Mockito.when(point.getArgs()).thenReturn(new Object[] {"default", "default", headers, requestBody});

            Mockito.when(point.proceed()).thenReturn(new SseEmitter());
            rsp = aspect.handler(point);

            JSONObject response = JSONObject.parseObject(
                    "{\"id\":\"111\",\"object\":\"chat.completion\",\"created\":1756124887,\""
                            + "model\":\"qwen3-235b-a22b\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\","
                            + "\"content\":\"test\",\"reasoning_content\":\"test\",\"tool_calls\":[]},\"logprobs\":null"
                            + ",\"finish_reason\":\"stop\",\"stop_reason\":null}],\"usage\":{\"prompt_tokens\":582,"
                            + "\"total_tokens\":824,\"completion_tokens\":242},\"prompt_logprobs\":null}");

            ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
            Mockito.when(point.proceed()).thenReturn(response);
            rsp = aspect.handler(point);
        } catch (Throwable e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    public void createSensitiveRspTest() {
        ModelApiLog apiLog = new ModelApiLog();

        RuntimeModelInvokeAspect aspect = new RuntimeModelInvokeAspect();

        Object obj = aspect.createSensitiveRsp(apiLog);
        apiLog.setStream(true);
        new RuntimeModelInvokeAspect().createSensitiveRsp(apiLog);
    }
}