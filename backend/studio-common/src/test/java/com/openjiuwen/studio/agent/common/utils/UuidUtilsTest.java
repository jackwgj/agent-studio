/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UuidUtilsTest {

    @Test
    public void testGetUUID_NotNull() {
        assertNotNull(UuidUtils.getUUID());
    }

    @Test
    public void testGetUUID_Length32() {
        assertEquals(32, UuidUtils.getUUID().length());
    }

    @Test
    public void testGetUUID_NoHyphens() {
        assertFalse(UuidUtils.getUUID().contains("-"));
    }

    @Test
    public void testGetUUID_Unique() {
        String uuid1 = UuidUtils.getUUID();
        String uuid2 = UuidUtils.getUUID();
        assertFalse(uuid1.equals(uuid2));
    }

    @Test
    public void testGetUUID_ValidHexChars() {
        String uuid = UuidUtils.getUUID();
        assertTrue(uuid.matches("[0-9a-f]+"));
    }

    private void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }
}
