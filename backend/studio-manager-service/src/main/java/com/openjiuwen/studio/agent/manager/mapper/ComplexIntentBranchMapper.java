/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.ComplexIntentBranchEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Complex Intent Branch
 *
 */
@Mapper
public interface ComplexIntentBranchMapper {
    /**
     * 新增
     *
     * @param entity 数据
     * @return 影响的行数
     */
    int createEntity(@Param("entity") ComplexIntentBranchEntity entity);

    /**
     * 删除
     *
     * @param branchId 分支id
     * @param intentId 意图id
     * @param projectId 项目id
     * @return 影响的行数
     */
    int deleteByKey(@Param("branchId") String branchId, @Param("intentId") String intentId,
        @Param("projectId") String projectId);

    /**
     * 更新
     *
     * @param entity
     * @return 影响的行数
     */

    int updateByKey(@Param("entity") ComplexIntentBranchEntity entity);

    /**
     * 查询
     *
     * @param branchId
     * @param intentId
     * @param projectId
     * @return 单个数据
     */
    ComplexIntentBranchEntity getByKey(@Param("branchId") String branchId, @Param("intentId") String intentId,
        @Param("projectId") String projectId);

    /**
     * 根据提供的键和工作空间ID获取ComplexIntentBranchEntity对象。
     *
     * @param branchId 分支ID，用于标识特定的分支
     * @param intentId 意图ID，用于标识特定的意图
     * @param projectId 项目ID，用于标识特定的项目
     * @param workspaceId 工作空间ID，用于标识特定的工作空间
     * @return 返回匹配的ComplexIntentBranchEntity对象
     */
    ComplexIntentBranchEntity getByKeyAndWorkspaceId(@Param("branchId") String branchId, @Param("intentId") String intentId,
        @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    /**
     * 查询列表
     *
     * @param entity
     * @return 数据列表
     */
    List<ComplexIntentBranchEntity> getEntities(@Param("entity") ComplexIntentBranchEntity entity);

    /**
     * 获取对打分支索引
     *
     * @param intentId
     * @param projectId
     * @return 最大值
     */
    Integer maxBranchIndex(@Param("intentId") String intentId, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    /**
     * 获取分支个数
     *
     * @param intentId
     * @param projectId
     * @return 个数
     */
    int countBranch(@Param("intentId") String intentId, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    /**
     * 清理分支的faq ids
     *
     * @param entity
     * @return
     */
    int clearFaqIds(@Param("entity") ComplexIntentBranchEntity entity);

    /**
     * 删除intent id下所有分支
     *
     * @param intentId 意图id
     * @param projectId 项目id
     * @return 影响的行数
     */
    int deleteByIntentId(@Param("intentId") String intentId, @Param("projectId") String projectId);

    List<ComplexIntentBranchEntity> getEntitiesAccurate(@Param("entity") ComplexIntentBranchEntity entity);
}
