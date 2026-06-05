/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.HistoryReleaseVersionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 已删除的Agent资源表管理
 *
 */
@Repository
public interface HistoryReleaseVersionRepository extends JpaRepository<HistoryReleaseVersionEntity, String> {
    /**
     * 根据appId删除已删除资源版本表
     * @param appId 资源AppId
     * @return int
     */
    int deleteByAppId(String appId);

    /**
     * 查找指定appId和versionId的历史版本
     * @param appId 资源AppId
     * @param versionId 版本Id
     * @return
     */
    Optional<HistoryReleaseVersionEntity> findByAppIdAndVersionId(String appId, String versionId);
}
