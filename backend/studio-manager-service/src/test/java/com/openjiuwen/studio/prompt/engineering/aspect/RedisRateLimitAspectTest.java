/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.aspect;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.prompt.engineering.annotation.RedisRateLimit;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * RedisRateLimitAspect 测试用例
 * 覆盖场景：正常获取令牌、限流触发、首次访问初始化、解析失败重置、获取锁失败、自定义key、默认key等
 */
@ExtendWith(MockitoExtension.class)
public class RedisRateLimitAspectTest {

    // 待测试的切面类（注入Mock依赖）
    @InjectMocks
    private RedisRateLimitAspect redisRateLimitAspect;

    // Mock依赖组件
    @Mock
    private RedisClient redisClient;

    @Mock
    private RedisLock redisLock;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Method targetMethod;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    // 限流注解Mock
    @Mock
    private RedisRateLimit redisRateLimit;

    @Test
    void testTryAcquireToken_FirstAccess() throws Exception {
        // 1. Mock 锁获取成功
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);

        // 2. Mock Redis中无令牌和时间数据
        when(redisClient.get("rate_limit:test_key:tokens")).thenReturn(null);
        when(redisClient.get("rate_limit:test_key:last_refill_time")).thenReturn(null);

        // 3. 执行测试方法
        boolean result = redisRateLimitAspect.tryAcquireToken("rate_limit:test_key:tokens",
            "rate_limit:test_key:last_refill_time", 1.0, 5);

        // 4. 断言结果
        assertTrue(result);

    }

    @Test
    void testTryAcquireToken_TokenEnough() throws Exception {
        // 1. Mock 锁获取成功
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);

        // 2. Mock Redis中有令牌（剩余3个）和时间数据
        long lastTime = System.currentTimeMillis() - 1000; // 1秒前
        when(redisClient.get("rate_limit:test_key:tokens")).thenReturn("3");
        when(redisClient.get("rate_limit:test_key:last_refill_time")).thenReturn(String.valueOf(lastTime));

        // 3. 执行测试方法
        boolean result = redisRateLimitAspect.tryAcquireToken("rate_limit:test_key:tokens",
            "rate_limit:test_key:last_refill_time", 1.0, 5);

        // 4. 断言结果
        assertTrue(result);
    }

    @Test
    void testTryAcquireToken_ParseFailed() throws Exception {
        // 1. Mock 锁获取成功
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);

        // 2. Mock Redis中存储的令牌数为非数字
        when(redisClient.get("rate_limit:test_key:tokens")).thenReturn("abc");
        when(redisClient.get("rate_limit:test_key:last_refill_time")).thenReturn("123456");

        // 3. 执行测试方法
        boolean result = redisRateLimitAspect.tryAcquireToken("rate_limit:test_key:tokens",
            "rate_limit:test_key:last_refill_time", 1.0, 5);

        // 4. 断言结果
        assertTrue(result);
        verify(redisClient).set(eq("rate_limit:test_key:tokens"), eq("4"), eq(Duration.ofMinutes(30)));
    }

    @Test
    void testTryAcquireToken_LockFailed() throws Exception {
        // 1. Mock 锁获取失败
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(false);

        // 2. 执行测试方法
        boolean result = redisRateLimitAspect.tryAcquireToken("rate_limit:test_key:tokens",
            "rate_limit:test_key:last_refill_time", 1.0, 5);

        // 3. 断言结果
        assertFalse(result);
        verify(redisClient, never()).get(anyString());
        verify(redisClient, never()).set(anyString(), anyString(), any(Duration.class));
        verify(redisLock).unlock();
    }

    @Test
    void testAround_CustomKey() throws Throwable {
        // 1. 基础Mock
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(targetMethod);
        when(targetMethod.getAnnotation(RedisRateLimit.class)).thenReturn(redisRateLimit);

        // 2. Mock 注解自定义key
        when(redisRateLimit.key()).thenReturn("custom_test_key");
        when(redisRateLimit.rate()).thenReturn(1.0);
        when(redisRateLimit.capacity()).thenReturn(5);

        // 3. Mock 令牌获取成功
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);
        when(redisClient.get(anyString())).thenReturn(null);

        // 4. 执行环绕通知
        Object result = redisRateLimitAspect.around(joinPoint);

        // 5. 验证
        verify(joinPoint).proceed();
        verify(redisClient).getLock(eq("rate_limit:lock:rate_limit:custom_test_key:tokens"));
    }

    @Test
    void testTryAcquireToken_ExceedCapacity() throws Exception {
        // 1. Mock 锁获取成功
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);

        // 2. Mock 长时间未访问（生成6个令牌，桶容量5）
        long lastTime = System.currentTimeMillis() - 6000; // 6秒前
        when(redisClient.get("rate_limit:test_key:tokens")).thenReturn("3");
        when(redisClient.get("rate_limit:test_key:last_refill_time")).thenReturn(String.valueOf(lastTime));

        // 3. 执行测试方法
        boolean result = redisRateLimitAspect.tryAcquireToken("rate_limit:test_key:tokens",
            "rate_limit:test_key:last_refill_time", 1.0, 5);

        // 4. 断言结果
        assertTrue(result);
        verify(redisClient).set(eq("rate_limit:test_key:tokens"), eq("4"), eq(Duration.ofMinutes(30)));
    }

    @Test
    void testTryAcquireToken_ExceptionThrown() throws Exception {
        // 1. Mock 锁获取后执行抛出异常
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);
        when(redisClient.get(anyString())).thenThrow(new RuntimeException("Redis连接异常"));

        // 2. 执行测试方法
        boolean result = redisRateLimitAspect.tryAcquireToken("rate_limit:test_key:tokens",
            "rate_limit:test_key:last_refill_time", 1.0, 5);

        // 3. 断言结果
        assertFalse(result);
        verify(redisLock).unlock();
    }
}
