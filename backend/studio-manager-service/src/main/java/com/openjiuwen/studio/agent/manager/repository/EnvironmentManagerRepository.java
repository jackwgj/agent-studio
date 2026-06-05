/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.EnvironmentManagerEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 功能描述
 *
 */
public interface EnvironmentManagerRepository extends JpaRepository<EnvironmentManagerEntity, String>,
    JpaSpecificationExecutor<EnvironmentManagerEntity> {
    List<EnvironmentManagerEntity> findByProjectId(String projectId);

    @Query("select env.projectId from EnvironmentManagerEntity env group by env.projectId")
    List<String> findAllWithGroupByProjectId();

    List<EnvironmentManagerEntity> findByProjectIdAndIsDefaultTrue(String projectId);

    List<EnvironmentManagerEntity> findByProjectIdAndIsDefaultFalseAndStatus(String projectId, String status);

    int countByProjectId(String projectId);

    List<EnvironmentManagerEntity> findByNameAndProjectId(String name, String projectId);

    EnvironmentManagerEntity findByIdAndProjectId(String id, String projectId);

    @Modifying
    @Query("update EnvironmentManagerEntity env set env.isDefault = :isDefault, env.updaterId = :updaterId, env.updatedOn = current_timestamp WHERE env.id in :ids")
    @Transactional
    int batchUpdateIsDefaultByIds(@Param("ids") List<String> ids, @Param("isDefault") boolean isDefault, @Param("updaterId") String updaterId);

    @Modifying
    @Query("update EnvironmentManagerEntity env set env.isDefault = :isDefault, env.updaterId = :updaterId, env.updatedOn = current_timestamp WHERE env.id = :id")
    @Transactional
    int updateIsDefaultById(@Param("id") String id, @Param("isDefault") boolean isDefault, @Param("updaterId") String updaterId);

    @Modifying
    @Query("update EnvironmentManagerEntity env set env.status = :status, env.updaterId = case when :updaterId is not null then :updaterId else env.updaterId end, env.updatedOn = current_timestamp WHERE env.id = :id")
    @Transactional
    int updateStatusById(@Param("id") String id, @Param("status") String status, @Param("updaterId") String updaterId);

    @Modifying
    @Query("update EnvironmentManagerEntity env set env.status = :status, env.isDefault = :isDefault, env.updaterId = :updaterId, env.updatedOn = current_timestamp WHERE env.id = :id")
    @Transactional
    void updateStatusAndIsDefaultById(@Param("id") String id, @Param("status") String status, @Param("updaterId") String updaterId, @Param("isDefault") boolean isDefault);

}
