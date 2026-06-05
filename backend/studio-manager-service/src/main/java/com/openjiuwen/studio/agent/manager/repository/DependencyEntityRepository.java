/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.DependencyEntityRepo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 功能描述
 *
 */
public interface DependencyEntityRepository extends JpaRepository<DependencyEntityRepo, String> {
    /**
     * @param entity entity
     * @return S
     */
    <S extends DependencyEntityRepo> S save(S entity);

    /**
     *
     * @param projectId
     * @param workspaceId
     * @param name
     * @return
     */
    List<DependencyEntityRepo> findAllByProjectIdAndWorkspaceIdAndName(String projectId, String workspaceId, String name);

    /**
     * findByProjectIdAndWorkspaceIdAndId
     * @param projectId
     * @param workspaceId
     * @param id
     * @return
     */
    DependencyEntityRepo findByProjectIdAndWorkspaceIdAndId(String projectId, String workspaceId, String id);

    /**
     * deleteById
     * @param id must not be {@literal null}.
     */
    void deleteById(String id);
}
