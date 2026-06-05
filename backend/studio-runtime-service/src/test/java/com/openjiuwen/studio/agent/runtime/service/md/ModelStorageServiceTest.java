/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.dto.ModelProps;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelStrategy;
import com.openjiuwen.studio.agent.runtime.enums.ModelStrategyEnum;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.ObsService;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

class ModelStorageServiceTest {
    private static final String PROJECT_ID = "test_project";

    private static final String WORKSPACE = "default";

    private ModelStorageService service;

    private ObsService mockObs;

    private RedisClient mockRedis;

    private static final String MODEL_108_JSON = """
        {
          "type": "model",
          "data": {
            "id": "108",
            "provider_id": "6",
            "service_name": "deepseek-chat",
            "service_key": "publisher:deepseek:deepseek-chat",
            "model_name": "deepseek-chat",
            "model_version": "deepseek-chat",
            "model_type": "LLM",
            "project_id": "test_project",
            "workspace_id": "default"
          }
        }
        """;

    private static final String MODEL_109_JSON = """
        {
          "type": "model",
          "data": {
            "id": "109",
            "provider_id": "6",
            "service_name": "gpt-4",
            "service_key": "publisher:openai:gpt-4",
            "model_name": "gpt-4",
            "model_version": "gpt-4",
            "model_type": "LLM",
            "project_id": "test_project",
            "workspace_id": "default"
          }
        }
        """;

    private static final String ROUTER_JSON = """
        {
          "type": "router",
          "data": {
            "id": "router_test",
            "strategy_name": "eewe",
            "strategy_type": "default",
            "strategy_key": "strategy:default:eewe",
            "service_id_list": "109",
            "auth_id_list": "1005",
            "project_id": "project_mock",
            "workspace_id": "default"
          }
        }
        """;

    @BeforeEach
    public void beforeEach() {
        ModelApiLogThreadLocal.setApiLog(new ModelApiLog());

        mockObs = Mockito.mock(ObsService.class);
        mockRedis = Mockito.mock(RedisClient.class);

        ModelAuthStorageService authStorageService = Mockito.mock(ModelAuthStorageService.class);
        // Mock auth service to return null, indicating no auth available
        Mockito.when(authStorageService.queryProviderAuth(
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
            Mockito.anyBoolean(), Mockito.anyString()))
            .thenReturn(null);

        service = new ModelStorageService();
        ReflectionTestUtils.setField(service, "obsService", mockObs);
        ReflectionTestUtils.setField(service, "redisClient", mockRedis);
        ReflectionTestUtils.setField(service, "authService", authStorageService);

        Mockito.doNothing().when(mockRedis).set(Mockito.anyString(), Mockito.anyString(), Mockito.any(Duration.class));
        Mockito.doNothing().when(mockRedis).set(Mockito.anyString(), Mockito.anyString());

        Mockito.when(mockObs.getObject("model-service/ir/router_test.json")).thenReturn(ROUTER_JSON);
        Mockito.when(mockObs.getObject("model-service/ir/109.json")).thenReturn(MODEL_109_JSON);
        Mockito.when(mockObs.getObject("model-service/ir/108.json")).thenReturn(MODEL_108_JSON);
    }

    @AfterEach
    public void afterEach() {
        ModelApiLogThreadLocal.clear();
    }

    @Test
    public void modelRouterTest() {
        ModelStrategy strategy = service.queryModelStrategy(PROJECT_ID, WORKSPACE, "router_test", null, false);
        Assertions.assertNotNull(strategy);
        Assertions.assertEquals(ModelStrategyEnum.ROUTER, strategy.getType());

        strategy = service.queryModelStrategy(PROJECT_ID, WORKSPACE, "router_test", null, true);
        Assertions.assertNotNull(strategy);
        Assertions.assertEquals(ModelStrategyEnum.ROUTER, strategy.getType());
    }

    @Test
    public void modelTest() {
        ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
        ModelStrategy strategy = service.queryModelStrategy(PROJECT_ID, WORKSPACE, "108", "1005", false);
        Assertions.assertNotNull(strategy);
        Assertions.assertEquals(ModelStrategyEnum.MODEL, strategy.getType());

        strategy = service.queryModelStrategy(PROJECT_ID, WORKSPACE, "108", "1005", true);
        Assertions.assertNotNull(strategy);
        Assertions.assertEquals(ModelStrategyEnum.MODEL, strategy.getType());
    }

    @Test
    public void modelNotExistTest() {
        Mockito.when(mockObs.getObject("model-service/ir/not_exist.json"))
            .thenThrow(new AgentStudioException(StudioError.OBS_FAILED, "resource is not exist!"));

        ModelStrategy strategy = service.queryModelStrategy(PROJECT_ID, WORKSPACE, "not_exist", "1005", false);
        Assertions.assertNull(strategy);
    }

    @Test
    public void queryModelPropsTest() {
        ModelProps props = service.queryModelProps("108");
        Assertions.assertNotNull(props);
        Assertions.assertEquals("108", props.getModelId());

        props = service.queryModelProps("router_test");
        Assertions.assertNotNull(props);
        Assertions.assertEquals("router_test", props.getModelId());
    }
}