/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.runtime.http.HttpResponse;
import com.openjiuwen.studio.agent.runtime.http.SseEmitterUTF8;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;
import com.openjiuwen.studio.agent.runtime.service.security.SecurityService;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.runtime.util.TestUtil;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class OpenApiRequestAdapterTest {
    private OpenApiRequestAdapter adapter;

    @BeforeEach
    public void beforeEach() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(1);
        // 设置最大线程数
        executor.setMaxPoolSize(1);
        // 设置队列容量
        executor.setQueueCapacity(1);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(100);
        // 设置默认线程名称
        executor.setThreadNamePrefix("test");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();

        adapter = new OpenApiRequestAdapter();
        ReflectionTestUtils.setField(adapter, "executor", executor);
        ReflectionTestUtils.setField(adapter, "securityCheckExecutor", executor);
        ReflectionTestUtils.setField(adapter, "apiLogService", new RuntimeModelApiLogService());
        ReflectionTestUtils.setField(adapter, "securityService", new SecurityService());
    }

    @Test
    public void requestBodyConvertTest() {
        ChatCompletionRequest body = new ChatCompletionRequest();
        Object obj = adapter.requestBodyConvert(new HashMap<>(), body, false);
        assertEquals(body, obj);
    }

    @Test
    public void resBodyConvertTest() {
        HttpResponse<String> response = new HttpResponse<>();
        try {
            adapter.resBodyConvert("", response);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL, e.getErrorCode());
        }

        response.setResponseBody("{\"usage\":{\"completion_tokens\":100}}");

        try {
            response.setErrorResponseBody("error");
            response.setStatusCode(409);
            adapter.resBodyConvert("", response);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_THIRD_MODEL_FAIL, e.getErrorCode());
        }

        try {
            response.setErrorResponseBody("error");
            response.setStatusCode(200);
            adapter.resBodyConvert("", response);
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_INVOKE_MODEL_SERVICE_FAIL, e.getErrorCode());
        }

        ModelApiLogThreadLocal.setApiLog(new ModelApiLog());
        Object obj = adapter.resBodyConvert("", response);
        assertEquals("{\"usage\":{\"completion_tokens\":100}}", obj.toString());
    }

    @Test
    public void streamResBodyConvertTest() {
        HttpResponse<String> response = new HttpResponse<>();
        response.setStatusCode(200);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "123");
        response.setHeaders(headers);
        try {
            adapter.streamResBodyConvert("", response);
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_THIRD_MODEL_FAIL, e.getErrorCode());
        }

        headers.put("Content-Type", "stream");

        ModelApiLogThreadLocal.setApiLog(new ModelApiLog());

        String content = TestUtil.getStringFromFile("classpath:stream/hello.text");

        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        response.setStreamResponse(inputStream);

        Object obj = adapter.streamResBodyConvert("", response);
        assertInstanceOf(SseEmitterUTF8.class, obj);

        AtomicBoolean finish = new AtomicBoolean(false);

        SseEmitterUTF8 emitter = (SseEmitterUTF8) obj;
        emitter.onCompletion(() -> {
            log.info("finish");
            finish.set(true);
        });

        assertFalse(finish.get());
    }

    @Test
    public void waitSecurityCheckJobTest() throws Exception {
        Map<Integer, Future<String>> jobFutureMap = new HashMap<>();
        jobFutureMap.put(1, CompletableFuture.completedFuture("success"));
        jobFutureMap.put(2, CompletableFuture.completedFuture("success"));
        jobFutureMap.put(3, CompletableFuture.completedFuture("block"));

        adapter.waitSecurityCheckJob(jobFutureMap, new SseEmitterUTF8(100L), new ModelApiLog());
    }

    @Test
    public void submitSecurityCheckJobTest() {
        ModelApiLog apiLog = new ModelApiLog();
        StringBuilder builder = new StringBuilder();
        JSONObject eventData = new JSONObject();

        adapter.submitSecurityCheckJob(1, eventData, builder, apiLog, new HashMap<>());

        apiLog.setResponseVerify(true);
        adapter.submitSecurityCheckJob(1, eventData, builder, apiLog, new HashMap<>());

        eventData = JSONObject.parseObject(
            "{\"id\":\"chat-123\",\"object\":\"chat.completion.chunk\",\"created\":1756433935,\"model\":\"qwen3-235b-a22b\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"test\"},\"logprobs\":null,\"finish_reason\":null}],\"usage\":{\"prompt_tokens\":21,\"total_tokens\":154,\"completion_tokens\":133}}");
        adapter.submitSecurityCheckJob(1, eventData, builder, apiLog, new HashMap<>());
    }
}
