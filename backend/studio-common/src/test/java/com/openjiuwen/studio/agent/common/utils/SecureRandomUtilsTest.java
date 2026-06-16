/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.security.SecureRandom;

import org.junit.jupiter.api.Test;

public class SecureRandomUtilsTest {

    @Test
    public void testGetInstance_NotNull() {
        assertNotNull(SecureRandomUtils.getInstance());
    }

    @Test
    public void testGetInstance_Singleton() {
        SecureRandom r1 = SecureRandomUtils.getInstance();
        SecureRandom r2 = SecureRandomUtils.getInstance();
        assertSame(r1, r2);
    }

    @Test
    public void testGetInstance_CanGenerateBytes() {
        byte[] bytes = new byte[16];
        SecureRandomUtils.getInstance().nextBytes(bytes);
        boolean allZero = true;
        for (byte b : bytes) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        assertNotNull(bytes);
    }
}
