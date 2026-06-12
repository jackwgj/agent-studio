/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.environment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.entity.EnvironmentManagerEntity;
import com.openjiuwen.studio.agent.manager.entity.EnvironmentVariableEntity;
import com.openjiuwen.studio.agent.manager.mapper.EnvironmentManagerMapper;
import com.openjiuwen.studio.agent.manager.mapper.EnvironmentVariableMapper;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EnvironmentCacheUtilTest {

    private static final String PROJECT_ENVIRONMENT = "environment:%s:workspaceId:%s";

    @Mock
    private EnvironmentManagerMapper environmentManagerMapper;

    @Mock
    private EnvironmentVariableMapper environmentVariableMapper;

    @Mock
    private RedisClient redisClient;

    @InjectMocks
    private EnvironmentCacheUtil environmentCacheUtil;

    @Mock
    private RedisLock redisLock;

    @Test
    void testUpdateEnvironmentCache() {
        doNothing().when(redisClient).set(anyString(), anyString(), any(Duration.class));
        environmentCacheUtil.updateEnvironmentCache("dev", Constants.TEST_WORKSPACE_ID, null);
        environmentCacheUtil.updateEnvironmentCache("dev", Constants.TEST_WORKSPACE_ID,"");
        environmentCacheUtil.updateEnvironmentCache("dev", Constants.TEST_WORKSPACE_ID,"{\"key\": \"value\"}");
    }

    @Test
    void testDeleteEnvironmentCache() {
        when(redisClient.delete(anyString())).thenReturn(true);
        environmentCacheUtil.deleteEnvironmentCache("project-123", "dev");
        verify(redisClient).delete(eq(String.format(PROJECT_ENVIRONMENT, "project-123", "dev")));
    }

    @Test
    void testIsExistEnvironmentVariableCache() {
        when(redisClient.exists(anyString())).thenReturn(true);
        environmentCacheUtil.isExistEnvironmentVariableCache("env-123", "dev");
        verify(redisClient).exists(eq(String.format(PROJECT_ENVIRONMENT, "env-123", "dev")));
    }

    @Test
    void testRefreshCachedEnvironmentVariables() throws Exception {
        // ===== 分支1：成功拿到锁，正常刷新 =====
        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);

        EnvironmentManagerEntity entity = new EnvironmentManagerEntity();
        entity.setId("e1");
        entity.setName("env1");

        when(environmentManagerMapper.findAllWithGroupByProjectId()).thenReturn(List.of("p1"));
        when(environmentManagerMapper.findByProjectId("p1")).thenReturn(List.of(entity));
        EnvironmentVariableEntity environmentVariableEntity = new EnvironmentVariableEntity();
        environmentVariableEntity.setEnvVariable("{\"k\":\"v\"}");
        environmentVariableEntity.setEnvId("env_id");
        environmentVariableEntity.setWorkspaceId("workspace_id");
        when(environmentVariableMapper.findByProjectIdAndEnvId(any(), any())).thenReturn(List.of(environmentVariableEntity));

        environmentCacheUtil.refreshCachedEnvironmentVariables();

        verify(redisClient).set(eq(String.format(PROJECT_ENVIRONMENT, "env_id", "workspace_id")), eq("{\"k\":\"v\"}"),
            eq(Duration.ofDays(1)));
        // unlock 必须被调用
        verify(redisLock, times(1)).unlock();

        // ===== 分支2：获取不到锁 =====
        reset(redisClient, redisLock, environmentManagerMapper);

        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(false);

        environmentCacheUtil.refreshCachedEnvironmentVariables();

        // 数据库查询不应该执行
        verify(environmentManagerMapper, never()).findAllWithGroupByProjectId();
        // 仍然会调用 unlock()
        verify(redisLock, times(1)).unlock();

        // ===== 分支3：执行中异常 =====
        reset(redisClient, redisLock, environmentManagerMapper);

        when(redisClient.getLock(anyString())).thenReturn(redisLock);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(true);
        when(environmentManagerMapper.findAllWithGroupByProjectId()).thenThrow(new RuntimeException("DB error"));

        assertThrows(AgentStudioException.class, () -> environmentCacheUtil.refreshCachedEnvironmentVariables());

        // unlock 必须被调用，即使发生异常
        verify(redisLock, times(1)).unlock();
    }
}