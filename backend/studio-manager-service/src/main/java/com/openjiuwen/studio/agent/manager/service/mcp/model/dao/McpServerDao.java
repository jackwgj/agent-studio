/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.dao;

import com.openjiuwen.studio.agent.manager.entity.McpServerEntity;
import com.openjiuwen.studio.agent.manager.mapper.McpServerV2Mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * NodeDao
 *
 * @Date: 2024/12/19 15:59
 * @Description:
 */

@Component
public class McpServerDao {
    private final McpServerV2Mapper mcpServerMapper;

    @Autowired
    public McpServerDao(McpServerV2Mapper mcpServerMapper) {
        this.mcpServerMapper = mcpServerMapper;
    }

    public List<McpServerEntity> listMineMcpServerEntities(String serverId, String tenantId, String deptCode) {
        return mcpServerMapper.selectList(serverId, tenantId, deptCode);
    }


    public List<McpServerEntity> selectServerList() {
        return mcpServerMapper.selectList();
    }

    public McpServerEntity selectById(String id) {
        return mcpServerMapper.selectById(id);
    }

    public int updateViewTimes(String serverId) {
        return mcpServerMapper.updateViewTimes(serverId);
    }

    public int insert(McpServerEntity serverEntity) {
        return mcpServerMapper.insert(serverEntity);
    }

    public int updateInstallTimes(String serverId) {
        return mcpServerMapper.updateInstallTimes(serverId);
    }

    public List<McpServerEntity> queryServerWithPage(int offset, int limit, String tenantId, String name, String language, String orgType, String category) {
        if (limit > 100) {
            limit = 10;
        }
        return mcpServerMapper.queryServerWithPage(offset, limit, tenantId, name, language, orgType, category);
    }

    public Long queryServerWithPageCount(String tenantId, String name, String language, String orgType, String catogory) {
        return mcpServerMapper.queryServerWithPageCount(tenantId, name, language, orgType, catogory);
    }
}
