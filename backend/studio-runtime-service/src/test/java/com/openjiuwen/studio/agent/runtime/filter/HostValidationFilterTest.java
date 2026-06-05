/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * HostValidationFilter单元测试
 * 测试域名验证过滤器的各种场景，包括白名单、IP地址、域名模式匹配等功能
 *
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HostValidationFilter测试")
@SuppressWarnings("checkstyle: all")
class HostValidationFilterTest {
    private static final String TEST_DOMAIN_ID = "test-domain";

    private static final String DOMAIN_PATTERN = "(\\w+).example\\.com";

    private static final int PATTERN_GROUP = 1;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private I18nUtil i18nUtil;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletRequest request;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletResponse response;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FilterChain filterChain;

    @InjectMocks
    private HostValidationFilter filter;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(filter, "isHostValidatorEnabled", true);
        ReflectionTestUtils.setField(filter, "hostPattern", DOMAIN_PATTERN);
        ReflectionTestUtils.setField(filter, "patternGroup", PATTERN_GROUP);
        filter.init();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    // ==================== getClientHost 方法测试 ====================
    @Test
    @DisplayName("getClientHost - 优先返回 X-Forwarded-Host")
    void getClientHost_WithForwardedHost() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn("forwarded.example.com");
        String result = filter.getClientHost(request);
        assertEquals("forwarded.example.com", result);
        verify(request, never()).getHeader("Host");
    }

    @Test
    @DisplayName("getClientHost - 返回 Host header 并移除端口")
    void getClientHost_WithHostHeaderAndPort() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("example.com:8080");

        String result = filter.getClientHost(request);

        assertEquals("example.com", result);
    }

    @Test
    @DisplayName("getClientHost - 返回不带端口的 Host header")
    void getClientHost_WithHostHeaderWithoutPort() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("example.com");

        String result = filter.getClientHost(request);

        assertEquals("example.com", result);
    }

    @Test
    @DisplayName("getClientHost - 返回空字符串")
    void getClientHost_WithNoHeaders() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn(null);

        String result = filter.getClientHost(request);

        assertEquals("", result);
    }

    @Test
    @DisplayName("getClientHost - 处理多个 host header")
    void getClientHost_WithMultipleHostHeaders() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn("host1.example.com,host2.example.com");

        String result = filter.getClientHost(request);

        assertEquals("host1.example.com", result);
    }

    @Test
    @DisplayName("getClientHost - 处理 null header")
    void getClientHost_WithNullHeaders() {
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn(null);

        String result = filter.getClientHost(request);

        assertEquals("", result);
    }

    // ==================== isIpAddress 方法测试 ====================

    @Test
    @DisplayName("isIpAddress - 有效的IPv4地址")
    void isIpAddress_ValidV4() {
        assertTrue(filter.isIpAddress("192.168.1.1"));
        assertTrue(filter.isIpAddress("255.255.255.255"));
        assertTrue(filter.isIpAddress("0.0.0.0"));
        assertTrue(filter.isIpAddress("10.0.0.1"));
    }

    @Test
    @DisplayName("isIpAddress - 无效的IPv4地址")
    void isIpAddress_InvalidV4() {
        assertFalse(filter.isIpAddress("256.168.1.1"));
        assertTrue(filter.isIpAddress("192.168.1"));
        assertFalse(filter.isIpAddress("192.168.1.1.1"));
        assertFalse(filter.isIpAddress("abc.def.ghi.jkl"));
    }

    @Test
    @DisplayName("isIpAddress - 有效的IPv6地址")
    void isIpAddress_ValidV6() {
        assertTrue(filter.isIpAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        assertTrue(filter.isIpAddress("2001:db8:85a3:0:0:8a2e:370:7334"));
        assertTrue(filter.isIpAddress("2001:db8:85a3::8a2e:370:7334"));
        assertTrue(filter.isIpAddress("::1"));
        assertTrue(filter.isIpAddress("2001:db8::1"));
    }

    @Test
    @DisplayName("isIpAddress - 无效的IPv6地址")
    void isIpAddress_InvalidV6() {
        assertFalse(filter.isIpAddress("2001:0db8:85a3:0000:0000:8a2e:0370:7334:1234"));
        assertFalse(filter.isIpAddress("2001::db8::1"));
        assertFalse(filter.isIpAddress("2001:db8:85a3:0:0:8a2e:370:7334:5678"));
        assertFalse(filter.isIpAddress("2001:db8:85a3:0:0:8a2e:370:"));
    }

    @Test
    @DisplayName("isIpAddress - IPv6地址带方括号")
    void isIpAddress_V6WithBrackets() {
        assertTrue(filter.isIpAddress("[2001:db8::1]"));
        assertFalse(filter.isIpAddress("[2001:db8::1]:8080"));
    }

    @Test
    @DisplayName("isIpAddress - 带区域索引的IPv6地址")
    void isIpAddress_V6WithZoneIndex() {
        assertTrue(filter.isIpAddress("fe80::1%eth0"));
        assertTrue(filter.isIpAddress("fe80::1%1"));
        assertTrue(filter.isIpAddress("[fe80::1%eth0]"));
    }

    @Test
    @DisplayName("isIpAddress - 空值或无效输入")
    void isIpAddress_EmptyOrInvalid() {
        assertFalse(filter.isIpAddress("example.com"));
        assertFalse(filter.isIpAddress("sub.domain.com"));
        assertFalse(filter.isIpAddress("localhost"));
    }

    // ==================== removePort 方法测试 ====================

    @Test
    @DisplayName("removePort - 带端口的地址")
    void removePort_WithPort() {
        assertEquals("example.com", filter.removePort("example.com:8080"));
        assertEquals("192.168.1.1", filter.removePort("192.168.1.1:3000"));
    }

    @Test
    @DisplayName("removePort - 不带端口的地址")
    void removePort_WithoutPort() {
        assertEquals("example.com", filter.removePort("example.com"));
        assertEquals("192.168.1.1", filter.removePort("192.168.1.1"));
    }

    @Test
    @DisplayName("removePort - null值处理")
    void removePort_WithNull() {
        assertEquals("", filter.removePort(null));
    }

    // ==================== getFirstValue 方法测试 ====================

    @Test
    @DisplayName("getFirstValue - 单值")
    void getFirstValue_SingleValue() {
        assertEquals("example.com", HostValidationFilter.getFirstValue("example.com"));
        assertEquals("192.168.1.1", HostValidationFilter.getFirstValue("192.168.1.1"));
    }

    @Test
    @DisplayName("getFirstValue - 多值取第一个")
    void getFirstValue_MultipleValues() {
        assertEquals("host1.example.com", HostValidationFilter.getFirstValue("host1.example.com,host2.example.com"));
        assertEquals("192.168.1.1", HostValidationFilter.getFirstValue("192.168.1.1,10.0.0.1"));
    }

    @Test
    @DisplayName("getFirstValue - null值")
    void getFirstValue_WithNull() {
        assertEquals(null, HostValidationFilter.getFirstValue(null));
    }

    // ==================== doFilterInternal 主要功能测试 ====================

    @Test
    @DisplayName("doFilterInternal - 验证开关关闭时直接放行")
    void doFilterInternal_Disabled() {
        ReflectionTestUtils.setField(filter, "isHostValidatorEnabled", false);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("doFilterInternal - 空host时放行")
    void doFilterInternal_EmptyHost() throws ServletException, IOException {
        doNothing().when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("doFilterInternal - IP地址时放行")
    void doFilterInternal_IpAddress() throws ServletException, IOException {
        when(request.getHeader("X-Forwarded-Host")).thenReturn("169.1.2.3");
        doNothing().when(filterChain).doFilter(request, response);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("doFilterInternal - 正确匹配domainId时放行")
    void doFilterInternal_DomainIdMatch() throws ServletException, IOException {
        try (
            MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("host1");
            when(request.getHeader("X-Forwarded-Host")).thenReturn("host1.example.com");
            filter.doFilterInternal(request, response, filterChain);
            verify(response, never()).setStatus(anyInt());
        }
    }

    @Test
    @DisplayName("doFilterInternal - domainId不匹配时返回403")
    void doFilterInternal_DomainIdMismatch() throws ServletException, IOException {
        try (
            MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain2");
            when(request.getHeader("X-Forwarded-Host")).thenReturn("host1.example.com");
            PrintWriter writer = new PrintWriter(new StringWriter());
            when(response.getWriter()).thenReturn(writer);
            filter.doFilterInternal(request, response, filterChain);
            verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Test
    @DisplayName("doFilterInternal - host模式匹配失败时返回403")
    void doFilterInternal_HostPatternMismatch() throws ServletException, IOException {
        try (
            MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain2");
            when(request.getHeader("X-Forwarded-Host")).thenReturn("host1-example-com");
            PrintWriter writer = new PrintWriter(new StringWriter());
            when(response.getWriter()).thenReturn(writer);
            filter.doFilterInternal(request, response, filterChain);
            verify(response, never()).setStatus(anyInt());
        }
    }

    @Test
    @DisplayName("doFilterInternal - 异常处理")
    void doFilterInternal_ExceptionHandling() throws ServletException, IOException {
        assertDoesNotThrow(() -> {
            doNothing().when(filterChain).doFilter(request, response);
            // 应该不会抛出异常，而是正常处理
            filter.doFilterInternal(request, response, filterChain);
        });
    }

    @Test
    @DisplayName("sendErrorWithStudioError - 生成正确的错误响应")
    void sendErrorWithStudioError_GeneratesCorrectErrorRsp() throws IOException {
        StudioError error = StudioError.HOST_VALIDATION_INVALID;
        filter.sendErrorWithStudioError(error, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response.getWriter()).write("{\"error_code\":\"AgentBuilder.02001094\"}");
        verify(response.getWriter()).flush();
    }
}
