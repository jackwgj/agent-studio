/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.redis.impl;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;

import lombok.extern.slf4j.Slf4j;

import org.redisson.client.codec.Codec;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Redis内存实现（仅用于测试）
 *
 */
@Slf4j
public class RedisClientMemory implements RedisClient {
    private static final Object LOCK = new Object();
    private final Map<String, String> cache = new HashMap<>();

    private final Map<String, CacheData<Long>> numberCache = new HashMap<>();

    public RedisClientMemory() {
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(this::clearExpiredData, 1, 1, TimeUnit.MINUTES);
    }

    private void clearExpiredData() {
        try {
            synchronized (LOCK) {
                numberCache.entrySet().removeIf(stringCacheDateEntry -> stringCacheDateEntry.getValue().isExpired());
            }
        } catch (Exception e) {
            log.warn("Clear expired data exception. {}", e.getMessage());
        }
    }

    @Override
    public boolean exists(String key) {
        return cache.containsKey(key);
    }

    @Override
    public String get(String key) {
        return cache.get(key);
    }

    @Override
    public String get(String key, Codec codec) {
        return cache.get(key);
    }

    @Override
    public void set(String key, String value, Duration duration) {
        cache.put(key, value);
    }
    
    @Override
    public void set(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public void setObj(String key, Object value) {
        cache.put(key, JSONObject.toJSONString(value));
    }

    @Override
    public boolean expire(String key, Duration duration) {
        return true;
    }

    @Override
    public boolean delete(String key) {
        cache.remove(key);
        return true;
    }

    @Override
    public void scoredSortedSet(String key, Long timeStamp, String str) {
        cache.put(timeStamp.toString(), str);
    }

    @Override
    public List<String> scoredSortedGet(String key, Long startTime, Long endTime) {
        return new ArrayList<>();
    }

    @Override
    public void scoredSortedRemoveList(String key, Long startTime, Long endTime) {
    }

    @Override
    public void scoredSortedRemove(String key, String str) {
    }

    @Override
    public Map<String, Object> getAll(List<String> keyList) {
        return new HashMap<>();
    }

    @Override
    public RedisLock getLock(String key) {
        return new RedisLock() {
            @Override
            public boolean tryLock(Duration maxWait) {
                return true;
            }

            @Override
            public void unlock() {

            }
        };
    }

    @Override
    public long getAndIncrement(String key, long timeoutSeconds) {
        synchronized (LOCK) {
            CacheData<Long> cacheData = numberCache.computeIfAbsent(key, k -> new CacheData<>(0L, timeoutSeconds));
            if (cacheData.isExpired()) {
                cacheData.data = 0L;
                cacheData.expireTime = System.currentTimeMillis() + timeoutSeconds * 1000L;
                return 0L;
            }
            return cacheData.data++;
        }
    }

    @Override
    public long addAndGet(String key, long dela, long timeoutSeconds) {
        synchronized (LOCK) {
            CacheData<Long> cacheData = numberCache.computeIfAbsent(key, k -> new CacheData<>(0L, timeoutSeconds));
            if (cacheData.isExpired()) {
                cacheData.data = 0L;
                cacheData.expireTime = System.currentTimeMillis() + timeoutSeconds * 1000L;
                return 0L;
            }
            cacheData.data += dela;
            return cacheData.data;
        }
    }

    @Override
    public void deleteByPrefix(String prefix) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
    }

    private static class CacheData<T> {
        CacheData(T data, long timeOutSeconds) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + timeOutSeconds * 1000;
        }

        private T data;

        private long expireTime;

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
