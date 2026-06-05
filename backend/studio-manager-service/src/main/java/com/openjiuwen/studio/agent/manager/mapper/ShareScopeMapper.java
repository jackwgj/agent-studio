/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.ShareScopeEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源共享管理Mapper
 *
 */
@Mapper
public interface ShareScopeMapper {

    /**
     * 插入实体。
     *
     * @param shareScopeEntity 依赖实体对象
     * @return 返回插入的记录数
     */
    int insert(ShareScopeEntity shareScopeEntity);

    /**
     * 批量插入共享范围数据到 t_share_scope 表。
     *
     * @param shareScopeEntityList 要插入的共享范围对象列表
     * @return 受影响的行数
     */
    int batchInsert(@Param("list") List<ShareScopeEntity> shareScopeEntityList);

    /**
     * 根据资源ID列出授权范围。
     *
     * @param resourceId 资源ID
     * @return 返回符合条件的授权范围实体列表
     */
    List<ShareScopeEntity> selectShareScopesByResourceId(@Param("resourceId") String resourceId);

    /**
     * 根据给定的资源ID删除授权实体。
     *
     * @param resourceId 资源ID。
     */
    int deleteByResourceId(@Param("resourceId") String resourceId);

    /**
     * 批量删除
     * @param shareScopeEntityList 实体列表
     * @return 删除个数
     */
    int batchDelete(@Param("resourceId") String resourceId, @Param("list") List<ShareScopeEntity> shareScopeEntityList);

    /**
     * 查询资源共享范围
     * @param resourceId 资源ID
     * @param workspaceId 空间ID
     * @return 共享范围类
     */
    ShareScopeEntity selectShareScopesByResourceIdAndWorkspaceId(@Param("resourceId") String resourceId, @Param("workspaceId") String workspaceId);

    List<ShareScopeEntity> selectShareScopesByResourceIdsAndWorkspaceId(@Param("resourceIds") List<String> resourceId, @Param("workspaceId") String workspaceId);
}
