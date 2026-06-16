/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UrlCheckUtilsTest {

    private UrlCheckUtils urlCheckUtils;

    @BeforeEach
    void setUp() {
        urlCheckUtils = new UrlCheckUtils();
        ReflectionTestUtils.setField(urlCheckUtils, "enableUrlCheck", true);
        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlBlackListStr", "blocked.com,10.0.0.1");
        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlWhiteListStr", "allowed.com,192.168.1.1");
        ReflectionTestUtils.invokeMethod(urlCheckUtils, "init");
    }

    @Test
    void testCheckUrl_DisabledCheck() {
        ReflectionTestUtils.setField(urlCheckUtils, "enableUrlCheck", false);
        ReflectionTestUtils.invokeMethod(urlCheckUtils, "init");
        assertDoesNotThrow(() -> urlCheckUtils.checkUrl("project1", "http://any-url.com"));
    }

    @Test
    void testCheckUrl_EmptyUrl() {
        assertDoesNotThrow(() -> urlCheckUtils.checkUrl("project1", ""));
    }

    @Test
    void testCheckUrl_NullUrl() {
        assertDoesNotThrow(() -> urlCheckUtils.checkUrl("project1", null));
    }

    @Test
    void testCheckUrl_UrlWithPlaceholder() {
        assertDoesNotThrow(() -> urlCheckUtils.checkUrl("project1", "http://example.com/{id}"));
    }

    @Test
    void testCheckUrl_InvalidUrlSyntax() {
        assertThrows(AgentStudioException.class, () -> urlCheckUtils.checkUrl("project1", "://invalid"));
    }

    @Test
    void testCheckUrl_BlacklistedHost() {
        assertThrows(AgentStudioException.class, () -> urlCheckUtils.checkUrl("project1", "http://blocked.com/path"));
    }

    @Test
    void testCheckUrl_WhitelistedHost() {
        assertDoesNotThrow(() -> urlCheckUtils.checkUrl("project1", "http://allowed.com/path"));
    }

    @Test
    void testCheckUrlInWhiteList_DisabledCheck() {
        ReflectionTestUtils.setField(urlCheckUtils, "enableUrlCheck", false);
        assertDoesNotThrow(() -> urlCheckUtils.checkUrlInWhiteList("http://any-url.com"));
    }

    @Test
    void testCheckUrlInWhiteList_EmptyUrl() {
        assertDoesNotThrow(() -> urlCheckUtils.checkUrlInWhiteList(""));
    }

    @Test
    void testCheckUrlInWhiteList_InvalidUrlSyntax() {
        assertThrows(AgentStudioException.class, () -> urlCheckUtils.checkUrlInWhiteList("://invalid"));
    }

    @Test
    void testCheckUrlInWhiteList_WhitelistedHost() {
        assertDoesNotThrow(() -> urlCheckUtils.checkUrlInWhiteList("http://allowed.com/path"));
    }

    @Test
    void testCheckUrlInWhiteList_NonWhitelistedHost() {
        assertThrows(AgentStudioException.class, () -> urlCheckUtils.checkUrlInWhiteList("http://unknown-host-xyz123.com/path"));
    }

    @Test
    void testIsInternalIp_LoopbackAddress() throws Exception {
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        assertTrue(urlCheckUtils.isInternalIp(loopback, "project1"));
    }

    @Test
    void testIsInternalIp_SiteLocalAddress() throws Exception {
        InetAddress siteLocal = InetAddress.getByName("192.168.1.100");
        assertTrue(urlCheckUtils.isInternalIp(siteLocal, "project1"));
    }

    @Test
    void testIsInternalIp_LinkLocalAddress() throws Exception {
        InetAddress linkLocal = InetAddress.getByName("169.254.1.1");
        assertTrue(urlCheckUtils.isInternalIp(linkLocal, "project1"));
    }

    @Test
    void testIsInternalIp_AnyLocalAddress() throws Exception {
        InetAddress anyLocal = InetAddress.getByName("0.0.0.0");
        assertTrue(urlCheckUtils.isInternalIp(anyLocal, "project1"));
    }

    @Test
    void testCheckUrl_BlankBlackListConfig() {
        UrlCheckUtils utils = new UrlCheckUtils();
        ReflectionTestUtils.setField(utils, "enableUrlCheck", true);
        ReflectionTestUtils.setField(utils, "toolUrlBlackListStr", "");
        ReflectionTestUtils.setField(utils, "toolUrlWhiteListStr", "");
        ReflectionTestUtils.invokeMethod(utils, "init");
        assertDoesNotThrow(() -> utils.checkUrl("project1", "http://example.com"));
    }

    @Test
    void testCheckUrl_NullBlackListConfig() {
        UrlCheckUtils utils = new UrlCheckUtils();
        ReflectionTestUtils.setField(utils, "enableUrlCheck", true);
        ReflectionTestUtils.setField(utils, "toolUrlBlackListStr", null);
        ReflectionTestUtils.setField(utils, "toolUrlWhiteListStr", null);
        ReflectionTestUtils.invokeMethod(utils, "init");
        assertDoesNotThrow(() -> utils.checkUrl("project1", "http://example.com"));
    }
}
