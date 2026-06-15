/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * OptimizationTaskStatus 枚举单元测试
 */
class OptimizationTaskStatusTest {

    // ========== 枚举值校验 ==========

    @Test
    void testStatus_Pending() {
        assertEquals("pending", OptimizationTaskStatus.PENDING.getCode());
    }

    @Test
    void testStatus_Running() {
        assertEquals("running", OptimizationTaskStatus.RUNNING.getCode());
    }

    @Test
    void testStatus_Paused() {
        assertEquals("paused", OptimizationTaskStatus.PAUSED.getCode());
    }

    @Test
    void testStatus_Success() {
        assertEquals("success", OptimizationTaskStatus.SUCCESS.getCode());
    }

    @Test
    void testStatus_Failed() {
        assertEquals("failed", OptimizationTaskStatus.FAILED.getCode());
    }

    // ========== executing() ==========

    @Test
    void testExecuting_OnlyRunning() {
        Set<OptimizationTaskStatus> executing = OptimizationTaskStatus.executing();
        assertEquals(1, executing.size());
        assertTrue(executing.contains(OptimizationTaskStatus.RUNNING));
    }

    // ========== getByCode ==========

    @Test
    void testGetByCode_Success() {
        assertEquals(OptimizationTaskStatus.PENDING, OptimizationTaskStatus.getByCode("pending"));
        assertEquals(OptimizationTaskStatus.RUNNING, OptimizationTaskStatus.getByCode("running"));
        assertEquals(OptimizationTaskStatus.PAUSED, OptimizationTaskStatus.getByCode("paused"));
        assertEquals(OptimizationTaskStatus.SUCCESS, OptimizationTaskStatus.getByCode("success"));
        assertEquals(OptimizationTaskStatus.FAILED, OptimizationTaskStatus.getByCode("failed"));
    }

    @Test
    void testGetByCode_NotExist_Throws() {
        AgentStudioException exception = assertThrows(AgentStudioException.class,
            () -> OptimizationTaskStatus.getByCode("invalid_status"));
        assertEquals(StudioError.OPTIMIZATION_TASK_STATUS_IS_NOT_EXIST, exception.getErrorCode());
    }

    // ========== values() / valueOf() ==========

    @Test
    void testValues_Count() {
        OptimizationTaskStatus[] values = OptimizationTaskStatus.values();
        assertEquals(5, values.length);
    }

    @Test
    void testValueOf() {
        assertEquals(OptimizationTaskStatus.PENDING, OptimizationTaskStatus.valueOf("PENDING"));
        assertEquals(OptimizationTaskStatus.RUNNING, OptimizationTaskStatus.valueOf("RUNNING"));
        assertEquals(OptimizationTaskStatus.PAUSED, OptimizationTaskStatus.valueOf("PAUSED"));
        assertEquals(OptimizationTaskStatus.SUCCESS, OptimizationTaskStatus.valueOf("SUCCESS"));
        assertEquals(OptimizationTaskStatus.FAILED, OptimizationTaskStatus.valueOf("FAILED"));
    }
}
