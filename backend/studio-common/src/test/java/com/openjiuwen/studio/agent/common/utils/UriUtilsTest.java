/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class UriUtilsTest {

    @Test
    public void testNormalizeURI_Null() throws Exception {
        assertNull(UriUtils.normalizeURI(null));
    }

    @Test
    public void testNormalizeURI_Empty() throws Exception {
        assertEquals("", UriUtils.normalizeURI(""));
    }

    @Test
    public void testNormalizeURI_SimpleUri() throws Exception {
        String result = UriUtils.normalizeURI("http://example.com/path");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_WithDotDot() throws Exception {
        String result = UriUtils.normalizeURI("http://example.com/a/b/../c");
        assertEquals("http://example.com/a/c", result);
    }

    @Test
    public void testNormalizeURI_WithDot() throws Exception {
        String result = UriUtils.normalizeURI("http://example.com/a/./b");
        assertEquals("http://example.com/a/b", result);
    }

    @Test
    public void testNormalizeURI_InvalidUri() {
        assertThrows(Exception.class, () -> UriUtils.normalizeURI("://invalid"));
    }

    @Test
    public void testNormalizeURI_RelativePath() throws Exception {
        String result = UriUtils.normalizeURI("/a/b/c");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_WithQuery() throws Exception {
        String result = UriUtils.normalizeURI("http://example.com/path?key=value");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_WithFragment() throws Exception {
        String result = UriUtils.normalizeURI("http://example.com/path#section");
        assertNotNull(result);
    }
}
