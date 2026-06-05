/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper.workspace;

import com.openjiuwen.studio.agent.manager.entity.workspace.WorkspaceMappingEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkspaceMappingMapper {

    void insert(WorkspaceMappingEntity workspaceMappingEntity);

    WorkspaceMappingEntity selectWorkspaceMappingEntityByWorkspaceIdAndSource(@Param("workspaceId") String workspaceId, @Param("source") String source);

    List<WorkspaceMappingEntity> selectWorkspaceMappingEntityByWorkspaceId(@Param("workspaceId") String workspaceId);

    void updateByPrimaryKey(WorkspaceMappingEntity workspaceMappingEntity);

    void deleteWorkspaceMappingEntityByWorkspaceId(@Param("workspaceId") String workspaceId, @Param("source") String source);

}
