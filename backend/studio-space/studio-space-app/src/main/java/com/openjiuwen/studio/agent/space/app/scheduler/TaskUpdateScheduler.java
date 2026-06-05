/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openjiuwen.studio.agent.space.api.vo.AgentType;
import com.openjiuwen.studio.agent.space.app.enums.DRTaskStatusType;
import com.openjiuwen.studio.agent.space.app.task.DRTaskHandler;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisLock;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderTaskMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务定时刷新
 */
@Component
@EnableScheduling
@Slf4j
public class TaskUpdateScheduler {
    @Resource
    private AgentBuilderTaskMapper AgentBuilderTaskMapper;

    @Resource
    private RedisClient redisClient;

    @Resource
    private DRTaskHandler drTaskHandler;

    private static final String KEY_PREFIX = "AgentSpace";

    @Scheduled(fixedDelay = 30000)
    public void executeDRTaskUpdate() {
        String lockKey = KEY_PREFIX + "DR_ExecuteTaskUpdate";
        RedisLock lock = redisClient.getLock(lockKey);
        try {
            if (lock.tryLock(Duration.ofSeconds(1800))) {
                // 获取锁成功，执行业务逻辑
                log.info("executeDRTaskUpdate get lock success.");
                updateDRTaskInfo();
            } else {
                // 获取锁失败，处理重试或降级
                log.info("executeDRTaskUpdate get lock failed.");
            }
        } catch (InterruptedException e) {
            log.error("[executeDRTaskUpdate] interrupted while acquiring lock.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[TaskUpdateScheduler] executeDRTaskUpdate execute failed:", e);
        } finally {
            if (lock != null) {
                try {
                    lock.unlock();
                    log.info("Released lock: {}", lockKey);
                } catch (Exception e) {
                    log.warn("Failed to release distributed lock: {}", lockKey, e);
                }
            }
        }
    }

    private void updateDRTaskInfo() {
        // 处理未完成的任务,正在跑  等待120分钟
        LambdaQueryWrapper<AgentBuilderTaskEntity> unfinishedQueryWrapper = new LambdaQueryWrapper<>();
        unfinishedQueryWrapper.in(AgentBuilderTaskEntity::getStatus,
            DRTaskStatusType.getRunningStatus().stream().map(DRTaskStatusType::getStatus).collect(Collectors.toList()));
        unfinishedQueryWrapper.eq(AgentBuilderTaskEntity::getAgentType, AgentType.DEEP_RESEARCH.getCode());
        unfinishedQueryWrapper.lt(AgentBuilderTaskEntity::getLastUpdatedDate, LocalDateTime.now().minusMinutes(120));
        List<AgentBuilderTaskEntity> agentBuilderTaskEntities = AgentBuilderTaskMapper.selectList(new Page<>(1, 60),
            unfinishedQueryWrapper);
        log.info("[updateDRTaskInfo] timeout task size: {} ", agentBuilderTaskEntities.size());
        for (AgentBuilderTaskEntity AgentBuilderTaskEntity : agentBuilderTaskEntities) {
            drTaskHandler.updateTaskStatus(AgentBuilderTaskEntity.getId(), DRTaskStatusType.TIME_OUT.getStatus());
        }

        // 处理未完成的任务,等待用户输入  等待5分钟
        unfinishedQueryWrapper = new LambdaQueryWrapper<>();
        unfinishedQueryWrapper.in(AgentBuilderTaskEntity::getStatus,
            List.of(DRTaskStatusType.INITIATING.getStatus(), DRTaskStatusType.WAITING_USER_INPUT.getStatus()));
        unfinishedQueryWrapper.eq(AgentBuilderTaskEntity::getAgentType, AgentType.DEEP_RESEARCH.getCode());
        unfinishedQueryWrapper.lt(AgentBuilderTaskEntity::getLastUpdatedDate, LocalDateTime.now().minusMinutes(5));
        agentBuilderTaskEntities = AgentBuilderTaskMapper.selectList(new Page<>(1, 60), unfinishedQueryWrapper);
        log.info("[updateDRTaskInfo] timeout task size: {} ", agentBuilderTaskEntities.size());
        for (AgentBuilderTaskEntity AgentBuilderTaskEntity : agentBuilderTaskEntities) {
            drTaskHandler.updateTaskStatus(AgentBuilderTaskEntity.getId(), DRTaskStatusType.TIME_OUT.getStatus());
        }
    }
}
