/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TaskStatusTest {

    @Test
    void testValues() {
        TaskStatus[] values = TaskStatus.values();
        assertEquals(6, values.length);
    }

    @Test
    void testGetValue() {
        assertEquals("INIT", TaskStatus.INIT.getValue());
        assertEquals("RUNNING", TaskStatus.RUNNING.getValue());
        assertEquals("PENDING", TaskStatus.PENDING.getValue());
        assertEquals("COMPLETED", TaskStatus.COMPLETED.getValue());
        assertEquals("FAILED", TaskStatus.FAILED.getValue());
        assertEquals("CANCELLED", TaskStatus.CANCELLED.getValue());
    }

    @Test
    void testFromValue() {
        assertEquals(TaskStatus.INIT, TaskStatus.fromValue("INIT"));
        assertEquals(TaskStatus.RUNNING, TaskStatus.fromValue("RUNNING"));
        assertEquals(TaskStatus.PENDING, TaskStatus.fromValue("PENDING"));
        assertEquals(TaskStatus.COMPLETED, TaskStatus.fromValue("COMPLETED"));
        assertEquals(TaskStatus.FAILED, TaskStatus.fromValue("FAILED"));
        assertEquals(TaskStatus.CANCELLED, TaskStatus.fromValue("CANCELLED"));
    }

    @Test
    void testFromValue_Invalid() {
        assertNull(TaskStatus.fromValue("INVALID"));
    }

    @Test
    void testValueOf() {
        assertEquals(TaskStatus.INIT, TaskStatus.valueOf("INIT"));
        assertEquals(TaskStatus.RUNNING, TaskStatus.valueOf("RUNNING"));
    }
}
