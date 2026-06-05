/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.annotation.quota;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisLock;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderQuotaMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderQuotaEntity;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * 配额管理服务
 */
@Service
@Slf4j
public class QuotaService {
    /**
     * 配额类型：任务运行次数
     */
    public static final String TASK_RUN_COUNT = "task_run_count";

    public static final String TASK_LOCK_KEY_TEMPLATE = "CHECK_TASK_QUOTA_%s_%s";

    @Value("#{'${agent.builder.task.whiteList.quota:9190086000001387250}'.split(',')}")
    private List<String> quotaWhiteList;

    @Value("${agent.builder.task.run.count: 15}")
    private int taskRunCount;

    @Resource
    private RedisClient redisClient;

    @Resource
    private AgentBuilderQuotaMapper quotaMapper;

    @Transactional
    public void verifyQuota(String type, int offset) {
        AppCommonUtils.validateDomainIdUserIdIsNotEmpty();
        if (quotaWhiteList.contains(AuthorizationContextHolder.domainId())) {
            // 白名单配额不做限制
            return;
        }
        if (TASK_RUN_COUNT.equals(type)) {
            // 并发拿锁
            String lockKey = TASK_LOCK_KEY_TEMPLATE.formatted(AuthorizationContextHolder.domainId(),
                AuthorizationContextHolder.userId());
            RedisLock lock = redisClient.getLock(lockKey);
            try {
                if (lock.tryLock(Duration.ofSeconds(10))) {
                    AgentBuilderQuotaEntity quota = quotaMapper.selectOne(
                        new LambdaQueryWrapper<AgentBuilderQuotaEntity>().eq(AgentBuilderQuotaEntity::getType, type)
                            .eq(AgentBuilderQuotaEntity::getDomainId, AuthorizationContextHolder.domainId()));
                    if (quota == null) {
                        quota = new AgentBuilderQuotaEntity(AppCommonUtils.getUUID(), type, taskRunCount, 0);
                        AppCommonUtils.setCommonCreateProperties(quota);
                        quotaMapper.insert(quota);
                    }
                    if (quota.getUsed() + offset > quota.getQuota()) {
                        log.error("[verifyQuota] validate task quota fail, domainId:{}, used:{}, quota:{}",
                            AuthorizationContextHolder.domainId(), quota.getUsed(), quota.getQuota());
                        throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_EXCEEDING_QUOTA_ERROR,
                            AuthorizationContextHolder.domainId(), TASK_RUN_COUNT, String.valueOf(quota.getQuota()));
                    }
                } else {
                    log.error("[verifyQuota] validate task fail, too frequent operation, tenantId:{}",
                        AuthorizationContextHolder.domainId());
                    throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_SERVICE_INTERNAL_ERROR,
                        "too frequent operation");
                }
            } catch (InterruptedException e) {
                log.error("[verifyQuota] Interrupted while acquiring lock, tenantId:{}",
                    AuthorizationContextHolder.domainId());
                Thread.currentThread().interrupt();
                throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_SERVICE_INTERNAL_ERROR,
                    "interrupted while acquiring lock");
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
    }

    public void operateQuota(String type, int offset) {
        AppCommonUtils.validateDomainIdUserIdIsNotEmpty();
        if (quotaWhiteList.contains(AuthorizationContextHolder.domainId())) {
            // 白名单配额不做限制
            return;
        }
        if (TASK_RUN_COUNT.equals(type)) {
            // 更新配额目前不做严格控制，后续计费上线再做并发同步逻辑
            AgentBuilderQuotaEntity quota = quotaMapper.selectOne(
                new LambdaQueryWrapper<AgentBuilderQuotaEntity>().eq(AgentBuilderQuotaEntity::getType, type)
                    .eq(AgentBuilderQuotaEntity::getDomainId, AuthorizationContextHolder.domainId()));
            quota.setUsed(quota.getUsed() + offset);
            AppCommonUtils.setCommonUpdateProperties(quota);
            quotaMapper.updateById(quota);
        }
    }
}