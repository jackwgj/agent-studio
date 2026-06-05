/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 功能描述
 *
 */
@Mapper
public interface McpServerConfigMapper {
    int insert(McpServerConfig config);

    McpServerConfig selectByUniqueIds(@Param("projectId") String projectId, @Param("userId") String userId,
        @Param("serverId") String serverId);

    int deleteByUniqueIds(@Param("projectId") String projectId, @Param("userId") String userId,
        @Param("serverId") String serverId);

    int updateByUniqueIds(@Param("projectId") String projectId, @Param("userId") String userId,
        @Param("serverId") String serverId, @Param("authKeys") String authKeys);

    List<McpServerConfig> selectAll();
}
