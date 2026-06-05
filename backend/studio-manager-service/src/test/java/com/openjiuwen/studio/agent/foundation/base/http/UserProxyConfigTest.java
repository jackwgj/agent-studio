/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Set;

class UserProxyConfigTest {

    @Test
    void testDefaultValues() {
        UserProxyConfig config = new UserProxyConfig();

        assertFalse(config.isEnabled());
        assertEquals("http", config.getSchema());
        assertEquals(8080, config.getPort());
        assertNotNull(config.getNoProxy());
        assertTrue(config.getNoProxy().contains("127.0.0.1"));
        assertNull(config.getHost());
        assertNull(config.getUsername());
        assertNull(config.getPassword());
    }

    @Test
    void testSettersAndGetters() {
        UserProxyConfig config = new UserProxyConfig();
        config.setEnabled(true);
        config.setSchema("https");
        config.setHost("proxy.local");
        config.setPort(3128);
        config.setUsername("user");
        config.setPassword("pwd");
        config.setNoProxy(Set.of("localhost", "127.0.0.1"));

        assertTrue(config.isEnabled());
        assertEquals("https", config.getSchema());
        assertEquals("proxy.local", config.getHost());
        assertEquals(3128, config.getPort());
        assertEquals("user", config.getUsername());
        assertEquals("pwd", config.getPassword());
        assertEquals(Set.of("localhost", "127.0.0.1"), config.getNoProxy());
    }
}
