/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.CustomObjectEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomObjectRepository extends JpaRepository<CustomObjectEntity, String> {
    /**
     * 根据id查询自定义对象
     * @param objectId 自定义对象id
     * @param projectId 项目id
     * @param workspaceId 工作空间id
     * @return CustomObjectEntity
     */
    CustomObjectEntity findByIdAndProjectIdAndWorkspaceId(String objectId, String projectId, String workspaceId);

    /**
     * 根据name统计数量
     * @param name 自定义对象名字
     * @param projectId 项目id
     * @param workspaceId 工作空间id
     * @return 数量
     */
    int countByNameAndProjectIdAndWorkspaceId(String name, String projectId, String workspaceId);

    /**
     * 查询自定义对象（按名称搜索）
     * @param projectId 项目id
     * @param workspaceId 工作空间id
     * @param name 对象名字
     * @param limit 限制数量
     * @param offset 偏移量
     * @return List<CustomObjectEntity>
     */
    @Query(value = "SELECT * FROM t_custom_object WHERE project_id = :projectId AND workspace_id = :workspaceId AND name LIKE CONCAT('%', :name, '%') ORDER BY updated_on DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<CustomObjectEntity> findByProjectIdAndWorkspaceIdAndNameContaining(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("name") String name, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 统计自定义对象数量
     * @param projectId 项目id
     * @param workspaceId 工作空间id
     * @param name 对象名字（可选）
     * @return 总数量
     */
    @Query(value = "SELECT COUNT(*) FROM t_custom_object WHERE project_id = :projectId AND workspace_id = :workspaceId" +
                  " AND (:name IS NULL OR name LIKE CONCAT('%', :name, '%'))", nativeQuery = true)
    long countByProjectIdAndWorkspaceIdAndNameContaining(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("name") String name);

    /**
     * 查询自定义对象
     * @param projectId 项目id
     * @param workspaceId 工作空间id
     * @param limit 限制数量
     * @param offset 偏移量
     * @return List<CustomObjectEntity>
     */
    @Query(value = "SELECT * FROM t_custom_object WHERE project_id = :projectId AND workspace_id = :workspaceId ORDER BY updated_on DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<CustomObjectEntity> findByProjectIdAndWorkspaceId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 根据id删除自定义对象
     * @param objectId 自定义对象id
     * @param projectId 项目id
     * @param workspaceId 工作空间id
     * @return int
     */
    int deleteByIdAndProjectIdAndWorkspaceId(String objectId, String projectId, String workspaceId);
}
