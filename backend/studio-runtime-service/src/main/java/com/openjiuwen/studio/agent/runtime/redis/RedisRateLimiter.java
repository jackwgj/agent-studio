/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.redis;

import jakarta.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 基于Redis的分布式限流实现
 */
@Component
public class RedisRateLimiter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // Lua脚本实现令牌桶算法
    private static final String RATE_LIMIT_LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])  -- 令牌桶容量
        local rate = tonumber(ARGV[2])      -- 令牌生成速率(个/秒)
        local now = tonumber(ARGV[3])       -- 当前时间戳(秒)
        local requested = tonumber(ARGV[4]) -- 请求的令牌数
        
        -- 初始化令牌桶
        local bucket = redis.call('hgetall', key)
        local lastRefillTime, tokens
        
        if table.getn(bucket) == 0 then
            -- 桶不存在，初始化
            lastRefillTime = now
            tokens = capacity
        else
            -- 从Redis中获取桶的信息
            lastRefillTime = tonumber(bucket[2])
            tokens = tonumber(bucket[1])
        end
        
        -- 计算上次填充到现在的时间差
        local delta = math.max(0, now - lastRefillTime)
        -- 计算新生成的令牌数
        tokens = math.min(capacity, tokens + delta * rate)
        -- 更新最后填充时间
        lastRefillTime = now
        
        -- 检查是否有足够的令牌
        local allowed = tokens >= requested
        if allowed then
            tokens = tokens - requested
        end
        
        -- 保存桶的状态，设置过期时间避免内存泄漏
        redis.call('hset', key, 'tokens', tokens, 'lastRefillTime', lastRefillTime)
        redis.call('expire', key, 60)  -- 60秒过期
        
        return allowed and 1 or 0
        """;

    private final DefaultRedisScript<Long> redisScript;

    public RedisRateLimiter() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RATE_LIMIT_LUA_SCRIPT);
        redisScript.setResultType(Long.class);
    }

    /**
     * 尝试获取令牌
     * @param modelId 模型ID
     * @param maxRequestsPerSecond 每秒最大请求数
     * @return 是否允许请求
     */
    public boolean tryAcquire(String modelId, int maxRequestsPerSecond) {
        if (maxRequestsPerSecond <= 0) {
            // 如果限流配置为0或负数，视为不限流
            return true;
        }

        String key = "model:rate:limiter:" + modelId;
        List<String> keys = Collections.singletonList(key);

        // 令牌桶容量设置为最大请求数，生成速率也是最大请求数/秒
        long now = System.currentTimeMillis() / 1000; // 当前时间戳(秒)
        Object[] args = {maxRequestsPerSecond, maxRequestsPerSecond, now, 1};

        // 执行Lua脚本
        Long result = stringRedisTemplate.execute(redisScript, keys, args);

        return result != null && result == 1;
    }
}
