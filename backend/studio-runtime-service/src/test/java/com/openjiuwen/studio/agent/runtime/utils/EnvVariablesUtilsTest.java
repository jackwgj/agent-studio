/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.service.ObsService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class EnvVariablesUtilsTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsService obsService;

    @InjectMocks
    private EnvVariablesUtils envVariablesUtils;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(envVariablesUtils, "irObsPath", "workflow/ir");
        ReflectionTestUtils.setField(envVariablesUtils, "redisClient", redisClient);
        ReflectionTestUtils.setField(envVariablesUtils, "obsService", obsService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testReadEnv() {
        String workflowId = "workflow123";
        String workspaceId = "workspace123";
        String environmentId = "environment123";
        String version = "123";
        String redisEnvStr = "[{\"name\":\"a\",\"value\":{\"content\":\"12\"}}]";
        String obsEnvStr = "[{\"name\":\"a\",\"value\":{\"content\":\"23\",\"type\":\"number\",\"secret\":true}}]";
        ExecuteParams executeParams = ExecuteParams.builder()
            .debug(true)
            .workflowId(workflowId)
            .workspaceId(workspaceId)
            .environmentId(environmentId)
            .releasedVersion(version)
            .build();
        // 调试版本从 redis 读取
        when(redisClient.get("environment:environment123:workspaceId:workspace123")).thenReturn(redisEnvStr);
        Map<String, Object> wrappedMap = envVariablesUtils.getEnvironmentVariables(executeParams);
        assertNotNull(wrappedMap);
        assertNotNull(wrappedMap.get("plugin_url_params"));
        Map<String, Object> envMap = JsonUtils.objectToClass(wrappedMap.get("plugin_url_params"));
        assertEquals("12", envMap.get("a"));
        // redis 中无缓存
        when(redisClient.get("environment:environment123:workspaceId:workspace123")).thenReturn(null);
        wrappedMap = envVariablesUtils.getEnvironmentVariables(executeParams);
        assertNotNull(wrappedMap);
        assertNotNull(wrappedMap.get("plugin_url_params"));
        envMap = JsonUtils.objectToClass(wrappedMap.get("plugin_url_params"));
        assertEquals(0, envMap.size());
        try (MockedStatic<CryptoUtils> cryptoUtilsMockedStatic = mockStatic(CryptoUtils.class)) {
            cryptoUtilsMockedStatic.when(() -> CryptoUtils.decrypt("23")).thenReturn("34");
            // 发布版本从 obs 读取
            executeParams.setDebug(false);
            envVariablesUtils.init();
            when(obsService.getObject("workflow/ir/workflow123/workflow123_123_environment123_env.json"))
                .thenReturn(obsEnvStr);
            wrappedMap = envVariablesUtils.getEnvironmentVariables(executeParams);
            assertNotNull(wrappedMap);
            assertNotNull(wrappedMap.get("plugin_url_params"));
            envMap = JsonUtils.objectToClass(wrappedMap.get("plugin_url_params"));
            assertEquals(34L, envMap.get("a"));
            // obs中无数据，从内存缓存读取
            when(obsService.getObject("workflow/ir/workflow123/workflow123_123_env.json")).thenReturn(null);
            wrappedMap = envVariablesUtils.getEnvironmentVariables(executeParams);
            assertNotNull(wrappedMap);
            assertNotNull(wrappedMap.get("plugin_url_params"));
            envMap = JsonUtils.objectToClass(wrappedMap.get("plugin_url_params"));
            assertEquals(34L, envMap.get("a"));
        }
    }

    @Test
    void testGetEnvironmentVariablesForAgent_BlankEnvironmentId() {
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
            .environmentId(null)
            .build();
        Map<String, Object> result = envVariablesUtils.getEnvironmentVariables(executeParams);
        assertNotNull(result);
        assertNotNull(result.get("plugin_url_params"));
        Map<String, Object> envMap = JsonUtils.objectToClass(result.get("plugin_url_params"));
        assertEquals(0, envMap.size());
    }

    @Test
    void testGetEnvironmentVariablesForAgent_DebugModeFromRedis() {
        String environmentId = "env123";
        String workspaceId = "ws123";
        String redisEnvStr = "[{\"name\":\"API_KEY\",\"value\":{\"content\":\"secret_value\"}}]";
        when(redisClient.get("environment:env123:workspaceId:ws123")).thenReturn(redisEnvStr);
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
            .debug(true)
            .environmentId(environmentId)
            .workspaceId(workspaceId)
            .build();
        Map<String, Object> result = envVariablesUtils.getEnvironmentVariables(executeParams);
        assertNotNull(result);
        Map<String, Object> envMap = JsonUtils.objectToClass(result.get("plugin_url_params"));
        assertEquals("secret_value", envMap.get("API_KEY"));
    }

    @Test
    void testGetEnvironmentVariablesForAgent_DebugModeRedisBlank() {
        String environmentId = "env123";
        String workspaceId = "ws123";
        when(redisClient.get("environment:env123:workspaceId:ws123")).thenReturn(null);
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
            .debug(true)
            .environmentId(environmentId)
            .workspaceId(workspaceId)
            .build();
        Map<String, Object> result = envVariablesUtils.getEnvironmentVariables(executeParams);
        assertNotNull(result);
        Map<String, Object> envMap = JsonUtils.objectToClass(result.get("plugin_url_params"));
        assertEquals(0, envMap.size());
    }

    @Test
    void testGetEnvironmentVariablesForAgent_ReleaseModeFromObs() {
        String agentId = "agent123";
        String versionId = "v1";
        String environmentId = "env123";
        String obsEnvStr = "[{\"name\":\"PORT\",\"value\":{\"content\":\"8080\",\"type\":\"number\"}}]";
        envVariablesUtils.init();
        when(obsService.getObject("workflow/ir/agent123/agent123_v1_env123_env.json")).thenReturn(obsEnvStr);
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
            .debug(false)
            .agentId(agentId)
            .versionId(versionId)
            .environmentId(environmentId)
            .workspaceId("ws123")
            .build();
        Map<String, Object> result = envVariablesUtils.getEnvironmentVariables(executeParams);
        assertNotNull(result);
        Map<String, Object> envMap = JsonUtils.objectToClass(result.get("plugin_url_params"));
        assertEquals(8080L, envMap.get("PORT"));
    }

    @Test
    void testGetEnvironmentVariablesForAgent_ReleaseModeObsBlank() {
        String agentId = "agent123";
        String versionId = "v1";
        String environmentId = "env123";
        envVariablesUtils.init();
        when(obsService.getObject("workflow/ir/agent123/agent123_v1_env123_env.json")).thenReturn(null);
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
            .debug(false)
            .agentId(agentId)
            .versionId(versionId)
            .environmentId(environmentId)
            .workspaceId("ws123")
            .build();
        Map<String, Object> result = envVariablesUtils.getEnvironmentVariables(executeParams);
        assertNotNull(result);
        Map<String, Object> envMap = JsonUtils.objectToClass(result.get("plugin_url_params"));
        assertEquals(0, envMap.size());
    }

    @Test
    void testGetEnvironmentVariablesForAgent_SecretVariableDecrypted() {
        String environmentId = "env123";
        String workspaceId = "ws123";
        String redisEnvStr = "[{\"name\":\"SECRET_KEY\",\"value\":{\"content\":\"encrypted_val\",\"secret\":true}}]";
        when(redisClient.get("environment:env123:workspaceId:ws123")).thenReturn(redisEnvStr);
        try (MockedStatic<CryptoUtils> cryptoUtilsMockedStatic = mockStatic(CryptoUtils.class)) {
            cryptoUtilsMockedStatic.when(() -> CryptoUtils.decrypt("encrypted_val")).thenReturn("decrypted_val");
            AgentExecuteParams executeParams = AgentExecuteParams.builder()
                .debug(true)
                .environmentId(environmentId)
                .workspaceId(workspaceId)
                .build();
            Map<String, Object> result = envVariablesUtils.getEnvironmentVariables(executeParams);
            Map<String, Object> envMap = JsonUtils.objectToClass(result.get("plugin_url_params"));
            assertEquals("decrypted_val", envMap.get("SECRET_KEY"));
        }
    }

    @Test
    void testGetEnvironmentVariablesForAgent_EmptyContentVariable() {
        String environmentId = "env123";
        String workspaceId = "ws123";
        String redisEnvStr = "[{\"name\":\"EMPTY_VAR\",\"value\":{\"content\":\"\"}}]";
        when(redisClient.get("environment:env123:workspaceId:ws123")).thenReturn(redisEnvStr);
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
            .debug(true)
            .environmentId(environmentId)
            .workspaceId(workspaceId)
            .build();
        Map<String, Object> result = envVariablesUtils.getEnvironmentVariables(executeParams);
        Map<String, Object> envMap = JsonUtils.objectToClass(result.get("plugin_url_params"));
        assertEquals("", envMap.get("EMPTY_VAR"));
    }
}
