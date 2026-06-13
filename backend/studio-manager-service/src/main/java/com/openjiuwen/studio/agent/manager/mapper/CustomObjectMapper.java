/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.CustomObjectEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomObjectMapper {

    CustomObjectEntity findByIdAndProjectIdAndWorkspaceId(
            @Param("id") String id,
            @Param("projectId") String projectId,
            @Param("workspaceId") String workspaceId
    );

    /**
     * 统计name的重复个数
     * @param name
     * @param projectId
     * @param workspaceId
     * @return
     */
    @Select("select count(*) from t_custom_object where name = #{name} and project_id = #{projectId} and workspace_id = #{workspaceId}")
    int countByNameAndProjectIdAndWorkspaceId(
            @Param("name") String name,
            @Param("projectId") String projectId,
            @Param("workspaceId") String workspaceId
    );

    List<CustomObjectEntity> findByProjectIdAndWorkspaceIdAndNameContaining(
            @Param("projectId") String projectId,
            @Param("workspaceId") String workspaceId,
            @Param("name") String name,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countByProjectIdAndWorkspaceIdAndNameContaining(
            @Param("projectId") String projectId,
            @Param("workspaceId") String workspaceId,
            @Param("name") String name
    );

    List<CustomObjectEntity> findByProjectIdAndWorkspaceId(
            @Param("projectId") String projectId,
            @Param("workspaceId") String workspaceId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /**
     * 删除操作
     * @param id
     * @param projectId
     * @param workspaceId
     * @return
     */
    @Delete("delete from t_custom_object where id = #{id} and project_id = #{projectId} and workspace_id = #{workspaceId}")
    int deleteByIdAndProjectIdAndWorkspaceId(
            @Param("id") String id,
            @Param("projectId") String projectId,
            @Param("workspaceId") String workspaceId
    );

    int insert(CustomObjectEntity entity);

    int updateById(CustomObjectEntity entity);

}
