/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.entity.PeEvaluationTask;
import com.openjiuwen.studio.prompt.engineering.entity.PeTask;
import com.openjiuwen.studio.prompt.engineering.entity.PeTaskTagMapping;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeOptimizationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeVariableMapper;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Task数据库事务封装类
 *
 */
@Component
public class TaskTransactionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerService.class);

    @Resource
    private PeTaskMapper peTaskMapper;

    @Resource
    private PePromptMapper pePromptMapper;

    @Resource
    private PeVariableMapper peVariableMapper;

    @Resource
    private PeEvaluationTaskMapper peEvaluationTaskMapper;

    @Resource
    private PeOptimizationTaskMapper peOptimizationTaskMapper;

    /**
     * 创建任务事务
     *
     * @param peTask 创建的任务信息
     * @param mappings 任务与标签关联关系
     */
    @Transactional
    public void createTask(PeTask peTask, List<PeTaskTagMapping> mappings, String projectId, String industryId) {
        peTaskMapper.insertOneTask(peTask, projectId, industryId);
        if (!CollectionUtils.isEmpty(mappings)) {
            peTaskMapper.batchInsertMappings(mappings);
        }
    }

    /**
     * 删除任务及任务下资源事务
     *
     * @param taskId 任务id
     */
    @Transactional
    public void deleteTaskAndResource(String taskId, String workspaceId) {
        List<PeEvaluationTask> evaluationTaskList = peEvaluationTaskMapper.queryEvaluationTaskByTaskId(taskId, workspaceId);
        evaluationTaskList.forEach(item -> {
            if (EvaluationTaskStatus.isExecuting(item.getStatus())) {
                throw new AgentStudioException(StudioError.TASK_HAVE_HAVE_RUNNING_EVALUATION_TASK);
            }
        });
        int cnt;
        try {
            List<String> evalTaskIds = evaluationTaskList.stream().map(PeEvaluationTask::getId)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(evalTaskIds)) {
                peEvaluationTaskMapper.deleteEvaluationResultByEvaluationTaskIds(evalTaskIds, workspaceId);
            }
            peEvaluationTaskMapper.deleteByTaskId(taskId, workspaceId);
            peOptimizationTaskMapper.deleteByTaskId(taskId, workspaceId);
            peVariableMapper.deleteAllVariablesByTaskId(taskId, workspaceId);
            pePromptMapper.deleteByTaskId(taskId, workspaceId);
            peTaskMapper.deleteMappingByTaskId(taskId, workspaceId);
            cnt = peTaskMapper.deleteTaskById(taskId, workspaceId);
        } catch (Exception e) {
            LOGGER.error("Failed to delete the task, taskId = {}", taskId, e);
            throw new AgentStudioException(StudioError.FAILED_TO_DELETE_TASK);
        }

        if(cnt < 1) {
            LOGGER.error("Failed to delete the task, taskId = {}", taskId);
            throw new AgentStudioException(StudioError.FAILED_DELETE_TASK, taskId);
        }
    }

    /**
     * 更新任务事务
     *
     * @param peTask 待更新事务相关信息
     * @param mappings 任务与标签关联关系
     */
    @Transactional
    public void updateTask(PeTask peTask, List<PeTaskTagMapping> mappings, String industryId, String workspaceId) {
        peTaskMapper.updateOneTask(peTask, industryId);
        peTaskMapper.deleteMappingByTaskId(peTask.getId(), workspaceId);
        if (!CollectionUtils.isEmpty(mappings)) {
            peTaskMapper.batchInsertMappings(mappings);
        }
    }
}
