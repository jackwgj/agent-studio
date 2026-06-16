/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.filter.simple;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.constant.SimpleConstants;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.common.utils.simple.ServletUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SidToTokenFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private MockedStatic<SpringBeanUtils> springBeanUtilsMock;
    private MockedStatic<ServletUtils> servletUtilsMock;

    private SidToTokenFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SidToTokenFilter();
        springBeanUtilsMock = mockStatic(SpringBeanUtils.class);
        servletUtilsMock = mockStatic(ServletUtils.class);
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("");
    }

    @AfterEach
    void tearDown() {
        springBeanUtilsMock.close();
        servletUtilsMock.close();
    }

    @Test
    void testDoFilter_PathNotMatching() throws Exception {
        when(request.getRequestURI()).thenReturn("/other/path");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_HealthPath() throws Exception {
        when(request.getRequestURI()).thenReturn("/health");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        assertTrue(captor.getValue() instanceof HttpServletRequestWrapper);
    }

    @Test
    void testDoFilter_V1Path_WithSid_HeaderOverride() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("my-sid");
        when(request.getHeader(SimpleConstants.X_AUTH_TOKEN)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals("my-sid", wrapper.getHeader(SimpleConstants.X_AUTH_TOKEN));
    }

    @Test
    void testDoFilter_V2Path_WithSid_ExistingHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/v2/workflows");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("my-sid");
        when(request.getHeader(SimpleConstants.X_AUTH_TOKEN)).thenReturn("existing-token");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals("existing-token", wrapper.getHeader(SimpleConstants.X_AUTH_TOKEN));
    }

    @Test
    void testDoFilter_AuthTokenUri_ReturnsEmptyHeader() throws Exception {
        when(request.getRequestURI()).thenReturn(SimpleConstants.AUTH_TOKEN_URI);
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("my-sid");
        when(request.getHeader(SimpleConstants.X_AUTH_TOKEN)).thenReturn("some-token");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals("", wrapper.getHeader(SimpleConstants.X_AUTH_TOKEN));
    }

    @Test
    void testDoFilter_V3Path_NoSid_NoHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/some-api");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn(null);
        when(request.getHeader(SimpleConstants.X_AUTH_TOKEN)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals(null, wrapper.getHeader(SimpleConstants.X_AUTH_TOKEN));
    }

    @Test
    void testDoFilter_V3Path_EmptySid_EmptyHeader() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/some-api");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("");
        when(request.getHeader(SimpleConstants.X_AUTH_TOKEN)).thenReturn("");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals("", wrapper.getHeader(SimpleConstants.X_AUTH_TOKEN));
    }

    @Test
    void testDoFilter_NonAuthTokenHeader_PassThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("my-sid");
        when(request.getHeader("Content-Type")).thenReturn("application/json");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals("application/json", wrapper.getHeader("Content-Type"));
    }

    @Test
    void testDoFilter_WithContextPath() throws Exception {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("/api");
        when(request.getRequestURI()).thenReturn("/api/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("sid-value");
        when(request.getHeader(SimpleConstants.X_AUTH_TOKEN)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals("sid-value", wrapper.getHeader(SimpleConstants.X_AUTH_TOKEN));
    }

    @Test
    void testDoFilter_WithContextPath_AuthTokenUri() throws Exception {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("/api");
        when(request.getRequestURI()).thenReturn("/api/v3/auth/tokens");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("sid-value");
        when(request.getHeader(SimpleConstants.X_AUTH_TOKEN)).thenReturn("token");

        filter.doFilter(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.ServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper) captor.getValue();
        assertEquals("", wrapper.getHeader(SimpleConstants.X_AUTH_TOKEN));
    }

    @Test
    void testDoFilter_WithContextPath_PathNotMatching() throws Exception {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("/api");
        when(request.getRequestURI()).thenReturn("/other/path");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDestroy_NoException() {
        assertDoesNotThrow(() -> filter.destroy());
    }
}
