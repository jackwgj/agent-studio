/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.mapper.v2;

import com.openjiuwen.studio.prompt.engineering.dto.ListPromptTasksQo;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PromptTaskMapper {
    // 插入数据
    void insert(PromptTaskEntity promptTaskEntity);

    // 根据主键删除数据
    void deleteByPrimaryKey(@Param("id") String id, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    /**
     * 软删除提示词优化任务
     * @param taskId 任务ID
     * @param projectId 项目ID
     * @param workspaceId 工作空间ID
     */
    void copyToHistory(@Param("taskId") String taskId, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    List<String> findTasksToBeDeleted(LocalDateTime twoYearsAgo);

    void deleteTasksPermanently(List<String> taskIds);

    // 更新数据
    int updateByPrimaryKey(@Param("promptTaskEntity") PromptTaskEntity promptTaskEntity,
        @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    // 通过任务名称查询数据
    PromptTaskEntity selectByTaskNameNotInDraft(@Param("name") String name, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    // 查询所有优化任务名称
    List<String> selectAllOptimizeTaskNames(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    // 查询除了本数据以外所有的优化任务名称
    List<String> selectAllOptimizeTaskNamesExcludeID(@Param("id") String id, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    // 通过任务ID查询数据
    PromptTaskEntity selectByPrimaryKey(@Param("id") String id, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    List<PromptTaskEntity> queryTaskIdsByCondition(@Param("listPromptTasksQo") ListPromptTasksQo listPromptTasksQo,
        @Param("projectId") String projectId, @Param("status") Integer status);

    List<PromptTaskEntity> selectTasksByPrimaryKey(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    void updateStatusByPrimaryKey(@Param("id") String id, @Param("status") int status,
        @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    void updateJobIdStatusByPrimaryKey(@Param("id") String id, @Param("jobId") String jobId,
        @Param("status") int status, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    void updateProgressRateByPrimaryKey(@Param("id") String id, @Param("progressRate") double progressRate,
        @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    void updateMessageByPrimaryKey(@Param("id") String id, @Param("errorMsg") String errorMsg,
        @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    List<PromptTaskEntity> selectByIds(@Param("ids") List<String> ids);

    void clearMessageById(@Param("id") String id, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    String queryProjectIdByModelId(@Param("id") String id);

    String queryPublishStatusByModelId(@Param("id") String id);

    String queryModelTypeByModelId(@Param("id") String id);

    List<PromptTaskEntity> selectByTaskName(@Param("name") String name, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);
}
