/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.mapper;

import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 分析事件操作类
 *
 */
@Mapper
public interface AnalyticsEventMapper {
    /**
     * 插入单条数据
     *
     * @param entity 事件
     * @return
     */
    int createEntity(@Param("entity") AnalyticsEventEntity entity);

    /**
     * 批量插入
     *
     * @param entities 事件
     * @return
     */
    int insertBatch(@Param("entities") List<AnalyticsEventEntity> entities);

    /**
     * 查询事件列表
     *
     * @param entity 过滤条件
     * @return
     */
    List<AnalyticsEventEntity> getEntity(@Param("entity") AnalyticsEventEntity entity);

    /**
     * 基于主键查询
     *
     * @param eventId 事件id
     * @param projectId 项目id
     * @return
     */
    AnalyticsEventEntity getByPrimaryKey(@Param("eventId") String eventId, @Param("projectId") String projectId);

    /**
     * 基于主键删除
     *
     * @param eventId 事件id
     * @param projectId 项目id
     * @return
     */
    int deleteByPrimaryKey(@Param("eventId") String eventId, @Param("projectId") String projectId);

    /**
     * 老化历史时间
     *
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return
     */
    int clear(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 查找是否存在历史事件
     *
     * @param entity 用户信息
     * @return 事件id
     */
    String findFirstUserEvent(@Param("entity") AnalyticsEventEntity entity);

    /**
     * 更新为首次用户事件
     *
     * @param entity 用户信息
     * @return 更新的行数
     */
    int updateFirstUserEvent(@Param("entity") AnalyticsEventEntity entity);
}
