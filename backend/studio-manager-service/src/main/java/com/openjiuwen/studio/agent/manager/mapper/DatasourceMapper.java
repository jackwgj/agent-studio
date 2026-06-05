/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.DatasourceEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * DatasourceMapper
 *
 */
@Mapper
public interface DatasourceMapper {
    /**
     * 创建DatasourceEntity
     *
     * @param datasourceEntity 创建数据源
     * @return int
     */
    int createDatasourceEntity(@Param("datasourceEntity") DatasourceEntity datasourceEntity);

    /**
     * 根据主键批量删除
     *
     * @param ids ids
     * @param projectId projectId
     * @return int
     */
    int deleteByIds(@Param("ids") List<String> ids, @Param("projectId") String projectId);

    /**
     * 查询DatasourceEntity
     *
     * @param datasourceEntity 筛选信息
     * @return DatasourceEntity
     */
    List<DatasourceEntity> getDatasourceEntity(@Param("datasourceEntity") DatasourceEntity datasourceEntity,
        @Param("order") String order);

    /**
     * 查询DatasourceEntity
     *
     * @param id id
     * @param projectId projectId
     * @return DatasourceEntity
     */
    DatasourceEntity getByPrimaryKey(@Param("id") String id, @Param("projectId") String projectId);

    /**
     * 根据主键、项目ID和工作区ID获取数据源实体。
     *
     * @param id 主键，用于标识数据源的唯一性。
     * @param projectId 项目ID，用于标识数据源所属的项目。
     * @param workspaceId 工作区ID，用于标识数据源所属的工作区。
     * @return 返回匹配的数据源实体，如果未找到则返回null。
     */
    DatasourceEntity getByPrimaryKeyAndWorkspaceId(@Param("id") String id, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    /**
     * 按id查询DatasourceEntity
     *
     * @param ids 数据源id列表
     * @return DatasourceEntity
     */
    List<DatasourceEntity> getByIds(@Param("ids") List<String> ids);

    /**
     * 按id查询DatasourceEntity
     *
     * @param ids 数据源id列表
     * @return DatasourceEntity
     */
    List<DatasourceEntity> getByIdsAndWorkspace(@Param("ids") List<String> ids, @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);

    /**
     * 按名称精确查询DatasourceEntity
     *
     * @param name 数据源名称
     * @return DatasourceEntity
     */
    List<DatasourceEntity> getByName(@Param("name") String name, @Param("projectId") String projectId, @Param("workspaceId") String workspaceId);

    /**
     * 更新datasourceEntity
     *
     * @param datasourceEntity 数据源信息
     * @return 影响的行数
     */
    int updateByPrimaryKey(@Param("datasourceEntity") DatasourceEntity datasourceEntity);
}
