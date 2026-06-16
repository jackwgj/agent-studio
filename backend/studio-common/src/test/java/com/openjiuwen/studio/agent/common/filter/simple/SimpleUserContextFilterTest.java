/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.filter.simple;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.constant.SimpleConstants;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.common.utils.simple.ServletUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimpleUserContextFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private OkHttpClientUtils okHttpClientUtils;

    @Mock
    private OkHttpClient httpClient;

    @Mock
    private Call call;

    private MockedStatic<SpringBeanUtils> springBeanUtilsMock;
    private MockedStatic<ServletUtils> servletUtilsMock;

    private SimpleUserContextFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SimpleUserContextFilter();
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

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_AuthTokenUri() throws Exception {
        when(request.getRequestURI()).thenReturn(SimpleConstants.AUTH_TOKEN_URI);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_V1Path_EmptySid() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_V2Path_EmptySid() throws Exception {
        when(request.getRequestURI()).thenReturn("/v2/workflows");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("");

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_V3Path_WithSid_SuccessfulResponse() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/some-api");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("test-sid");

        springBeanUtilsMock.when(() -> SpringBeanUtils.getBean(OkHttpClientUtils.class)).thenReturn(okHttpClientUtils);
        when(okHttpClientUtils.getHttpClient()).thenReturn(httpClient);
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("feign.client.config.userAuth.url"), anyString()))
            .thenReturn("http://localhost");

        String userJson = "{\"userId\":\"user1\",\"token\":\"test-sid\",\"userName\":\"user1\",\"projectId\":\"proj1\"}";
        ResponseBody body = ResponseBody.create(userJson, okhttp3.MediaType.parse("application/json"));
        Response okResponse = new Response.Builder()
            .request(new Request.Builder().url("http://localhost/v3/auth/tokens").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body)
            .build();

        when(httpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(okResponse);

        filter.doFilter(request, response, filterChain);

        verify(request).setAttribute(eq("CURRENT_USER"), any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_V3Path_WithSid_FailedResponse() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/some-api");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("test-sid");

        springBeanUtilsMock.when(() -> SpringBeanUtils.getBean(OkHttpClientUtils.class)).thenReturn(okHttpClientUtils);
        when(okHttpClientUtils.getHttpClient()).thenReturn(httpClient);
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("feign.client.config.userAuth.url"), anyString()))
            .thenReturn("http://localhost");

        Response failResponse = new Response.Builder()
            .request(new Request.Builder().url("http://localhost/v3/auth/tokens").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Internal Server Error")
            .body(ResponseBody.create("", okhttp3.MediaType.parse("application/json")))
            .build();

        when(httpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(failResponse);

        filter.doFilter(request, response, filterChain);

        verify(request, never()).setAttribute(eq("CURRENT_USER"), any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_V3Path_WithSid_ExceptionThrown() throws Exception {
        when(request.getRequestURI()).thenReturn("/v3/some-api");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("test-sid");

        springBeanUtilsMock.when(() -> SpringBeanUtils.getBean(OkHttpClientUtils.class)).thenReturn(okHttpClientUtils);
        when(okHttpClientUtils.getHttpClient()).thenReturn(httpClient);
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("feign.client.config.userAuth.url"), anyString()))
            .thenReturn("http://localhost");

        when(httpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenThrow(new RuntimeException("connection failed"));

        filter.doFilter(request, response, filterChain);

        verify(request, never()).setAttribute(eq("CURRENT_USER"), any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_WithContextPath() throws Exception {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq(SimpleConstants.SERVLET_CONTEXT_PATH), anyString()))
            .thenReturn("/api");
        when(request.getRequestURI()).thenReturn("/api/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

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
    void testDoFilter_V1Path_WithSid_NullBody() throws Exception {
        when(request.getRequestURI()).thenReturn("/v1/agents");
        servletUtilsMock.when(() -> ServletUtils.getAgentSid(request)).thenReturn("test-sid");

        springBeanUtilsMock.when(() -> SpringBeanUtils.getBean(OkHttpClientUtils.class)).thenReturn(okHttpClientUtils);
        when(okHttpClientUtils.getHttpClient()).thenReturn(httpClient);
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("feign.client.config.userAuth.url"), anyString()))
            .thenReturn("http://localhost");

        Response okResponseNoBody = new Response.Builder()
            .request(new Request.Builder().url("http://localhost/v3/auth/tokens").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build();

        when(httpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenReturn(okResponseNoBody);

        filter.doFilter(request, response, filterChain);

        verify(request, never()).setAttribute(eq("CURRENT_USER"), any());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDestroy_NoException() {
        assertDoesNotThrow(() -> filter.destroy());
    }
}
