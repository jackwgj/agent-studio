/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.EnvironmentVariableEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 功能描述
 *
 */
public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariableEntity, String> {
    EnvironmentVariableEntity findByProjectIdAndWorkspaceIdAndEnvId(String projectId, String workspaceId, String envId);

    EnvironmentVariableEntity findByProjectIdAndWorkspaceIdAndEnvIdAndId(String projectId, String workspaceId, String envId, String id);

    List<EnvironmentVariableEntity> findByProjectIdAndEnvId(String projectId, String envId);

    List<EnvironmentVariableEntity> findByProjectIdAndWorkspaceId(String projectId, String workspaceId);

    List<EnvironmentVariableEntity> findByProjectIdAndWorkspaceIdAndEnvIdIn(String projectId, String workspaceId, List<String> envIds);

    @Modifying
    @Query("update EnvironmentVariableEntity env set env.envVariable = :envVariable, env.updaterId = :updaterId, env.updatedOn = current_timestamp WHERE env.id = :id")
    @Transactional
    int updateEvnVariableById(@Param("id") String id, @Param("envVariable") String envVariable, @Param("updaterId") String updaterId);

}
