/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.utils.redis.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.redis.impl.RedisClientMemory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class RedisClientMemoryTest {

    private RedisClientMemory redisClientMemory;

    @BeforeEach
    void setUp() {
        redisClientMemory = new RedisClientMemory();
    }

    /**
     * 覆盖所有基础 CRUD 和 内存模拟逻辑
     */
    @Test
    void testAllMemoryOperations() throws Exception {
        String key = "testKey";
        String val = "testVal";

        redisClientMemory.set(key, val);
        assertTrue(redisClientMemory.exists(key));
        assertEquals(val, redisClientMemory.get(key));

        redisClientMemory.set(key, val, Duration.ofSeconds(10));

        redisClientMemory.setObj("objKey", new Object());

        assertDoesNotThrow(() -> {
            redisClientMemory.scoredSortedSet("zkey", 1000L, "member");
            redisClientMemory.scoredSortedRemove("zkey", "member");
            redisClientMemory.scoredSortedRemoveList("zkey", 0L, 100L);
        });

        List<String> zsetResult = redisClientMemory.scoredSortedGet("zkey", 0L, 100L);
        assertNotNull(zsetResult);
        assertTrue(zsetResult.isEmpty());

        long incRes = redisClientMemory.getAndIncrement("atomicKey", 60);
        assertEquals(0L, incRes);

        long addRes = redisClientMemory.addAndGet("atomicKey", 5L, 60);
        assertEquals(6L, addRes);

        long zeroAdd = redisClientMemory.addAndGet("atomicKey", 0L, 60);
        assertEquals(6L, zeroAdd);

        Map<String, Object> all = redisClientMemory.getAll(List.of("k1", "k2"));
        assertNotNull(all);

        RedisLock lock = redisClientMemory.getLock("lockKey");
        assertNotNull(lock);
        assertTrue(lock.tryLock(Duration.ofSeconds(1)));
        assertDoesNotThrow(lock::unlock);

        assertTrue(redisClientMemory.expire(key, Duration.ofSeconds(1)));
        assertTrue(redisClientMemory.delete(key));
        assertFalse(redisClientMemory.exists(key));
    }
}