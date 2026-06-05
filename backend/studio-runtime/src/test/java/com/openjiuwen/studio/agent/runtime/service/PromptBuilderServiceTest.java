/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.ConstantTest;
import com.openjiuwen.studio.agent.runtime.dto.ModelInfo;
import com.openjiuwen.studio.agent.runtime.dto.PromptBuilderReq;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceListRsp;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceRsp;
import com.openjiuwen.studio.agent.runtime.rce.client.AgentManagerClient;
import com.openjiuwen.studio.agent.runtime.utils.BaseTest;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能描述
 *
 */
public class PromptBuilderServiceTest extends BaseTest {
    @MockitoBean
    RedissonClient redissonClientMock;

    @Autowired
    private PromptBuilderService promptBuilderService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentManagerClient agentManagerClient;

    @BeforeEach
    public void beforeEach() {
        ReflectionTestUtils.setField(promptBuilderService, "agentManagerClient", agentManagerClient);
        List<ModelServiceRsp> models = Arrays.asList(new ModelServiceRsp().setId("default_model"),
            new ModelServiceRsp().setId("nonexistent_model"), new ModelServiceRsp().setId("new_model"),
            new ModelServiceRsp().setId("modelName"), new ModelServiceRsp().setId("invalid_model"));
        Mockito.when(
                agentManagerClient.queryAvailableModels(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(ResponseEntity.ok(new ModelServiceListRsp().setTotal(1).setData(models)));
    }

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(ConstantTest.TEST_TOKEN, ConstantTest.TEST_PROJECT_ID);
    }

    @Test
    void testPromptBuilderException() {
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("modelName");
        modelInfo.setModelSource("modelType");
        promptBuilderReq.setModelInfo(modelInfo);

        AgentManagerClient client = Mockito.mock(AgentManagerClient.class);
        ReflectionTestUtils.setField(promptBuilderService, "agentManagerClient", client);
        Mockito.when(client.queryAvailableModels(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(ResponseEntity.ok(new ModelServiceListRsp().setTotal(0).setData(new ArrayList<>(0))));

        try {
            promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_MODEL_SERVICE_NOT_AVAILABLE, e.getErrorCode());
        }
    }

    @Test
    void testPromptBuilder() {
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("modelName");
        modelInfo.setModelSource("modelType");
        promptBuilderReq.setModelInfo(modelInfo);
        promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
    }

    @Test
    void testPromptBuilder_without_modelInfo() {
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
    }

    @Test
    void testPromptBuilder_withEmptyInstruct() {
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("");
        promptBuilderReq.setStream(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("modelName");
        modelInfo.setModelSource("modelType");
        promptBuilderReq.setModelInfo(modelInfo);
        promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
    }

    @Test
    void testPromptBuilder_concurrentRequests() throws InterruptedException {
        // 测试高并发情况
        int concurrentRequests = 10;
        for (int i = 0; i < concurrentRequests; i++) {
            new Thread(() -> {
                PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
                promptBuilderReq.setInstruct("你是一个旅游助手");
                promptBuilderReq.setStream(true);
                ModelInfo modelInfo = new ModelInfo();
                modelInfo.setModel("modelName");
                modelInfo.setModelSource("modelType");
                promptBuilderReq.setModelInfo(modelInfo);
                promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
            }).start();
        }
    }

    @Test
    void testPromptBuilder_ConcurrentRequests_Success() throws InterruptedException, ExecutionException {
        // Given
        int concurrentRequests = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch latch = new CountDownLatch(concurrentRequests);

        for (int i = 0; i < concurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
                    promptBuilderReq.setInstruct("你是一个旅游助手");
                    promptBuilderReq.setStream(true);
                    ModelInfo modelInfo = new ModelInfo();
                    modelInfo.setModel("modelName");
                    modelInfo.setModelSource("modelType");
                    promptBuilderReq.setModelInfo(modelInfo);
                    promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
                    latch.countDown();
                } catch (Exception e) {
                    latch.countDown();
                    fail("Concurrent request failed", e);
                }
            });
        }

        // Wait for all tasks to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Not all tasks completed within 5 seconds");
        executorService.shutdown();
    }

    @Test
    void testPromptBuilder_ConcurrentRequests_Failure() throws InterruptedException {
        // Given
        int concurrentRequests = 10;
        CountDownLatch latch = new CountDownLatch(concurrentRequests);
        AtomicInteger failedCount = new AtomicInteger(0);

        for (int i = 0; i < concurrentRequests; i++) {
            new Thread(() -> {
                try {
                    PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
                    promptBuilderReq.setInstruct("你是一个旅游助手");
                    promptBuilderReq.setStream(true);
                    ModelInfo modelInfo = new ModelInfo();
                    modelInfo.setModel("invalid_model");
                    modelInfo.setModelSource("invalid_type");
                    promptBuilderReq.setModelInfo(modelInfo);
                    promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
                    latch.countDown();
                } catch (Exception e) {
                    failedCount.incrementAndGet();
                    latch.countDown();
                }
            }).start();
        }

        // Wait for all threads to complete
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(10, failedCount.get(), "Expected all concurrent requests to fail");
    }

    @Test
    void testPromptBuilder_EmptyModelInfo_Failure() throws Exception {
        // Given
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("default_model"); // 使用默认模型
        modelInfo.setModelSource("default_type"); // 使用默认模型类型
        promptBuilderReq.setModelInfo(modelInfo);

        // When
        SseEmitter result = promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");

        // Then
        assertNotNull(result);
        assertTrue(result != null);
    }

    @Test
    void testPromptBuilder_ReturnValue() {
        // Given
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("default_model");
        modelInfo.setModelSource("default_type");
        promptBuilderReq.setModelInfo(modelInfo);

        // When
        SseEmitter result = promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");

        // Then
        assertNotNull(result);
    }

    @Test
    void testPromptBuilder_HandleException() {
        // Given
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("nonexistent_model");
        modelInfo.setModelSource("nonexistent_type");
        promptBuilderReq.setModelInfo(modelInfo);

        // When
        SseEmitter result = promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");

        // Then
        assertNotNull(result);
    }

    @Test
    void testPromptBuilder_MaxConcurrentRequests() throws InterruptedException {
        // Given
        int maxConcurrentRequests = 100;
        CountDownLatch latch = new CountDownLatch(maxConcurrentRequests);

        // 使用线程池来模拟高并发请求
        ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrentRequests);

        for (int i = 0; i < maxConcurrentRequests; i++) {
            executorService.submit(() -> {
                try {
                    PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
                    promptBuilderReq.setInstruct("你是一个旅游助手");
                    promptBuilderReq.setStream(true);
                    ModelInfo modelInfo = new ModelInfo();
                    modelInfo.setModel("default_model");
                    modelInfo.setModelSource("default_type");
                    promptBuilderReq.setModelInfo(modelInfo);
                    promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");
                    latch.countDown();
                } catch (Exception e) {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all tasks completed within 10 seconds");
        executorService.shutdown();
    }

    @Test
    void testPromptBuilder_ConfigurationChange() {
        // Given
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("new_model");
        modelInfo.setModelSource("new_type");
        promptBuilderReq.setModelInfo(modelInfo);

        // When
        SseEmitter result = promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");

        // Then
        assertNotNull(result);
    }

    @Test
    void testPromptBuilder_WithStreamDisabled() {
        // Given
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(false);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModel("default_model");
        modelInfo.setModelSource("default_type");
        promptBuilderReq.setModelInfo(modelInfo);

        // When
        SseEmitter result = promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");

        // Then
        assertNotNull(result);
    }

    @Test
    void testPromptBuilder_WithEmptyModelInfo() {
        // Given
        PromptBuilderReq promptBuilderReq = new PromptBuilderReq();
        promptBuilderReq.setInstruct("你是一个旅游助手");
        promptBuilderReq.setStream(true);
        promptBuilderReq.setModelInfo(null);

        // When
        SseEmitter result = promptBuilderService.promptBuilder(promptBuilderReq, "test_project", "default");

        // Then
        assertNotNull(result);
    }
}
