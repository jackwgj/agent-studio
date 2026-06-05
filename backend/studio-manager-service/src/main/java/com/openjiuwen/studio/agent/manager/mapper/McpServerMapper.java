/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper;

import com.openjiuwen.studio.agent.manager.dto.ListMcpServersQo;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 功能描述
 *
 */
@Mapper
public interface McpServerMapper {
    int insert(McpServerInfo record);

    McpServerInfo selectByPrimaryKey(@Param("projectId") String projectId, @Param("serverId") String serverId,
        @Param("userId") String userId);

    List<McpServerInfo> selectBySearchCriteria(@Param("projectId") String projectId, @Param("userId") String userId,
        ListMcpServersQo searchCriteria);

    int deleteByPrimaryKey(@Param("projectId") String projectId, @Param("serverId") String serverId,
        @Param("userId") String userId);

    int updateById(@Param("projectId") String projectId, @Param("serverId") String serverId,
        @Param("userId") String userId, McpServerInfo updateValue);

    List<McpServerInfo> selectByMcpServerIds(@Param("projectId") String projectId, @Param("workspaceId") String workspaceId,
        @Param("mcpServerIds") List<String> mcpServerIds);

    McpServerInfo selectById(@Param("serverId") String serverId);

    List<McpServerInfo> selectByProjectIdAndSearchCriteria(@Param("projectId") String projectId,
        @Param("searchCriteria") SearchCriteria searchCriteria);

    List<McpServerInfo> selectAll();
}
