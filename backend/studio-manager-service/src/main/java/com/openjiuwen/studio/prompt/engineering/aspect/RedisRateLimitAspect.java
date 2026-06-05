/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.aspect;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.prompt.engineering.annotation.RedisRateLimit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

@Slf4j
@Aspect
@Component
public class RedisRateLimitAspect {

    @Autowired
    private RedisClient redisClient;

    private static final long EXPIRE_TIME = 30L;

    // 切入点：标记了 @RedisRateLimit 注解的方法
    @Pointcut("@annotation(com.openjiuwen.studio.prompt.engineering.annotation.RedisRateLimit)")
    public void rateLimitPointcut() {
    }

    /**
     * 环绕通知：执行限流逻辑
     */
    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 获取注解和接口信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RedisRateLimit rateLimit = method.getAnnotation(RedisRateLimit.class);

        // 2. 确定限流 key（默认用接口url + domainId）
        String baseKey = rateLimit.key();
        if (baseKey.isEmpty()) {
            ServletRequestAttributes attributes
                = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            baseKey = request.getRequestURI() + ":" + RequestContextUtils.getRequestUserDomainId();
        }
        // 拼接前缀，避免 key 冲突
        String tokenKey = "rate_limit:" + baseKey + ":tokens";
        String timeKey = "rate_limit:" + baseKey + ":last_refill_time";

        // 3. 执行令牌桶限流逻辑
        boolean allowed = tryAcquireToken(tokenKey, timeKey, rateLimit.rate(), rateLimit.capacity());
        if (!allowed) {
            throw new AgentStudioException(StudioError.CALL_EXCEED_LIMIT);
        }

        // 4. 放行请求
        return joinPoint.proceed();
    }

    /**
     * 尝试获取令牌（适配自定义 RedisClient 的令牌桶核心逻辑）
     *
     * @param tokenKey 存储剩余令牌数的 key
     * @param timeKey 存储最后填充时间的 key
     * @param rate 令牌生成速率（个/秒）
     * @param capacity 令牌桶最大容量
     * @return 是否获取到令牌
     */
    boolean tryAcquireToken(String tokenKey, String timeKey, double rate, int capacity) {
        // 获取分布式锁，保证并发下操作原子性（锁 key 与限流 key 关联）
        String lockKey = "rate_limit:lock:" + tokenKey;
        RedisLock lock = redisClient.getLock(lockKey);
        try {
            // 加锁
            if (lock.tryLock(Duration.ofMillis(500))) {
                // 1. 获取当前桶的状态（剩余令牌数、最后填充时间）
                String tokensStr = redisClient.get(tokenKey);
                String timeStr = redisClient.get(timeKey);
                long now = System.currentTimeMillis();

                long currentTokens;
                long lastRefillTime;

                // 2. 初始化桶（首次访问）
                if (tokensStr == null || timeStr == null) {
                    currentTokens = capacity - 1L; // 首次请求消耗 1 个令牌
                    lastRefillTime = now;
                    // 存储初始值，设置 1 小时过期
                    redisClient.set(tokenKey, String.valueOf(currentTokens), Duration.ofMinutes(EXPIRE_TIME));
                    redisClient.set(timeKey, String.valueOf(lastRefillTime), Duration.ofMinutes(EXPIRE_TIME));
                    return true;
                }

                // 3. 解析令牌数和时间
                try {
                    currentTokens = Long.parseLong(tokensStr);
                    lastRefillTime = Long.parseLong(timeStr);
                } catch (NumberFormatException e) {
                    // 解析失败，重置桶
                    log.error("parse token failed, reset status");
                    currentTokens = capacity - 1L;
                    lastRefillTime = now;
                    redisClient.set(tokenKey, String.valueOf(currentTokens), Duration.ofMinutes(EXPIRE_TIME));
                    redisClient.set(timeKey, String.valueOf(lastRefillTime), Duration.ofMinutes(EXPIRE_TIME));
                    return true;
                }

                // 4. 计算从上一次填充到现在生成的新令牌数
                double timeElapsed = (now - lastRefillTime) / 1000.0; // 时间差（秒）
                long newTokens = (long) (timeElapsed * rate);

                // 5. 填充令牌（不超过桶的最大容量）
                if (newTokens > 0) {
                    currentTokens = Math.min(currentTokens + newTokens, capacity);
                    redisClient.set(timeKey, String.valueOf(now), Duration.ofMinutes(EXPIRE_TIME)); // 更新最后填充时间
                }

                // 6. 尝试获取 1 个令牌
                if (currentTokens > 0) {
                    redisClient.set(tokenKey, String.valueOf(currentTokens - 1), Duration.ofMinutes(EXPIRE_TIME));
                    return true;
                } else {
                    log.warn("Rate limit triggered");
                    return false;
                }
            } else {
                // 获取锁失败，默认拒绝（或重试，根据业务调整）
                log.warn("Failed to acquire rate limit lock, rejecting request");
                return false;
            }
        } catch (Exception e) {
            log.warn("Failed to acquire rate limit lock, rejecting request");
            return false;
        } finally {
            // 释放锁
            lock.unlock();
        }
    }
}
