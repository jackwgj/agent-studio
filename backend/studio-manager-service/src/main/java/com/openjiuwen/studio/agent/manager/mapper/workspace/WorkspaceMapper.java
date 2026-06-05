/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.workspace;

import com.openjiuwen.studio.agent.manager.dto.WorkspaceInfo;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目空间管理数据层
 *
 */
@Mapper
public interface WorkspaceMapper {

    /**
     * 根据项目ID获取工作空间列表。
     *
     * @param projectId 项目的唯一标识符。
     * @return 与指定项目ID相关联的工作空间实体列表。
     */
    List<WorkspaceEntity> getWorkspaceByProjectId(@Param("projectId") String projectId);

    /**
     * 根据在租户ID获取工作空间列表。
     *
     * @param tenantId 租户的唯一标识符。
     * @return 与指定租户ID相关联的工作空间实体列表。
     */
    List<WorkspaceEntity> getWorkspaceByTenantId(@Param("tenantId") String tenantId);

    /**
     * 根据工作空间ID获取工作空间详情。
     *
     * @param workspaceId 工作空间ID。
     * @return 工作空间详情。
     */
    WorkspaceEntity getWorkspaceByWorkspaceId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    /**
     * 根据项目ID获取工作空间列表。
     *
     * @param projectId 项目的唯一标识符。
     * @param workspaceIds 工作空间id列表。
     * @return 工作空间实体列表。
     */
    List<WorkspaceEntity> getWorkspaceByProjectIdAndWorkspaceIds(@Param("projectId") String projectId,
        @Param("workspaceIds") List<String> workspaceIds, @Param("type") String type);

    /**
     * 根据项目ID和用户获取工作空间列表。
     *
     * @param projectId 项目的唯一标识符。
     * @param creatorId 用户Id。
     * @return 工作空间实体列表。
     */
    List<WorkspaceInfo> getWorkspaceByProjectIdAndUserId(@Param("projectId") String projectId, @Param("creatorId") String creatorId);

    /**
     * 将项目工作空间实体插入数据库中。
     *
     * @param workspaceEntity 项目工作空间实体对象，包含需要插入的数据。
     * @return 插入操作影响的行数，通常用于判断插入是否成功。
     */
    int insert(WorkspaceEntity workspaceEntity);

    /**
     * 根据主键更新agent的部分非空字段
     *
     * @param workspace record
     * @return int
     */
    int updateByPrimaryKeySelective(WorkspaceEntity workspace);

    /**
     * 根据项目ID和用户获取个人工作空间。
     *
     * @param projectId 项目的唯一标识符。
     * @param creatorId 用户Id。
     * @return 工作空间实体列表。
     */
    WorkspaceEntity getPersonalWorkspaceByProjectIdAndUserId(@Param("projectId") String projectId, @Param("creatorId") String creatorId);

    WorkspaceInfo selectById(@Param("projectId") String projectId, @Param("id") String id);

    int countWorkspaceByDomainId(@Param("projectId") String projectId, @Param("domainId") String domainId);

    WorkspaceEntity selectByProjectIdAndDomainIdAndWorkspaceId(@Param("projectId") String projectId, @Param("domainId") String domainId, @Param("workspaceId") String workspaceId);

    List<WorkspaceEntity> selectWorkspaceListByDomainId(@Param("projectId") String projectId, @Param("domainId") String domainId);

    int countWorkspaceEntityByName(@Param("projectId") String projectId, @Param("name") String name);

    WorkspaceEntity selectWorkspaceEntityById(@Param("projectId") String projectId, @Param("id") String id);

    List<WorkspaceInfo> selectWorkspaceWithMappingInfo(@Param("projectId") String projectId, @Param("source") String source);

    List<WorkspaceEntity> selectWorkspaceByWorkspaceIdList(@Param("workspaceIdList") List<String> workspaceIdList);
}
