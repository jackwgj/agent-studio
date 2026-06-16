/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThreadLocalUtilsTest {

    @AfterEach
    void tearDown() {
        ThreadLocalUtils.clearWorkspaceId();
    }

    @Test
    void testSetAndGetWorkspaceId() {
        ThreadLocalUtils.setWorkspaceId("ws-123");
        assertEquals("ws-123", ThreadLocalUtils.getWorkspaceId());
    }

    @Test
    void testSetAndGetWorkspaceName() {
        ThreadLocalUtils.setWorkspaceName("my-workspace");
        assertEquals("my-workspace", ThreadLocalUtils.getWorkspaceName());
    }

    @Test
    void testGetWorkspaceId_Default() {
        assertEquals("default", ThreadLocalUtils.getWorkspaceId());
    }

    @Test
    void testGetWorkspaceName_Default() {
        assertEquals("defaultName", ThreadLocalUtils.getWorkspaceName());
    }

    @Test
    void testClearWorkspaceId() {
        ThreadLocalUtils.setWorkspaceId("ws-123");
        ThreadLocalUtils.clearWorkspaceId();
        assertEquals("default", ThreadLocalUtils.getWorkspaceId());
    }

    @Test
    void testSetWorkspaceId_Overwrite() {
        ThreadLocalUtils.setWorkspaceId("first");
        ThreadLocalUtils.setWorkspaceId("second");
        assertEquals("second", ThreadLocalUtils.getWorkspaceId());
    }

    @Test
    void testSetWorkspaceName_Overwrite() {
        ThreadLocalUtils.setWorkspaceName("first");
        ThreadLocalUtils.setWorkspaceName("second");
        assertEquals("second", ThreadLocalUtils.getWorkspaceName());
    }
}
