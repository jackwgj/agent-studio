/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.dto.QueryCondition;
import com.openjiuwen.studio.prompt.engineering.dto.TaskConfirmVo;
import com.openjiuwen.studio.prompt.engineering.dto.TaskDto;
import com.openjiuwen.studio.prompt.engineering.dto.TaskVo;
import com.openjiuwen.studio.prompt.engineering.entity.PeTask;
import com.openjiuwen.studio.prompt.engineering.entity.PeTaskTagMapping;
import com.openjiuwen.studio.prompt.engineering.enums.EvaluationTaskStatus;
import com.openjiuwen.studio.prompt.engineering.enums.SourceType;
import com.openjiuwen.studio.prompt.engineering.mapper.PeEvaluationTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.PeVariableMapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

/**
 * TaskManager service
 *
 */
@Component
@Slf4j
public class TaskManagerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerService.class);

    @Resource
    private TaskTransactionService taskTransactionService;

    @Resource
    private PeTaskMapper peTaskMapper;

    @Resource
    private PePromptMapper pePromptMapper;

    @Resource
    private PeEvaluationTaskMapper evaluationTaskMapper;

    @Resource
    private PeVariableMapper peVariableMapper;

    @Value("${agentBuilder.task.quota:500}")
    private Integer taskQuota;

    /**
     * 创建工程任务
     *
     * @param projectId projectId
     * @param body body 请求体
     * @return 创建成功的返回体
     */
    public TaskVo createTask(String projectId, String workspaceId, TaskDto body) {
        if (Boolean.TRUE.equals(peTaskMapper.isExist(body, projectId, workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_NAME_IS_EXIST);
        }
        int taskNum = peTaskMapper.countByProjectId(projectId, workspaceId);
        if (taskNum >= taskQuota) {
            throw new AgentStudioException(StudioError.TASK_EXCEEDS_QUOTA, List.of(taskQuota.toString()));
        }
        try {
            String id = UUID.randomUUID().toString();
            String creator = RequestContextUtils.getRequestUserName();
            PeTask task = PeTask.builder()
                .id(id)
                .name(body.getName())
                .creator(creator)
                .updater(creator)
                .description(body.getDescription())
                .workspaceId(workspaceId)
                .build();
            List<PeTaskTagMapping> mappings = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(body.getTagIds())) {
                body.getTagIds()
                    .forEach(tagId -> mappings.add(PeTaskTagMapping.builder().taskId(id).tagId(tagId).workspaceId(workspaceId).build()));
            }
            taskTransactionService.createTask(task, mappings, projectId, body.getIndustryId());
            return peTaskMapper.queryTaskDetailsByTaskIds(Collections.singletonList(id), workspaceId).get(0).convertToTaskVo();
        } catch (Exception e) {
            LOGGER.error("Failed to create task, task name = {}", body.getName(), e);
            throw new AgentStudioException(StudioError.FAILED_TO_CREATE_TASK, body.getName());
        }
    }

    /**
     * 删除工程任务
     *
     * @param taskId taskId 要删除的工程任务id
     * @return “OK” OR “Method not allowed”
     */

    public String deleteTask(String projectId, String taskId, String workspaceId) {
        taskTransactionService.deleteTaskAndResource(taskId, workspaceId);
        return HttpStatus.OK.getReasonPhrase();
    }

    /**
     * 查询工程任务列表
     *
     * @param listTasksQo listTasksQo 筛选条件
     * @return 工程任务列表
     */


    /**
     * 更新工程任务信息
     *
     * @param taskId taskId 要更新的工程任务的id
     * @param body body 工程任务新的信息
     * @return 更新成功的返回体
     */

    public TaskVo updateTask(String projectId, String taskId, String workspaceId, TaskDto body) {
        if (!Boolean.TRUE.equals(peTaskMapper.isIdExist(taskId, workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_ID_IS_NOT_EXIST, taskId);
        }
        if (Boolean.TRUE.equals(peTaskMapper.isExist(body.setId(taskId), projectId, workspaceId))) {
            throw new AgentStudioException(StudioError.TASK_NAME_IS_EXIST);
        }
        try {
            String updater = RequestContextUtils.getRequestUserName();
            PeTask peTask = PeTask.builder()
                .id(taskId)
                .name(body.getName())
                .description(body.getDescription())
                .updater(updater)
                .workspaceId(workspaceId)
                .build();
            List<PeTaskTagMapping> mappings = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(body.getTagIds())) {
                body.getTagIds()
                    .forEach(tagId -> mappings.add(PeTaskTagMapping.builder().taskId(taskId).tagId(tagId).workspaceId(workspaceId).build()));
            }
            taskTransactionService.updateTask(peTask, mappings, body.getIndustryId(), workspaceId);
            return peTaskMapper.queryTaskDetailsByTaskIds(Collections.singletonList(taskId), workspaceId).get(0).convertToTaskVo();
        } catch (Exception e) {
            LOGGER.error("Failed to update task, taskId = {}", taskId, e);
            throw new AgentStudioException(StudioError.FAILED_TO_UPDATE_TASK, taskId);
        }
    }

    /**
     * 校验工程任务是否能删除
     *
     * @param taskId taskId
     * @return Integer
     */

    public TaskConfirmVo confirmTask(String projectId, String taskId, String workspaceId) {
        TaskConfirmVo confirmVo = new TaskConfirmVo();
        List<EvaluationTaskStatus> taskStatuses = evaluationTaskMapper.queryEvaluationTaskStatusByTaskId(taskId, workspaceId);
        long unDeletedNum = taskStatuses.stream().filter(EvaluationTaskStatus::isExecuting).count();
        confirmVo.setEvalTaskNum(taskStatuses.size());
        confirmVo.setExecutingEvalTaskNum((int) unDeletedNum);
        QueryCondition condition = new QueryCondition();
        condition.setTaskId(taskId);
        condition.setType(SourceType.CANDIDATE.getType());
        confirmVo.setPromptNum(pePromptMapper.queryByCondition(condition, workspaceId).size());
        condition.setType(SourceType.HISTORY.getType());
        confirmVo.setHistoryPromptNum(pePromptMapper.queryByCondition(condition, workspaceId).size());
        confirmVo.setVariableNum(peVariableMapper.queryVariablesByTaskId(taskId, workspaceId).size());
        return confirmVo;
    }

}
