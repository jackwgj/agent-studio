/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.task;

import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.v2.PromptEngineerDataSetService;

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
public class PromptCleanTask {

    private static final Logger logger = LoggerFactory.getLogger(PromptCleanTask.class);

    @Autowired
    private PePromptTemplateMapper pePromptTemplateMapper;

    @Autowired
    private PromptEngineerDataSetService promptEngineerDataSetService;

    @Autowired
    private PromptTaskMapper promptTaskMapper;

    private LocalDateTime lastSuccessfulTemplateCleanTime;

    private LocalDateTime lastSuccessfulTaskCleanTime;

    private int lastSuccessfulTemplateCleanCount = 0;

    private int lastSuccessfulTaskCleanCount = 0;

    /**
     * 定时清理软删除的模板数据
     * 每天凌晨执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanOldSoftDeletedTemplates() {
        logger.info("Start cleaning old soft deleted templates...");

        // 计算两年前的时间
        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);

        // 查询所有两年前软删除的模板 ID
        List<String> templateIds = pePromptTemplateMapper.findTemplatesToBeDeleted(twoYearsAgo);

        if (templateIds == null || templateIds.isEmpty()) {
            logger.info("No templates to clean.");
            return;
        }

        try {
            // 执行真正删除模板
            pePromptTemplateMapper.deleteTemplatesPermanently(templateIds);

            lastSuccessfulTemplateCleanTime = LocalDateTime.now();
            lastSuccessfulTemplateCleanCount = templateIds.size();

            logger.info("Successfully deleted {} templates permanently.", lastSuccessfulTemplateCleanCount);

        } catch (Exception e) {
            logger.error("Failed to delete templates permanently.", e);
        }
    }

    /**
     * 定时清理软删除的优化任务
     * 每天凌晨执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanOldSoftDeletedTasks() {
        logger.info("Start cleaning old soft deleted optimization tasks...");

        // 计算两年前的时间
        LocalDateTime twoYearsAgo = LocalDateTime.now().minusYears(2);

        // 查询所有两年前软删除的优化任务 ID
        List<String> taskIds = promptTaskMapper.findTasksToBeDeleted(twoYearsAgo);
        if (taskIds == null || taskIds.isEmpty()) {
            logger.info("No optimization tasks to clean.");
            return;
        }

        try {
            List<PromptTaskEntity> taskDetails = promptTaskMapper.selectByIds(taskIds);

            for (PromptTaskEntity taskDetail : taskDetails) {
                // 删除 OBS 中的 dataset 文件
                promptEngineerDataSetService.deleteDateSet(taskDetail.getId(), taskDetail.getProjectId(),
                    taskDetail.getWorkspaceId());
            }

            // 执行真正删除优化任务
            promptTaskMapper.deleteTasksPermanently(taskIds);

            lastSuccessfulTaskCleanTime = LocalDateTime.now();
            lastSuccessfulTaskCleanCount = taskDetails.size();

            logger.info("Successfully deleted {} optimization tasks permanently.", lastSuccessfulTaskCleanCount);

        } catch (Exception e) {
            logger.error("Failed to delete optimization tasks permanently.", e);
        }
    }

    /**
     * 获取任务状态（用于监控）
     */
    public String getTaskStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("lastTemplateSuccess", lastSuccessfulTemplateCleanTime);
        status.put("templatesToClean", lastSuccessfulTemplateCleanCount);
        status.put("lastTaskSuccess", lastSuccessfulTaskCleanTime);
        status.put("tasksToClean", lastSuccessfulTaskCleanCount);

        // 格式化输出
        StringBuilder sb = new StringBuilder("PromptCleanTask[\n");
        for (Map.Entry<String, Object> entry : status.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        sb.append("]");

        return sb.toString();
    }
}
