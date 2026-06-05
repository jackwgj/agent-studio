/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.aop.monitor;

import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.runtime.utils.SseEmitterUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 功能描述
 *
 */
public class RuntimeApiMonitorTest extends BaseTest {

    @MockitoBean
    RedissonClient redissonClientMock;

    @Autowired
    private RuntimeApiMonitor runtimeApiMonitor;

    @Test
    public void test_after_should_void_when_objects_is_null() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/v1/111/workflows/222/conversations");
        ServletRequestAttributes attributes = new ServletRequestAttributes(servletRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        MethodInvocationProceedingJoinPoint joinPoint = Mockito.mock(MethodInvocationProceedingJoinPoint.class);
        Object[] args = new String[] {"1", "2", "3"};
        Mockito.when(joinPoint.getArgs()).thenReturn(args);

        runtimeApiMonitor.after(joinPoint, "");
        runtimeApiMonitor.afterThrowing(joinPoint, new Exception("sss"));
    }

    @Test
    public void test_after_should_void_when_test_data_combination() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/v1/111/agents/222/conversations");
        ServletRequestAttributes attributes = new ServletRequestAttributes(servletRequest);
        RequestContextHolder.setRequestAttributes(attributes);

        MethodInvocationProceedingJoinPoint joinPoint = Mockito.mock(MethodInvocationProceedingJoinPoint.class);
        Object[] args = new String[] {"1", "2", "3"};
        Mockito.when(joinPoint.getArgs()).thenReturn(args);

        SseEmitter sseEmitter = new SseEmitter(1L);
        runtimeApiMonitor.after(joinPoint, sseEmitter);
        Runnable callback = SseEmitterUtils.getCompletionOnSseEmitter(sseEmitter, "completionCallback");
        callback.run();

        sseEmitter = new SseEmitter(1L);
        runtimeApiMonitor.after(joinPoint, sseEmitter);
        callback = SseEmitterUtils.getCompletionOnSseEmitter(sseEmitter, "timeoutCallback");
        callback.run();
        callback = SseEmitterUtils.getCompletionOnSseEmitter(sseEmitter, "completionCallback");
        callback.run();

        runtimeApiMonitor.afterThrowing(joinPoint, new Exception("sss"));
    }
}
