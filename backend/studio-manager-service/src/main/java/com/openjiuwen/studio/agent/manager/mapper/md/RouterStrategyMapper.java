/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper.md;

import com.openjiuwen.studio.agent.common.entity.RouterStrategyEntity;
import com.openjiuwen.studio.agent.manager.entity.md.RouterStrategyEntityCondition;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RouterStrategyMapper {

    void insert(@Param("entity") RouterStrategyEntity entity);

    void deleteById(@Param("id") String id);

    int selectTotalSizeByQueryParameter(@Param("queryParameter") RouterStrategyEntityCondition queryParameter);

    List<RouterStrategyEntity> selectInfoByQueryParameter(@Param("queryParameter")RouterStrategyEntityCondition queryParameter);

    RouterStrategyEntity selectInfoById(@Param("id") String id);

    void update(@Param("entity") RouterStrategyEntity entity);

    List<RouterStrategyEntity> selectInfoByStrategyName(@Param("project_id") String projectId,
        @Param("workspace_id") String workspaceId, @Param("strategy_name") String strategyName);

    /**
     * 数据备份
     */
    void insertBackup(@Param("entity") RouterStrategyEntity entity);

    List<RouterStrategyEntity> queryBackupByUpdateTime(@Param("lastUpdatedDate") Long lastUpdatedDate);

    void deleteBackupByIds(@Param("ids") List<String> ids);

    List<RouterStrategyEntity> queryStrategiesByModelId(@Param("project_id") String projectId, @Param("workspace_id") String workspaceId,
        @Param("model_id") String modelId);

    List<RouterStrategyEntity> queryByIds(@Param("ids") List<String> ids,
        @Param("projectId") String projectId,
        @Param("workspaceId") String workspaceId);
    List<RouterStrategyEntity> queryByRouterIds(@Param("ids") List<String> ids);
}
