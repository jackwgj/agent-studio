/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.mapper;

import com.openjiuwen.studio.prompt.engineering.dto.TaskDto;
import com.openjiuwen.studio.prompt.engineering.entity.PeTask;
import com.openjiuwen.studio.prompt.engineering.entity.PeTaskTagMapping;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 针对表【t_pe_task(工程任务表)】的数据库操作Mapper
 *
 */
@Mapper
public interface PeTaskMapper {
    /**
     * 查询标签所绑定的任务id数据
     *
     * @param tagId 标签id
     * @return 任务id列表
     */
    List<String> queryTaskIdsByTagId(String tagId, String workspaceId);

    /**
     * 创建工程任务
     *
     * @param peTask 工程任务
     */
    void insertOneTask(@Param("peTask") PeTask peTask, @Param("projectId") String projectId,
        @Param("industryId") String industryId);

    /**
     * 更新工程任务
     *
     * @param peTask 工程任务
     */
    void updateOneTask(@Param("peTask") PeTask peTask, @Param("industryId") String industryId);

    /**
     * 关联工程任务和标签
     *
     * @param mappings 关联关系
     */
    void batchInsertMappings(List<PeTaskTagMapping> mappings);

    /**
     * 根据工程任务ID查询工程任务详细信息
     *
     * @param taskIds 工程任务ID列表
     * @return 工程任务
     */
    List<PeTask> queryTaskDetailsByTaskIds(List<String> taskIds, String workspaceId);

    /**
     * 删除工程任务
     *
     * @param id 工程任务ID
     */
    Integer deleteTaskById(String id, String workspaceId);

    /**
     * 根据工程任务ID，删除任务与标签的绑定关系
     *
     * @param taskId 工程任务ID
     */
    void deleteMappingByTaskId(String taskId, String workspaceId);

    /**
     * @param body
     * @param projectId
     * @return
     */
    Boolean isExist(@Param("body") TaskDto body, @Param("projectId") String projectId, String workspaceId);

    /**
     * 判断任务ID是否存在
     *
     * @param id 任务ID
     * @return 存在返回true， 否则不返回
     */
    Boolean isIdExist(String id, String workspaceId);

    String queryStatusById(String id, String serviceName);

    String queryPublishStatusById(String id, String serviceName);

    List<String> queryTagIdsByTaskId(String taskId, String workspaceId);

    String queryTaskNameById(String taskId, String workspaceId);

    void raiseIterationNum(String taskId, String workspaceId);

    int countByIndustryId(String industryId, String workspaceId);

    @Select("select count(1) from t_pe_task where project_id = #{projectId} and workspace_id = #{workspaceId}")
    int countByProjectId(String projectId, String workspaceId);

    String selectModelAuthId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId,
        @Param("modelId") String modelId);
}
