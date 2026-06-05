/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


/**
 * 评估任务数据库事务封装类
 *
 */
@Component
public class EvaluationTaskTransactionService {
    @Resource
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @Transactional
    public void createEvaluationTask(PeEvaluationTask evaluationTask) {
        // 执行评估任务相关操作
        peEvaluationTaskMapper.insertOneEvaluationTask(evaluationTask);
    }

    @Transactional
    public void deleteEvaluationTaskAndResult(String id, String workspaceId) {
        peEvaluationTaskMapper.deleteEvaluationResultByEvaluationTaskId(id, workspaceId);
        peEvaluationTaskMapper.deleteEvaluationTaskById(id, workspaceId);
    }

    @Transactional
    public void deleteEvaluationTaskResult(String id, String workspaceId) {
        peEvaluationTaskMapper.deleteEvaluationResultByEvaluationTaskId(id, workspaceId);
    }

    @Transactional
    public Integer modifyEvaluationTaskInfo(String evaluationTaskId, String name, String description, EvaluationTaskStatus status, String workspaceId) {
        return peEvaluationTaskMapper.updateEvaluationTaskInfoById(evaluationTaskId, name, description, status, workspaceId);
    }
}
