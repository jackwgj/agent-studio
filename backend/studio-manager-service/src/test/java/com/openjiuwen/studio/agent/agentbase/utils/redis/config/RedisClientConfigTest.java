/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.utils.redis.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.agent.common.redis.config.RedisClientConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class RedisClientConfigTest {
    @InjectMocks
    private RedisClientConfig redisClientConfig;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(redisClientConfig, "redisHost", "not_empty");
        ReflectionTestUtils.setField(redisClientConfig, "redisPort", 0);
        ReflectionTestUtils.setField(redisClientConfig, "redisPassword", "not_empty");
        ReflectionTestUtils.setField(redisClientConfig, "connectionMinimumIdleSize", 0);
        ReflectionTestUtils.setField(redisClientConfig, "connectionPoolSize", 0);
        ReflectionTestUtils.setField(redisClientConfig, "nodeAddrList", "not_empty");
        ReflectionTestUtils.setField(redisClientConfig, "masterName", "not_empty");
        ReflectionTestUtils.setField(redisClientConfig, "sentinelPorts", "not_empty");
        ReflectionTestUtils.setField(redisClientConfig, "targetPorts", "not_empty");
        ReflectionTestUtils.setField(redisClientConfig, "clusterNodeList", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getRedisHost_should_equal_result() throws Exception {

        // When
        String result = redisClientConfig.getRedisHost();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getRedisPort_should_equal_result() throws Exception {

        // When
        int result = redisClientConfig.getRedisPort();

        // Then
        assertEquals(0, result);
    }

    @Test
    void test_getRedisPassword_should_equal_result() throws Exception {

        // When
        String result = redisClientConfig.getRedisPassword();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getConnectionMinimumIdleSize_should_equal_result() throws Exception {

        // When
        int result = redisClientConfig.getConnectionMinimumIdleSize();

        // Then
        assertEquals(0, result);
    }

    @Test
    void test_getConnectionPoolSize_should_equal_result() throws Exception {

        // When
        int result = redisClientConfig.getConnectionPoolSize();

        // Then
        assertEquals(0, result);
    }

    @Test
    void test_getNodeAddrList_should_equal_result() throws Exception {

        // When
        String result = redisClientConfig.getNodeAddrList();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getMasterName_should_equal_result() throws Exception {

        // When
        String result = redisClientConfig.getMasterName();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getSentinelPorts_should_equal_result() throws Exception {

        // When
        String result = redisClientConfig.getSentinelPorts();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getTargetPorts_should_equal_result() throws Exception {

        // When
        String result = redisClientConfig.getTargetPorts();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_getClusterNodeList_should_equal_result() throws Exception {

        // When
        String result = redisClientConfig.getClusterNodeList();

        // Then
        assertEquals("not_empty", result);
    }

    @Test
    void test_setRedisHost_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setRedisHost("not_empty");
        });
    }

    @Test
    void test_setRedisPort_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setRedisPort(0);
        });
    }

    @Test
    void test_setRedisPassword_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setRedisPassword("not_empty");
        });
    }

    @Test
    void test_setConnectionMinimumIdleSize_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setConnectionMinimumIdleSize(0);
        });
    }

    @Test
    void test_setConnectionPoolSize_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setConnectionPoolSize(0);
        });
    }

    @Test
    void test_setNodeAddrList_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setNodeAddrList("not_empty");
        });
    }

    @Test
    void test_setMasterName_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setMasterName("not_empty");
        });
    }

    @Test
    void test_setSentinelPorts_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setSentinelPorts("not_empty");
        });
    }

    @Test
    void test_setTargetPorts_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setTargetPorts("not_empty");
        });
    }

    @Test
    void test_setClusterNodeList_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            redisClientConfig.setClusterNodeList("not_empty");
        });
    }

}