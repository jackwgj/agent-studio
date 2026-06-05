/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.dao;

import com.openjiuwen.studio.agent.manager.entity.McpServerRatingEntity;
import com.openjiuwen.studio.agent.manager.entity.ServerScore;
import com.openjiuwen.studio.agent.manager.mapper.McpServerRatingMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NodeDao
 *
 * @Date: 2024/12/19 15:59
 * @Description: mcp服务评分
 */

@Component
public class McpServerRatingDao {
    private final McpServerRatingMapper mcpServerRatingMapper;

    @Autowired
    public McpServerRatingDao(McpServerRatingMapper mcpServerRatingMapper) {
        this.mcpServerRatingMapper = mcpServerRatingMapper;
    }

    public int insert(McpServerRatingEntity mcpServerRatingEntity) {
        return mcpServerRatingMapper.insertRating(mcpServerRatingEntity);
    }

    public List<ServerScore> queryScoreAllServer() {
        return mcpServerRatingMapper.queryScoreAllServer();
    }

    public List<ServerScore> queryScoreByServerId(String serverId) {
        return mcpServerRatingMapper.queryScoreByServerId(serverId);
    }

    public ServerScore queryScoreByServerIdAndTenantId(String serverId, String tenantId) {
        return mcpServerRatingMapper.queryScoreByServerIdAndTenantId(serverId, tenantId);
    }

    public List<ServerScore> queryScoreByServerIds(List<String> serverIds) {
        return mcpServerRatingMapper.queryScoreByServerIds(serverIds);
    }
}
