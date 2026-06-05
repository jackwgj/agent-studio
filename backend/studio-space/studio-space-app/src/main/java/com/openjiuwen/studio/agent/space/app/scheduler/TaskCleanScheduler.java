/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisLock;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderActionMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderFileMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderMessageMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderStepMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderTaskMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderActionEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderFileEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderMessageEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderStepEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务清理，任务只保留30天
 */
@Slf4j
public class TaskCleanScheduler {
    @Value("${agent.builder.task.duration:30}")
    private Integer duration;

    @Resource
    private AgentBuilderTaskMapper AgentBuilderTaskMapper;

    @Resource
    private AgentBuilderMessageMapper AgentBuilderMessageMapper;

    @Resource
    private AgentBuilderStepMapper AgentBuilderStepMapper;

    @Resource
    private AgentBuilderActionMapper AgentBuilderActionMapper;

    @Resource
    private AgentBuilderFileMapper AgentBuilderFileMapper;

    private static final String KEY_PREFIX = "AgentSpace";

    @Resource
    private RedisClient redisClient;

    @Scheduled(cron = "${task.update.scheduleTime:0 0 3 * * ?}")
    @Transactional(rollbackFor = Exception.class)
    public void executeCleanTask() {
        String lockKey = KEY_PREFIX + "ExecuteCleanTask";
        RedisLock lock = redisClient.getLock(lockKey);
        try {
            if (lock.tryLock(Duration.ofSeconds(180L * 60))) {
                // 获取锁成功，执行业务逻辑
                log.info("[executeCleanTask] get lock success.");
                cleanTask();
            } else {
                // 获取锁失败，处理重试或降级
                log.info("[executeCleanTask] get lock failed.");
            }
        } catch (InterruptedException e) {
            log.error("[executeCleanTask] interrupted while acquiring lock.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[executeCleanTask] init failed:", e);
        } finally {
            if (lock != null) {
                try {
                    lock.unlock();
                    log.info("[executeCleanTask] Released lock: {}", lockKey);
                } catch (Exception e) {
                    log.warn("[executeCleanTask] Failed to release distributed lock: {}", lockKey, e);
                }
            }
        }
    }

    public void cleanTask() {
        log.info("[cleanTask] Clean task start.");

        // 获取N天前的日期
        LocalDate dateExpired = LocalDate.now().minusDays(duration);
        long expirationTime = AppCommonUtils.getDayStartTime(dateExpired);
        deleteTask(expirationTime);

        log.info("[cleanTask] Clean task end.");
    }

    public void deleteTask(long expirationTime) {
        int totalDeletedSize = 0;
        int batchSize = 100;
        long currentPage = 1;
        List<AgentBuilderTaskEntity> taskEntities;

        log.info("[deleteTask] start delete expired tasks, expired time: {}",
            AppCommonUtils.getStandardDateTime(expirationTime));

        do {
            // 创建分页对象，从当前页开始查询
            Page<AgentBuilderTaskEntity> page = new Page<>(currentPage, batchSize);

            // 修正查询条件：使用创建时间而非用户ID
            LambdaQueryWrapper<AgentBuilderTaskEntity> taskQuery = new LambdaQueryWrapper<>();
            taskQuery.le(AgentBuilderTaskEntity::getCreatedDate, new Timestamp(expirationTime));
            taskQuery.like(AgentBuilderTaskEntity::getDisplayInfo, "\"is_pin\":false");
            taskQuery.orderByAsc(AgentBuilderTaskEntity::getId); // 确保分页顺序稳定

            // 执行分页查询
            Page<AgentBuilderTaskEntity> taskEntityPage = AgentBuilderTaskMapper.selectPage(page, taskQuery);
            taskEntities = taskEntityPage.getRecords();

            if (!taskEntities.isEmpty()) {
                // 收集所有需要删除的ID
                List<String> taskIds = taskEntities.stream()
                    .map(AgentBuilderTaskEntity::getId)
                    .collect(Collectors.toList());

                // 1. 批量删除关联的step
                List<String> messageIds = deleteStepsByTaskIds(taskIds).stream()
                    .map(AgentBuilderMessageEntity::getId)
                    .toList();

                // 2. 批量删除关联的action文件
                deleteActionFilesByTaskIds(taskIds);

                // 3. 批量删除关联的action
                deleteActionsByMessageIds(messageIds);

                // 4. 批量删除关联的message
                deleteMessagesByTaskIds(taskIds);

                // 5. 批量删除task
                AgentBuilderTaskMapper.deleteBatchIds(taskIds);

                totalDeletedSize += taskIds.size();
                currentPage++;

                log.info("[deleteTask] Processed {} overdue tasks. Processed {} tasks in the current batch.",
                    totalDeletedSize, taskIds.size());
            }
        } while (taskEntities.size() >= batchSize);

        log.info("[deleteTask] Expired task deletion completed. Total number of records processed: {}",
            totalDeletedSize);
    }

    // 批量删除action关联的file
    private void deleteActionFilesByTaskIds(List<String> taskIds) {
        // 查询所有关联的file
        LambdaQueryWrapper<AgentBuilderFileEntity> fileQuery = new LambdaQueryWrapper<>();
        fileQuery.in(AgentBuilderFileEntity::getTaskId, taskIds);
        AgentBuilderFileMapper.delete(fileQuery);
    }

    // 批量删除action
    private void deleteActionsByMessageIds(List<String> messageIds) {
        // 先查询关联的steps
        LambdaQueryWrapper<AgentBuilderStepEntity> stepQuery = new LambdaQueryWrapper<>();
        stepQuery.in(AgentBuilderStepEntity::getMessageId, messageIds);
        List<AgentBuilderStepEntity> steps = AgentBuilderStepMapper.selectList(stepQuery);

        if (!steps.isEmpty()) {
            List<String> stepIds = steps.stream().map(AgentBuilderStepEntity::getId).collect(Collectors.toList());

            LambdaQueryWrapper<AgentBuilderActionEntity> actionQuery = new LambdaQueryWrapper<>();
            actionQuery.in(AgentBuilderActionEntity::getStepId, stepIds);
            AgentBuilderActionMapper.delete(actionQuery);
        }
    }

    // 批量删除step
    private List<AgentBuilderMessageEntity> deleteStepsByTaskIds(List<String> taskIds) {
        // 先查询关联的messageIds
        LambdaQueryWrapper<AgentBuilderMessageEntity> messageQuery = new LambdaQueryWrapper<>();
        messageQuery.in(AgentBuilderMessageEntity::getTaskId, taskIds);
        List<AgentBuilderMessageEntity> messages = AgentBuilderMessageMapper.selectList(messageQuery);

        if (!messages.isEmpty()) {
            List<String> messageIds = messages.stream().map(AgentBuilderMessageEntity::getId).collect(Collectors.toList());

            LambdaQueryWrapper<AgentBuilderStepEntity> stepQuery = new LambdaQueryWrapper<>();
            stepQuery.in(AgentBuilderStepEntity::getMessageId, messageIds);
            AgentBuilderStepMapper.delete(stepQuery);
        }

        return messages;
    }

    // 批量删除message
    private void deleteMessagesByTaskIds(List<String> taskIds) {
        LambdaQueryWrapper<AgentBuilderMessageEntity> messageQuery = new LambdaQueryWrapper<>();
        messageQuery.in(AgentBuilderMessageEntity::getTaskId, taskIds);
        AgentBuilderMessageMapper.delete(messageQuery);
    }
}
