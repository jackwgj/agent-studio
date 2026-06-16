/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.aop;

import com.openjiuwen.studio.agent.common.annotation.OperationLog;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationLogAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private OperationLog operationLog;

    @Mock
    private MethodSignature methodSignature;

    private OperationLogAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new OperationLogAspect();
    }

    @Test
    void testAround_SwitchDisabled() throws Throwable {
        ReflectionTestUtils.setField(aspect, "operationLogSwitch", false);
        when(joinPoint.proceed()).thenReturn("result");

        Object result = aspect.around(joinPoint, operationLog);

        assertEquals("result", result);
    }

    @Test
    void testAround_SwitchEnabled_Success() throws Throwable {
        ReflectionTestUtils.setField(aspect, "operationLogSwitch", true);
        when(joinPoint.proceed()).thenReturn("result");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getTestMethod());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test/api");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            Object result = aspect.around(joinPoint, operationLog);

            assertEquals("result", result);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void testAround_SwitchEnabled_Exception() throws Throwable {
        ReflectionTestUtils.setField(aspect, "operationLogSwitch", true);
        RuntimeException ex = new RuntimeException("test error");
        when(joinPoint.proceed()).thenThrow(ex);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getTestMethod());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test/api");
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            assertThrows(RuntimeException.class, () -> aspect.around(joinPoint, operationLog));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void testAround_SwitchEnabled_NullAttributes() throws Throwable {
        ReflectionTestUtils.setField(aspect, "operationLogSwitch", true);
        when(joinPoint.proceed()).thenReturn("ok");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(getTestMethod());

        RequestContextHolder.resetRequestAttributes();

        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");

            Object result = aspect.around(joinPoint, operationLog);

            assertEquals("ok", result);
        }
    }

    private Method getTestMethod() {
        try {
            return this.getClass().getDeclaredMethod("setUp");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
