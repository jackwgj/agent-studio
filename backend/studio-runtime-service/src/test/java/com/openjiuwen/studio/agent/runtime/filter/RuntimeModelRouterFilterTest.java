/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Enumeration;

@MockitoSettings(strictness = Strictness.LENIENT)
class RuntimeModelRouterFilterTest {
    private RuntimeContextFilter runtimeModelRouterFilter = new RuntimeContextFilter(true);

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
    void test_doFilter_should_not_throw_exception() throws Exception {
        ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
        // Given
        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class, Answers.RETURNS_DEEP_STUBS);
        Mockito.when(servletRequest.getRemoteAddr()).thenReturn("nn");
        Mockito.when(servletRequest.getRequestURI()).thenReturn("/v1/abc");
        ServletResponse servletResponse = Mockito.mock(ServletResponse.class, Answers.RETURNS_DEEP_STUBS);

        FilterChain filterChain = Mockito.mock(FilterChain.class, Answers.RETURNS_DEEP_STUBS);

        // When
        runtimeModelRouterFilter.doFilter(servletRequest, servletResponse, filterChain);
        ModelApiLogThreadLocal.clear();
    }

    @SuppressWarnings("unchecked")
    @Test
    void test_doFilter_should_not_throw_exception1() throws Exception {
        ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
        FilterChain chain = Mockito.mock(FilterChain.class);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getRemoteAddr()).thenReturn("nn");
        Mockito.when(request.getRequestURI()).thenReturn("/v1/agent-builder/abc");

        Enumeration<String> headerNames = new IteratorEnumeration(Collections.singletonList("test").iterator());

        Mockito.when(request.getHeaderNames()).thenReturn(headerNames);
        Mockito.when(request.getHeader("test")).thenReturn("test");

        // When
        runtimeModelRouterFilter.doFilter(request, null, chain);
        ModelApiLogThreadLocal.clear();
    }
}
