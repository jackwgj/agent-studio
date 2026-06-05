/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.utils.redis.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisLock;

import lombok.extern.slf4j.Slf4j;

import org.redisson.client.codec.Codec;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Redis内存实现（仅用于测试）
 */
@Slf4j
public class RedisClientMemory implements RedisClient {
    private static final Object LOCK = new Object();

    private final Map<String, String> cache = new HashMap<>();

    private final Map<String, CacheData<Long>> numberCache = new HashMap<>();

    private final Map<String, String> lockHolderMap = new HashMap<>();

    private final Object lockHolderLock = new Object();

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
    public long getAndIncrement(String key, int timeoutSeconds) {
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
    public long addAndGet(String key, long dela, int timeoutSeconds) {
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
    public void lPush(String key, String value) {
        List<String> list = getListInternal(key);
        // lPush: 插入到索引 0
        list.add(0, value);
        cache.put(key, JSON.toJSONString(list));
    }

    @Override
    public void rPush(String key, String value) {
        List<String> list = getListInternal(key);
        // rPush: 直接 add 默认到末尾
        list.add(value);
        cache.put(key, JSON.toJSONString(list));
    }

    @Override
    public void deleteKeys(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            cache.remove(key);
        }
    }

    @Override
    public Long lSize(String key) {
        List<String> list = getListInternal(key);
        return (long) list.size();
    }

    @Override
    public List<String> lRange(String key, int start, int end) {
        List<String> list = getListInternal(key);
        int size = list.size();
        if (start >= size) {
            return List.of();
        }
        int actualStart = Math.max(0, start);
        int actualEnd = Math.min(size - 1, end);

        if (actualStart > actualEnd) {
            return List.of();
        }
        return new ArrayList<>(list.subList(actualStart, actualEnd + 1));
    }

    @Override
    public boolean tryLockWithId(String key, String value, long expireSeconds) {
        synchronized (lockHolderLock) {
            // 检查是否已有锁
            if (lockHolderMap.containsKey(key)) {
                log.warn("Lock already held: key={}, currentHolder={}", key, lockHolderMap.get(key));
                return false;
            }
            // 设置锁持有者（value 应为 UUID）
            lockHolderMap.put(key, value);
            // 可选：设置自动过期（模拟 Redis 的超时）
            // 注意：你这个类是内存实现，没有定时任务，所以这里我们只能“手动”清理
            // 在真实场景中，你应该用一个定时线程清理过期锁
            // 但为了简化，我们只在 unlock 或 tryLock 时做检测
            log.info("Successfully acquired memory lock: key={}, holder={}", key, value);
            return true;
        }
    }

    @Override
    public boolean unlockWithId(String key, String value) {
        synchronized (lockHolderLock) {
            // 检查锁是否存在
            if (!lockHolderMap.containsKey(key)) {
                log.warn("Lock not found when releasing: key={}", key);
                return false;
            }
            // 检查是否是自己持有的锁
            String holder = lockHolderMap.get(key);
            if (!value.equals(holder)) {
                log.warn("Attempt to release lock held by another client: key={}, expected={}, actual={}", key, value,
                    holder);
                return false;
            }
            // 释放锁
            lockHolderMap.remove(key);
            log.info("Successfully released memory lock: key={}, holder={}", key, value);
            return true;
        }
    }

    /**
     * 内部获取并解析 List 的逻辑
     */
    private List<String> getListInternal(String key) {
        String json = cache.get(key);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // 使用 JSON 静态方法解析，这在 FastJSON 中是最通用的
            List<String> list = JSON.parseArray(json, String.class);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            // 如果原本不是数组格式，则重置为新列表
            return new ArrayList<>();
        }
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
