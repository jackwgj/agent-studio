/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.utils.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.constant.SimpleConstants;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;

import jakarta.servlet.http.Cookie;
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
public class ServletUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private MockedStatic<SpringBeanUtils> springBeanUtilsMock;

    @BeforeEach
    void setUp() {
        springBeanUtilsMock = mockStatic(SpringBeanUtils.class);
    }

    @AfterEach
    void tearDown() {
        springBeanUtilsMock.close();
    }

    @Test
    void testGenAgentSid_Success() {
        String sid = ServletUtils.genAgentSid();
        assertNotNull(sid);
        assertEquals(64, sid.length());
        assertTrue(sid.matches("[0-9A-F]+"));
    }

    @Test
    void testGenAgentSid_UniqueValues() {
        String sid1 = ServletUtils.genAgentSid();
        String sid2 = ServletUtils.genAgentSid();
        assertNotNull(sid1);
        assertNotNull(sid2);
        assertTrue(!sid1.equals(sid2));
    }

    @Test
    void testGetAgentSid_FromAttribute() {
        when(request.getAttribute(SimpleConstants.AGENT_SID)).thenReturn("attr-sid-value");
        String result = ServletUtils.getAgentSid(request);
        assertEquals("attr-sid-value", result);
    }

    @Test
    void testGetAgentSid_FromCookie() {
        when(request.getAttribute(SimpleConstants.AGENT_SID)).thenReturn(null);
        Cookie cookie = new Cookie(SimpleConstants.AGENT_SID, "cookie-sid-value");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        String result = ServletUtils.getAgentSid(request);
        assertEquals("cookie-sid-value", result);
    }

    @Test
    void testGetAgentSid_NoCookies() {
        when(request.getAttribute(SimpleConstants.AGENT_SID)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        String result = ServletUtils.getAgentSid(request);
        assertNull(result);
    }

    @Test
    void testGetAgentSid_EmptyCookies() {
        when(request.getAttribute(SimpleConstants.AGENT_SID)).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{});
        String result = ServletUtils.getAgentSid(request);
        assertNull(result);
    }

    @Test
    void testGetAgentSid_CookieNotFound() {
        when(request.getAttribute(SimpleConstants.AGENT_SID)).thenReturn(null);
        Cookie otherCookie = new Cookie("OTHER_COOKIE", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});
        String result = ServletUtils.getAgentSid(request);
        assertNull(result);
    }

    @Test
    void testGetAgentSid_AttributeTakesPrecedence() {
        when(request.getAttribute(SimpleConstants.AGENT_SID)).thenReturn("attr-sid");
        String result = ServletUtils.getAgentSid(request);
        assertEquals("attr-sid", result);
    }

    @Test
    void testSetSidToCookie_SecureEnabled() {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("cookie_security_enable"), anyString()))
            .thenReturn("true");
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("server.ssl.enabled"), anyString()))
            .thenReturn("true");

        ServletUtils.setSidToCookie(response, "test-sid");

        verify(response).addCookie(org.mockito.ArgumentMatchers.argThat(cookie ->
            cookie.getName().equals(SimpleConstants.AGENT_SID)
                && cookie.getValue().equals("test-sid")
                && cookie.getPath().equals("/")
                && cookie.getMaxAge() == Integer.MAX_VALUE
                && cookie.getSecure()
        ));
    }

    @Test
    void testSetSidToCookie_SecureDisabled() {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("cookie_security_enable"), anyString()))
            .thenReturn("false");
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("server.ssl.enabled"), anyString()))
            .thenReturn("true");

        ServletUtils.setSidToCookie(response, "test-sid");

        verify(response).addCookie(org.mockito.ArgumentMatchers.argThat(cookie ->
            cookie.getName().equals(SimpleConstants.AGENT_SID)
                && cookie.getValue().equals("test-sid")
                && !cookie.getSecure()
        ));
    }

    @Test
    void testBuildAgentSidCookie_SecureEnabled() {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("cookie_security_enable"), anyString()))
            .thenReturn("true");
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("server.ssl.enabled"), anyString()))
            .thenReturn("true");

        Cookie cookie = ServletUtils.buildAgentSidCookie("my-sid");

        assertEquals(SimpleConstants.AGENT_SID, cookie.getName());
        assertEquals("my-sid", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(Integer.MAX_VALUE, cookie.getMaxAge());
        assertTrue(cookie.getSecure());
    }

    @Test
    void testBuildAgentSidCookie_SecureDisabled() {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("cookie_security_enable"), anyString()))
            .thenReturn("true");
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("server.ssl.enabled"), anyString()))
            .thenReturn("false");

        Cookie cookie = ServletUtils.buildAgentSidCookie("my-sid");

        assertEquals(SimpleConstants.AGENT_SID, cookie.getName());
        assertEquals("my-sid", cookie.getValue());
        assertEquals("/", cookie.getPath());
        assertEquals(Integer.MAX_VALUE, cookie.getMaxAge());
        assertEquals(false, cookie.getSecure());
    }

    @Test
    void testDeleteSidCookie() {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("cookie_security_enable"), anyString()))
            .thenReturn("false");
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("server.ssl.enabled"), anyString()))
            .thenReturn("false");

        ServletUtils.deleteSidCookie(response);

        verify(response).addCookie(org.mockito.ArgumentMatchers.argThat(cookie ->
            cookie.getName().equals(SimpleConstants.AGENT_SID)
                && cookie.getValue().equals("")
                && cookie.getMaxAge() == 0
                && cookie.getPath().equals("/")
        ));
    }

    @Test
    void testDeleteSidCookie_SecureEnabled() {
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("cookie_security_enable"), anyString()))
            .thenReturn("true");
        springBeanUtilsMock.when(() -> SpringBeanUtils.getProperty(eq("server.ssl.enabled"), anyString()))
            .thenReturn("true");

        ServletUtils.deleteSidCookie(response);

        verify(response).addCookie(org.mockito.ArgumentMatchers.argThat(cookie ->
            cookie.getName().equals(SimpleConstants.AGENT_SID)
                && cookie.getMaxAge() == 0
                && cookie.getSecure()
        ));
    }
}
