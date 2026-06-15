/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import static org.mockito.Mockito.verify;

import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationTask;
import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.PeOptimizationTaskMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * OptimizationTaskTransactionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OptimizationTaskTransactionServiceTest {

    @InjectMocks
    private OptimizationTaskTransactionService optimizationTaskTransactionService;

    @Mock
    private PeOptimizationTaskMapper peOptimizationTaskMapper;

    // ========== createOptimizationTask ==========

    @Test
    void testCreateOptimizationTask_Success() {
        PeOptimizationTask task = new PeOptimizationTask();
        task.setId("opt-001");
        task.setName("优化任务");
        task.setStatus(OptimizationTaskStatus.PENDING);

        optimizationTaskTransactionService.createOptimizationTask(task);

        verify(peOptimizationTaskMapper).insertOneOptimizationTask(task);
    }

    // ========== deleteOptimizationTaskAndResult ==========

    @Test
    void testDeleteOptimizationTaskAndResult_Success() {
        String id = "opt-001";
        String workspaceId = "workspace1";

        optimizationTaskTransactionService.deleteOptimizationTaskAndResult(id, workspaceId);

        verify(peOptimizationTaskMapper).deleteOptimizationTaskById(id, workspaceId);
    }
}
