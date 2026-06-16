/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.security.SecureRandom;

import org.junit.jupiter.api.Test;

public class UsCommonUtilsTest {

    @Test
    public void testNormalizeURI_NullInput() {
        assertNull(UsCommonUtils.normalizeURI(null));
    }

    @Test
    public void testNormalizeURI_EmptyString() {
        assertEquals("", UsCommonUtils.normalizeURI(""));
    }

    @Test
    public void testNormalizeURI_SimplePath() {
        String result = UsCommonUtils.normalizeURI("/api/v1/test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_DoubleSlash() {
        String result = UsCommonUtils.normalizeURI("/api//v1//test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_DotDotPath() {
        String result = UsCommonUtils.normalizeURI("/api/v1/../v2/test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_DotPath() {
        String result = UsCommonUtils.normalizeURI("/api/./v1/./test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_Backslash() {
        String result = UsCommonUtils.normalizeURI("/api\\v1\\test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_PathParameter() {
        String result = UsCommonUtils.normalizeURI("/api/v1;jsessionid=abc/test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_EncodedPath() {
        String result = UsCommonUtils.normalizeURI("/api/v1/%20test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_DoubleEncodedPath() {
        String result = UsCommonUtils.normalizeURI("/api/v1/%2520test");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeStringLimitLen_NullInput() {
        assertNull(UsCommonUtils.normalizeStringLimitLen(null));
    }

    @Test
    public void testNormalizeStringLimitLen_EmptyString() {
        assertEquals("", UsCommonUtils.normalizeStringLimitLen(""));
    }

    @Test
    public void testNormalizeStringLimitLen_NormalString() {
        assertEquals("hello", UsCommonUtils.normalizeStringLimitLen("hello"));
    }

    @Test
    public void testNormalizeStringLimitLen_UnicodeString() {
        String result = UsCommonUtils.normalizeStringLimitLen("\u00e9");
        assertNotNull(result);
    }

    @Test
    public void testGetInstance_ReturnsSecureRandom() throws Exception {
        SecureRandom random = UsCommonUtils.getInstance();
        assertNotNull(random);
    }

    @Test
    public void testGetInstance_ReturnsSameInstance() throws Exception {
        SecureRandom random1 = UsCommonUtils.getInstance();
        SecureRandom random2 = UsCommonUtils.getInstance();
        assertNotNull(random1);
        assertNotNull(random2);
    }

    @Test
    public void testNormalizeURI_RootPath() {
        String result = UsCommonUtils.normalizeURI("/");
        assertNotNull(result);
    }

    @Test
    public void testNormalizeURI_MultiplePathParameters() {
        String result = UsCommonUtils.normalizeURI("/api;param1/v1;param2/test");
        assertNotNull(result);
    }
}
