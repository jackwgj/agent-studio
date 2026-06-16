/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisClientWrapperTest {

    @Mock
    private RedisClient redisClient;

    private RedisClientWrapper wrapperWithLog;

    private RedisClientWrapper wrapperWithoutLog;

    @BeforeEach
    void setUp() {
        wrapperWithLog = new RedisClientWrapper(redisClient, true);
        wrapperWithoutLog = new RedisClientWrapper(redisClient, false);
    }

    @Test
    void testExists_True() {
        when(redisClient.exists("key1")).thenReturn(true);
        assertTrue(wrapperWithLog.exists("key1"));
        verify(redisClient).exists("key1");
    }

    @Test
    void testExists_False() {
        when(redisClient.exists("key1")).thenReturn(false);
        assertFalse(wrapperWithLog.exists("key1"));
    }

    @Test
    void testExists_Exception() {
        when(redisClient.exists("key1")).thenThrow(new RuntimeException("Redis error"));
        assertFalse(wrapperWithLog.exists("key1"));
    }

    @Test
    void testGet() {
        when(redisClient.get("key1")).thenReturn("value1");
        assertEquals("value1", wrapperWithLog.get("key1"));
    }

    @Test
    void testGet_Null() {
        when(redisClient.get("key1")).thenReturn(null);
        assertNull(wrapperWithLog.get("key1"));
    }

    @Test
    void testGet_Exception() {
        when(redisClient.get("key1")).thenThrow(new RuntimeException("Redis error"));
        assertNull(wrapperWithLog.get("key1"));
    }

    @Test
    void testGetWithCodec() {
        when(redisClient.get(eq("key1"), any())).thenReturn("value1");
        assertEquals("value1", wrapperWithLog.get("key1", null));
    }

    @Test
    void testSetWithDuration() {
        Duration duration = Duration.ofMinutes(5);
        wrapperWithLog.set("key1", "value1", duration);
        verify(redisClient).set("key1", "value1", duration);
    }

    @Test
    void testSetWithDuration_Exception() {
        Duration duration = Duration.ofMinutes(5);
        doThrow(new RuntimeException("Redis error")).when(redisClient).set("key1", "value1", duration);
        wrapperWithLog.set("key1", "value1", duration);
    }

    @Test
    void testSetWithoutDuration() {
        wrapperWithLog.set("key1", "value1");
        verify(redisClient).set("key1", "value1");
    }

    @Test
    void testSetWithoutDuration_Exception() {
        doThrow(new RuntimeException("Redis error")).when(redisClient).set("key1", "value1");
        wrapperWithLog.set("key1", "value1");
    }

    @Test
    void testSetObj() {
        Object obj = new Object();
        wrapperWithLog.setObj("key1", obj);
        verify(redisClient).setObj("key1", obj);
    }

    @Test
    void testSetObj_Exception() {
        Object obj = new Object();
        doThrow(new RuntimeException("Redis error")).when(redisClient).setObj("key1", obj);
        wrapperWithLog.setObj("key1", obj);
    }

    @Test
    void testExpire() {
        Duration duration = Duration.ofMinutes(5);
        when(redisClient.expire("key1", duration)).thenReturn(true);
        assertTrue(wrapperWithLog.expire("key1", duration));
    }

    @Test
    void testExpire_Exception() {
        Duration duration = Duration.ofMinutes(5);
        when(redisClient.expire("key1", duration)).thenThrow(new RuntimeException("Redis error"));
        assertFalse(wrapperWithLog.expire("key1", duration));
    }

    @Test
    void testDelete() {
        when(redisClient.delete("key1")).thenReturn(true);
        assertTrue(wrapperWithLog.delete("key1"));
    }

    @Test
    void testDelete_Exception() {
        when(redisClient.delete("key1")).thenThrow(new RuntimeException("Redis error"));
        assertFalse(wrapperWithLog.delete("key1"));
    }

    @Test
    void testScoredSortedSet() {
        wrapperWithLog.scoredSortedSet("key1", 1000L, "value1");
        verify(redisClient).scoredSortedSet("key1", 1000L, "value1");
    }

    @Test
    void testScoredSortedSet_Exception() {
        doThrow(new RuntimeException("Redis error")).when(redisClient).scoredSortedSet("key1", 1000L, "value1");
        wrapperWithLog.scoredSortedSet("key1", 1000L, "value1");
    }

    @Test
    void testScoredSortedGet() {
        List<String> expected = Arrays.asList("v1", "v2");
        when(redisClient.scoredSortedGet("key1", 0L, 1000L)).thenReturn(expected);
        assertEquals(expected, wrapperWithLog.scoredSortedGet("key1", 0L, 1000L));
    }

    @Test
    void testScoredSortedGet_Exception() {
        when(redisClient.scoredSortedGet("key1", 0L, 1000L)).thenThrow(new RuntimeException("Redis error"));
        assertNull(wrapperWithLog.scoredSortedGet("key1", 0L, 1000L));
    }

    @Test
    void testScoredSortedRemoveList() {
        wrapperWithLog.scoredSortedRemoveList("key1", 0L, 1000L);
        verify(redisClient).scoredSortedRemoveList("key1", 0L, 1000L);
    }

    @Test
    void testScoredSortedRemove() {
        wrapperWithLog.scoredSortedRemove("key1", "value1");
        verify(redisClient).scoredSortedRemove("key1", "value1");
    }

    @Test
    void testGetAll() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("key1", "value1");
        List<String> keys = Arrays.asList("key1");
        when(redisClient.getAll(keys)).thenReturn(expected);
        assertEquals(expected, wrapperWithLog.getAll(keys));
    }

    @Test
    void testGetAll_Exception() {
        List<String> keys = Arrays.asList("key1");
        when(redisClient.getAll(keys)).thenThrow(new RuntimeException("Redis error"));
        assertNull(wrapperWithLog.getAll(keys));
    }

    @Test
    void testGetLock() {
        RedisLock mockLock = new RedisLock() {
            @Override
            public boolean tryLock(Duration maxWait) {
                return true;
            }

            @Override
            public void unlock() {
            }
        };
        when(redisClient.getLock("lockKey")).thenReturn(mockLock);
        RedisLock lock = wrapperWithLog.getLock("lockKey");
        assertNotNull(lock);
    }

    @Test
    void testGetLock_TryLock() throws Exception {
        RedisLock mockLock = new RedisLock() {
            @Override
            public boolean tryLock(Duration maxWait) {
                return true;
            }

            @Override
            public void unlock() {
            }
        };
        when(redisClient.getLock("lockKey")).thenReturn(mockLock);
        RedisLock lock = wrapperWithLog.getLock("lockKey");
        assertTrue(lock.tryLock(Duration.ofSeconds(1)));
    }

    @Test
    void testGetLock_Unlock() {
        RedisLock mockLock = new RedisLock() {
            @Override
            public boolean tryLock(Duration maxWait) {
                return true;
            }

            @Override
            public void unlock() {
            }
        };
        when(redisClient.getLock("lockKey")).thenReturn(mockLock);
        RedisLock lock = wrapperWithLog.getLock("lockKey");
        lock.unlock();
    }

    @Test
    void testGetAndIncrement() {
        when(redisClient.getAndIncrement("counter", 60)).thenReturn(5L);
        assertEquals(5L, wrapperWithLog.getAndIncrement("counter", 60));
    }

    @Test
    void testGetAndIncrement_Exception() {
        when(redisClient.getAndIncrement("counter", 60)).thenThrow(new RuntimeException("Redis error"));
        assertEquals(0L, wrapperWithLog.getAndIncrement("counter", 60));
    }

    @Test
    void testAddAndGet() {
        when(redisClient.addAndGet("counter", 5, 60)).thenReturn(10L);
        assertEquals(10L, wrapperWithLog.addAndGet("counter", 5, 60));
    }

    @Test
    void testAddAndGet_Exception() {
        when(redisClient.addAndGet("counter", 5, 60)).thenThrow(new RuntimeException("Redis error"));
        assertEquals(0L, wrapperWithLog.addAndGet("counter", 5, 60));
    }

    @Test
    void testDeleteByPrefix() {
        wrapperWithLog.deleteByPrefix("prefix_");
        verify(redisClient).deleteByPrefix("prefix_");
    }

    @Test
    void testDeleteByPrefix_Exception() {
        doThrow(new RuntimeException("Redis error")).when(redisClient).deleteByPrefix("prefix_");
        wrapperWithLog.deleteByPrefix("prefix_");
    }

    @Test
    void testSetAndKeepTtl() {
        Duration duration = Duration.ofMinutes(5);
        wrapperWithLog.setAndKeepTtl("key1", "value1", duration);
        verify(redisClient).setAndKeepTtl("key1", "value1", duration);
    }

    @Test
    void testSetAndKeepTtl_Exception() {
        Duration duration = Duration.ofMinutes(5);
        doThrow(new RuntimeException("Redis error")).when(redisClient).setAndKeepTtl("key1", "value1", duration);
        wrapperWithLog.setAndKeepTtl("key1", "value1", duration);
    }

    @Test
    void testWithoutLog_Exists() {
        when(redisClient.exists("key1")).thenReturn(true);
        assertTrue(wrapperWithoutLog.exists("key1"));
    }

    @Test
    void testWithoutLog_Get() {
        when(redisClient.get("key1")).thenReturn("value1");
        assertEquals("value1", wrapperWithoutLog.get("key1"));
    }

    @Test
    void testWithoutLog_Set() {
        wrapperWithoutLog.set("key1", "value1");
        verify(redisClient).set("key1", "value1");
    }
}
