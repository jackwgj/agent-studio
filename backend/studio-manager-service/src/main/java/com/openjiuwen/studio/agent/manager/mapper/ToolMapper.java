/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;
import com.openjiuwen.studio.agent.manager.entity.ToolEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 工具Mapper
 *
 */
@Mapper
public interface ToolMapper {
    int deleteOnlyByPrimaryKey(@Param("toolId") String toolId);

    int deleteByPrimaryKey(@Param("toolId") String toolId, @Param("projectId") String projectId);

    void copyToHistoryTool(@Param("toolId") String toolId, @Param("projectId") String projectId,
                                 @Param("historyId") String historyId);

    int insert(ToolEntity record);

    int insertPlugin(PluginEntity plugin);

    ToolEntity selectByPrimaryKey(@Param("toolId") String toolId, @Param("projectId") String projectId);

    ToolEntity selectByPrimaryKeyAndWorkspace(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    ToolEntity selectWithoutStatus(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    Integer selectStatus(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    ToolEntity selectByNameAndWorkspaceIdAndProjectId(@Param("toolDisplayName") String toolDisplayName,
        @Param("workspaceId") String workspaceId, @Param("projectId") String projectId);

    ToolEntity selectToolEntityWithCredential(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("userId") String userId);

    int updateByPrimaryKeySelective(ToolEntity record);

    List<ToolEntity> selectByProjectIdAndSearchCriteria(@Param("projectId") String projectId,
        @Param("userId") String userId, @Param("searchCriteria") SearchCriteria searchCriteria,
        @Param("workspaceId") String workspaceId);

    List<ToolEntity> selectPresetPluginByProjectIdAndSearchCriteria(@Param("projectId") String projectId,
        @Param("userId") String userId, @Param("searchCriteria") SearchCriteria searchCriteria,
        @Param("workspaceId") String workspaceId);

    List<ToolEntity> selectAllBySearchCriteria(@Param("projectId") String projectId,
        @Param("opProjectId") String opProjectId, @Param("workspaceId") String workspaceId,
        @Param("userId") String userId, @Param("searchCriteria") SearchCriteria searchCriteria);

    ToolEntity selectByProjectId(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("opProjectId") String opProjectId);

    List<ToolEntity> selectByToolIds(@Param("toolIds") List<String> toolIds, @Param("projectId") String projectId,
        @Param("opProjectId") String opProjectId);

    List<ToolEntity> selectByPrimaryIdAndToolIdsWithSearchCriteria(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("toolIds") List<String> toolIds,
        @Param("searchCriteria") SearchCriteria searchCriteria);

    ToolEntity selectById(@Param("toolId") String toolId);

    List<ToolEntity> selectWithQuoteNums(@Param("projectId") String projectId, @Param("limitNums") Integer limitNums);

    List<ToolEntity> selectByWorkspaceIdAndProjectId(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("toolId") String toolId);

    int updateById(@NotNull @Param("updateValue") ToolEntity updateValue, @Param("toolId") String toolId);

    /**
     * 根据projectId查询发布为自定义节点的插件数量
     *
     * @param projectId projectId
     * @return 发布为自定义节点的插件数量
     */
    int selectCustomizeNodeCountByProjectId(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    /**
     * 更新工具的最后版本ID
     *
     * @param toolId 工具的唯一标识符
     * @param projectId 项目的唯一标识符
     * @param lastVersionId 工具的最后版本ID
     * @return 更新的记录数
     */
    int updateLastVersionId(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("lastVersionId") String lastVersionId);

    /**
     * 更新工具的最后版本ID
     *
     * @param toolId 工具的唯一标识符
     * @param projectId 项目的唯一标识符
     * @param published 是否发布
     */
    void updatePublished(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("published") Integer published);

    List<ToolEntity> selectByTraceIdAndWorkspaceId(@Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId, @Param("traceId") String traceId);

    int selectByDisplayNameAndWorkspaceId(@Param("toolDisplayName") String toolDisplayName,
        @Param("workspaceId") String workspaceId);

    int selectByChineseNameAndWorkspaceId(@Param("toolChineseName") String toolChineseName,
        @Param("workspaceId") String workspaceId);

    int selectByChineseNameAndWorkspaceIdAndProjectId(@Param("toolChineseName") String toolChineseName,
        @Param("workspaceId") String workspaceId, @Param("toolId") String toolId);

    int selectByDisplayNameAndWorkspaceIdAndProjectId(@Param("toolChineseName") String toolChineseName,
        @Param("workspaceId") String workspaceId, @Param("toolId") String toolId);

    List<ToolEntity> selectByToolIdsAndWorkspaceId(@Param("toolIds") List<String> toolIds,
        @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    List<ToolEntity> selectByIds(@Param("toolIds") List<String> toolIds);

    Boolean selectIsInputList(@Param("toolId") String toolId);

    Boolean selectIsOutputList(@Param("toolId") String toolId);

    List<ToolEntity> selectByProjectIdAndSearchCriteriaWithoutTestStatus(@Param("projectId") String projectId,
        @Param("userId") String userId, @Param("searchCriteria") SearchCriteria searchCriteria,
        @Param("workspaceId") String workspaceId);

    ToolEntity selectByProjectIdWithoutStatus(@Param("toolId") String toolId, @Param("projectId") String projectId,
        @Param("opProjectId") String opProjectId);

    void updateIdAndName(@Param("toolId") String toolId, @Param("uuid") String uuid);
}
