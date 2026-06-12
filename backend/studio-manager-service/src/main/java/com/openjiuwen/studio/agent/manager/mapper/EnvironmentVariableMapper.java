/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.EnvironmentVariableEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface EnvironmentVariableMapper {

    @Select("select * from t_environment_variable_info where project_id = #{projectId} and workspace_id = #{workspaceId} and env_id = #{envId}")
    EnvironmentVariableEntity findByProjectIdAndWorkspaceIdAndEnvId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("envId") String envId);

    @Select("select * from t_environment_variable_info where project_id = #{projectId} and workspace_id = #{workspaceId} and env_id = #{envId} and id = #{id}")
    EnvironmentVariableEntity findByProjectIdAndWorkspaceIdAndEnvIdAndId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId, @Param("envId") String envId, @Param("id") String id);

    @Select("select * from t_environment_variable_info where project_id = #{projectId} and env_id = #{envId}")
    List<EnvironmentVariableEntity> findByProjectIdAndEnvId(@Param("projectId") String projectId, @Param("envId") String envId);

    @Select("select * from t_environment_variable_info where project_id = #{projectId} and workspace_id = #{workspaceId}")
    List<EnvironmentVariableEntity> findByProjectIdAndWorkspaceId(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId);


    List<EnvironmentVariableEntity> findByProjectIdAndWorkspaceIdAndEnvIdIn(String projectId, String workspaceId, List<String> envIds);

    @Update("update t_environment_variable_info " +
            "set env_variable = #{envVariable}, updater_id = #{updaterId}, updatde_on = current_timestamp" +
            "where id = #{id}")
    int updateEvnVariableById(@Param("id") String id, @Param("envVariable") String envVariable, @Param("updaterId") String updaterId);


    int insert(EnvironmentVariableEntity entity);

    @Delete("DELETE FROM t_environment_variable_info WHERE id = #{id}")
    int deleteById(@Param("id") String id);

}
