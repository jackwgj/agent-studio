/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationTask;
import com.openjiuwen.studio.prompt.engineering.mapper.PeOptimizationTaskMapper;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


/**
 * 优化任务数据库事务封装类
 *
 */
@Component
public class OptimizationTaskTransactionService {
    @Resource
    private PeOptimizationTaskMapper peOptimizationTaskMapper;

    @Transactional
    public void createOptimizationTask(PeOptimizationTask evaluationTask) {
        // 执行优化任务相关操作
        peOptimizationTaskMapper.insertOneOptimizationTask(evaluationTask);
    }

    @Transactional
    public void deleteOptimizationTaskAndResult(String id, String workspaceId) {
        peOptimizationTaskMapper.deleteOptimizationTaskById(id, workspaceId);
    }

}
