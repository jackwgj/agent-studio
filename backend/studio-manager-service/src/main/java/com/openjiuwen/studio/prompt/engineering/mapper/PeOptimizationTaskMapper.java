/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationResult;
import com.openjiuwen.studio.prompt.engineering.entity.PeOptimizationTask;
import com.openjiuwen.studio.prompt.engineering.enums.OptimizationTaskStatus;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
 * 针对表【t_pe_evaluation_task】的数据库操作Mapper
 *
 */
@Mapper
public interface PeOptimizationTaskMapper {
    Boolean isNameExist(String taskId, String name, String workspaceId);

    void insertOneOptimizationTask(PeOptimizationTask evaluationTask);

    Boolean isOptimizationTaskExist(String id, String workspaceId);

    void deleteOptimizationTaskById(String id, String workspaceId);

    Integer deleteByTaskId(String taskId, String workspaceId);

    List<PeOptimizationTask> queryOptimizationTaskDetailsById(String id, String workspaceId);

    List<PeOptimizationTask> queryOptimizationTaskDetailsByIds(List<String> ids);

    Integer updateOptimizationTaskResultById(PeOptimizationResult peOptimizationResult);

    void updateOptimizationTaskRuningStatusById(String id, OptimizationTaskStatus status, String workspaceId);

    void updateOptimizationTaskById(String id, OptimizationTaskStatus status, String jiuwenTaskId, String workspaceId);

    int countByTaskIdAndStatus(String taskId, List<OptimizationTaskStatus> statusList, String workspaceId);

    PeOptimizationTask getOptimizationTask(String projectId, String taskId, Set<OptimizationTaskStatus> statusList, String workspaceId);

    List<PeOptimizationTask> queryOptimizationTaskListByStatus(OptimizationTaskStatus runningStatus);

    int modifyOptimizationTaskInfo(String id, String name, String description, OptimizationTaskStatus status, String workspaceId);
}
