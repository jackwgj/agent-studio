/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.HistoryMappingEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 已删除的Agent资源关系表管理
 *
 */
@Repository
public interface HistoryMappingRepository extends JpaRepository<HistoryMappingEntity, String> {
    /**
     * 根据appId删除Mapping资源关系表
     * @param appId 资源AppId
     * @return int
     */
    int deleteByAppId(String appId);
}
