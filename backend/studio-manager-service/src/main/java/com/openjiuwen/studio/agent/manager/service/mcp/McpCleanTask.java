/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp;

import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpCleanTask {
    private static final Logger logger = LoggerFactory.getLogger(McpCleanTask.class);

    @Autowired
    McpServiceDao mcpServiceDao;

    private LocalDateTime lastSuccessfulMcpCleanTime;

    private int lastSuccessfulMcpCleanCount = 0;

    /**
     * 定时清理软删除的模板数据
     * 每天凌晨执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void hardDeleteExpiredMcpServices() {
        logger.info("Start cleaning old soft deleted mcp services...");
        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);
        try {
            List<String> ids = mcpServiceDao.listSoftDeletedMcpServiceBeforeTime(twoYearsAgo);
            if (CollectionUtils.isEmpty(ids)) {
                logger.info("No soft deleted mcp services found before 2 years ago, skip cleaning...");
                return;
            }
            int cleanCount = mcpServiceDao.hardDeleteMcpService(ids);
            lastSuccessfulMcpCleanTime = LocalDateTime.now();
            lastSuccessfulMcpCleanCount = cleanCount;
            logger.info("Cleaned {} soft deleted mcp services", cleanCount);
        } catch (Exception e) {
            logger.error("Failed to clean soft deleted mcp services", e);
        }
    }

    /**
     * 获取任务状态（用于监控）
     */
    public String getTaskStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastMcpSuccess", lastSuccessfulMcpCleanTime);
        status.put("mcpToClean", lastSuccessfulMcpCleanCount);
        // 格式化输出
        StringBuilder sb = new StringBuilder("McpCleanTask[\n");
        for (Map.Entry<String, Object> entry : status.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
