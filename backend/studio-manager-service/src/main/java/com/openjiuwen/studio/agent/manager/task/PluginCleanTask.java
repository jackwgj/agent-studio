/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.task;

import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;

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
public class PluginCleanTask {

    private static final Logger logger = LoggerFactory.getLogger(PluginCleanTask.class);

    @Autowired
    private PluginMapper pluginMapper;

    @Autowired
    private ReleaseVersionMapper releaseVersionMapper;


    public LocalDateTime lastSuccessfulPluginCleanTime;

    public int lastSuccessfulPluginCleanCount = 0;

    /**
     * 定时清理软删除的模板数据
     * 每天凌晨执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanOldSoftDeletePluginIds() {
        logger.info("Start cleaning old soft deleted pluginIds...");

        // 计算两年前的时间
        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);

        // 查询所有两年前软删除的模板 ID
        List<String> pluginIds = pluginMapper.findPluginToBeDeleted(twoYearsAgo);

        if (pluginIds == null || pluginIds.isEmpty()) {
            logger.info("No pluginIds to clean.");
            return;
        }

        try {
            // 执行真正删除模板
            for (String pluginId : pluginIds) {
                releaseVersionMapper.deleteByPluginId(pluginId, twoYearsAgo);
            }
            pluginMapper.deleteByPrimaryKeys(pluginIds);

            lastSuccessfulPluginCleanTime = LocalDateTime.now();
            lastSuccessfulPluginCleanCount = pluginIds.size();

            logger.info("Successfully deleted {} pluginIds permanently.", lastSuccessfulPluginCleanCount);

        } catch (Exception e) {
            logger.error("Failed to delete pluginIds permanently.", e);
        }
    }

    /**
     * 获取任务状态（用于监控）
     */
    public String getTaskStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastPluginIdSuccess", lastSuccessfulPluginCleanTime);
        status.put("pluginIdsToClean", lastSuccessfulPluginCleanCount);

        // 格式化输出
        StringBuilder sb = new StringBuilder("PluginCleanTask[\n");
        for (Map.Entry<String, Object> entry : status.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        sb.append("]");

        return sb.toString();
    }
}
