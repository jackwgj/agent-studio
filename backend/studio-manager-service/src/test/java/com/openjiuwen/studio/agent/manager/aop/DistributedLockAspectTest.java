/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.aop;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.annotation.DistributedLock;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

@MockitoSettings(strictness = Strictness.LENIENT)
class DistributedLockAspectTest {
    @Mock()
    private RedisClient redisClient;

    @InjectMocks
    private DistributedLockAspect distributedLockAspect;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(distributedLockAspect, "redisClient", redisClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_around_should_return_not_null() throws Throwable {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("not_empty");

            RedisLock lock = Mockito.mock(RedisLock.class);
            when(lock.tryLock(Duration.ofSeconds(10))).thenReturn(true);
            when(redisClient.getLock(anyString())).thenReturn(lock);

            ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
            Object object = new Object();
            when(joinPoint.proceed()).thenReturn(object);

            DistributedLock distributedLock = Mockito.mock(DistributedLock.class);
            when(distributedLock.prefix()).thenReturn("not_empty");

            Object result = distributedLockAspect.around(joinPoint, distributedLock);

            assertNotNull(result);
        }
    }


}
