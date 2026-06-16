/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.filter.simple;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.constant.SimpleConstants;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.common.utils.simple.ServletUtils;
import com.openjiuwen.studio.agent.common.utils.simple.SimpleAuthUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimpleAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private MockedStatic<SpringBeanUtils> springBeanUtilsMock;
    private MockedStatic<ServletUtils> servletUtilsMock;
    private MockedStatic<SimpleAuthUtils> simpleAuthUtilsMock;

    private SimpleAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SimpleAuthFilter();
        springBeanUtilsMock = mockStatic(SpringBeanUtils.class);
        servletUtilsMock = mockStatic(ServletUtils.class);
        simpleAuthUtilsMock = mockStatic(SimpleAuthUtils.class);
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("");
    }

    @AfterEach
    void tearDown() {
        springBeanUtilsMock.close();
        servletUtilsMock.close();
        simpleAuthUtilsMock.close();
    }

    @Test
    void testDoFilter_AuthTokenUri() throws Exception {
        when(request.getRequestURI()).thenReturn(SimpleConstants.AUTH_TOKEN_URI);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        servletUtilsMock.verify(() -> ServletUtils.getAgentSid(request), never());
    }

    @Test
    void testDoFilter_NonAuthUri_WithToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("existing-sid");
        simpleAuthUtilsMock.when(() -> SimpleAuthUtils.paramToToken(request, "existing-sid")).thenReturn("user1|proj1");

        filter.doFilter(request, response, filterChain);

        verify(request).setAttribute(SimpleConstants.AGENT_SID, "user1|proj1");
        servletUtilsMock.verify(() -> ServletUtils.setSidToCookie(response, "user1|proj1"), times(1));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_NonAuthUri_EmptyToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn(null);
        simpleAuthUtilsMock.when(() -> SimpleAuthUtils.paramToToken(request, null)).thenReturn("");

        filter.doFilter(request, response, filterChain);

        verify(request, never()).setAttribute(eq(SimpleConstants.AGENT_SID), anyString());
        servletUtilsMock.verify(() -> ServletUtils.setSidToCookie(eq(response), anyString()), never());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_NonAuthUri_NullToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn(null);
        simpleAuthUtilsMock.when(() -> SimpleAuthUtils.paramToToken(request, null)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(request, never()).setAttribute(eq(SimpleConstants.AGENT_SID), anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithContextPath() throws Exception {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("/api");
        when(request.getRequestURI()).thenReturn("/api/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("sid");
        simpleAuthUtilsMock.when(() -> SimpleAuthUtils.paramToToken(request, "sid")).thenReturn("user1|proj1");

        filter.doFilter(request, response, filterChain);

        verify(request).setAttribute(SimpleConstants.AGENT_SID, "user1|proj1");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithContextPath_AuthTokenUri() throws Exception {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("/api");
        when(request.getRequestURI()).thenReturn("/api/v3/auth/tokens");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_NoExistToken_NewToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/other/path");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn(null);
        simpleAuthUtilsMock.when(() -> SimpleAuthUtils.paramToToken(request, null)).thenReturn("newUser|newProj");

        filter.doFilter(request, response, filterChain);

        verify(request).setAttribute(SimpleConstants.AGENT_SID, "newUser|newProj");
        servletUtilsMock.verify(() -> ServletUtils.setSidToCookie(response, "newUser|newProj"), times(1));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDestroy_NoException() {
        assertDoesNotThrow(() -> filter.destroy());
    }
}
