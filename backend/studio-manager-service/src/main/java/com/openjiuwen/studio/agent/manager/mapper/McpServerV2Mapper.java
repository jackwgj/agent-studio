package com.openjiuwen.studio.agent.manager.mapper;/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

import com.openjiuwen.studio.agent.manager.entity.McpServerEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ws_mcp_server_def 表操作类
 *
 */
@Mapper
public interface McpServerV2Mapper {
    List<McpServerEntity> queryServerWithPage(@Param("offset") int offset, @Param("limit") int limit,
          @Param("tenantId") String tenantId, @Param("name") String name, @Param("language") String language,
                                              @Param("orgType") String orgType, @Param("category") String category);

    Long queryServerWithPageCount(@Param("tenantId") String tenantId, @Param("name") String name, @Param("language") String language, @Param("orgType") String orgType, @Param("category") String category);

    List<McpServerEntity> selectList();

    McpServerEntity selectById(String id);

    List<McpServerEntity> selectList(String serverId, String tenantId, String deptCode);

    int insert(McpServerEntity serverEntity);

    int updateViewTimes(String serverId);

    int updateInstallTimes(String serverId);
}
