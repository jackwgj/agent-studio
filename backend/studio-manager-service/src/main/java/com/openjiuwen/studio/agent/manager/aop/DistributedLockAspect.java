/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.aop;

import com.openjiuwen.studio.agent.common.annotation.DistributedLock;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 分布式锁
 *
 */
@Aspect
@Component
@Slf4j
public class DistributedLockAspect {

    private final RedisClient redisClient;

    public DistributedLockAspect(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {

        // 锁级别为用户级别
        String lockKey = distributedLock.prefix() + RequestContextUtils.getRequestUserId();
        log.info("[Distribute Lock] Current user ID for lock: {}", RequestContextUtils.getRequestUserId());
        log.info("[Distribute Lock] Generated lock key: {}", lockKey);

        RedisLock lock = redisClient.getLock(lockKey);
        try {
            log.info("[Distribute Lock] acquire success, thread: {}, LockKey: {}",
                Thread.currentThread().getName(), lockKey);
            boolean acquired = lock.tryLock(Duration.ofSeconds(10));
            if (!acquired) {
                log.error("Failed to obtain the distributed lock....");
                throw new AgentStudioException(StudioError.OBTAIN_DISTRIBUTED_LOCK_ERROR);
            }
            return joinPoint.proceed();
        } finally {
            if (lock != null) {
                try {
                    lock.unlock();
                    log.debug("Released lock: {}", lockKey);
                } catch (Exception e) {
                    log.warn("Failed to release distributed lock: {}", lockKey, e);
                }
            }
        }

    }
}
