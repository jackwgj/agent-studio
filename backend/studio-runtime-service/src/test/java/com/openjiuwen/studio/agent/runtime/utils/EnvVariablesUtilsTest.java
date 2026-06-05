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
}
