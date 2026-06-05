/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.entity.McpServerRatingEntity;
import com.openjiuwen.studio.agent.manager.entity.ServerScore;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ws_mcp_service_def 表操作类
 *
 */
public interface McpServerRatingMapper {

    List<ServerScore> queryScoreAllServer();

    List<ServerScore> queryScoreByServerId(@Param("serverId")String serverId);


    ServerScore queryScoreByServerIdAndTenantId(@Param("serverId")String serverId, @Param("tenantId")String tenantId);

    List<ServerScore> queryScoreByServerIds(@Param("serverIds") List<String> serverIds);

    int insertRating(McpServerRatingEntity mcpServerRatingEntity);
}
