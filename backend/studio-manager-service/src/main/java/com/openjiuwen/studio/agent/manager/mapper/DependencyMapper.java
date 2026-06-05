/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.DependencyEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 依赖包管理Mapper
 *
 */
@Mapper
public interface DependencyMapper {
    /**
     * 根据工作区ID选择计数。
     *
     * @param projectId   项目ID
     * @param workspaceId 工作区ID
     * @param name        名称
     * @return 返回符合条件的记录数
     */
    int selectCountByWorkspaceIdAndName(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("name") String name);

    /**
     * 插入依赖实体。
     *
     * @param dependencyEntity 依赖实体对象
     * @return 返回插入的记录数
     */
    int insert(DependencyEntity dependencyEntity);

    /**
     * 根据项目ID和工作区ID列出依赖项。
     *
     * @param projectId   项目ID
     * @param workspaceId 工作区ID
     * @return 返回符合条件的依赖实体列表
     */
    List<DependencyEntity> listDependenciesByProjectIdAndWorkspaceId(@Param("projectId") String projectId, @Param("opSvcProjectId") String opSvcProjectId, @Param("workspaceId") String workspaceId, @Param("keyword") String keyword);

    /**
     * 根据给定的项目ID、操作服务项目ID、工作区ID和依赖项ID列表，获取对应的依赖实体列表。
     *
     * @param projectId 项目的唯一标识符
     * @param opSvcProjectId 操作服务项目的唯一标识符
     * @param workspaceId 工作区的唯一标识符
     * @param depIds 依赖项的ID列表
     * @return 返回包含符合条件的依赖实体的列表
     */
    @Deprecated
    List<DependencyEntity> listDependenciesByIds(@Param("projectId") String projectId, @Param("opSvcProjectId") String opSvcProjectId, @Param("workspaceId") String workspaceId, @Param("depIds") List<String> depIds);

    /**
     * 根据给定的项目ID、工作区ID和依赖项ID，查询并返回对应的依赖项实体。
     *
     * @param projectId   项目ID，用于标识特定的项目
     * @param workspaceId 工作区ID，用于标识项目中的特定工作区
     * @param id          依赖项ID，用于标识要查询的特定依赖项
     * @return 返回查找到的DependencyEntity对象，如果未找到则返回null
     */
    DependencyEntity selectDependencyEntityById(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("id") String id);

    DependencyEntity selectPresetDependencyEntityById(@Param("id") String id);

    /**
     * 根据ID更新依赖实体。
     *
     * @param dependencyEntity 包含更新信息的依赖实体对象。
     * @return 更新操作影响的记录数。
     */
    int updateDependencyEntity(DependencyEntity dependencyEntity);

    /**
     * 根据给定的ID删除依赖实体。
     *
     * @param id 要删除的依赖实体的唯一标识符。
     * @return 被删除的依赖实体的数量，通常为1，如果未找到则为0。
     */
    int deleteByPrimaryKey(String id);
}
