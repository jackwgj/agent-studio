/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonUtilsTest {

    @Test
    void testGetResourceContent_NullResourceName() {
        String result = CommonUtils.getResourceContent(null);
        assertEquals("", result);
    }

    @Test
    void testGetResourceContent_EmptyResourceName() {
        String result = CommonUtils.getResourceContent("");
        assertEquals("", result);
    }

    @Test
    void testGetResourceContent_NonExistentResource() {
        String result = CommonUtils.getResourceContent("non-existent-file.txt");
        assertEquals("", result);
    }

    @Test
    void testGetResourceContent_ExistingResource() {
        String result = CommonUtils.getResourceContent("application.properties");
        assertNotNull(result);
    }
}
