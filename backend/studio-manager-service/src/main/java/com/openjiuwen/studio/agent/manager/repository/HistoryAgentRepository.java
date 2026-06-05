/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.repository;

import com.openjiuwen.studio.agent.manager.entity.HistoryAgentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 已删除的Agent资源表管理
 *
 */
@Repository
public interface HistoryAgentRepository extends JpaRepository<HistoryAgentEntity, String> {

    /**
     * 查询更新时间小于某个时间点的数据
     * @param time 时间点
     * @return 符合条件的实体列表
     */
    List<HistoryAgentEntity> findByUpdatedOnLessThan(Date time);
}
