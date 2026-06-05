/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.local;

import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServerDao;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * @description 异步更新表
 * @since 2025/5/9
 **/
@Slf4j
@Service
public class McpUpdateTask {
    @Resource
    private McpServerDao serverDao;


    public synchronized void updateServerViewCount(String serverId) {
        serverDao.updateViewTimes(serverId);
    }

    public synchronized void updateInstallTimes(String serverId) {
        serverDao.updateInstallTimes(serverId);
    }
}

