/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.runtime.dto.md.EmbeddingRequest;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;
import com.openjiuwen.studio.agent.runtime.sse.TestSseEmitter;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 添加类的描述
 *
 */
@Slf4j
public class BaiChuanRequestAdapterTest {
    private BaiChuanRequestAdapter adapter;

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

        adapter = new BaiChuanRequestAdapter();
        ReflectionTestUtils.setField(adapter, "executor", executor);
        ReflectionTestUtils.setField(adapter, "apiLogService", new RuntimeModelApiLogService());
    }

    @Test
    public void requestBodyConvertTest() {
        Map<String, Object> body = new HashMap<>();
        body.put("frequency_penalty", 0);
        @SuppressWarnings("unchecked") Map<String, Object> obj = (Map<String, Object>) adapter.requestBodyConvert(
            new HashMap<>(), body, false);
        assertEquals(1.0, obj.get("frequency_penalty"));
    }

    @Test
    public void requestBodyConvertTest2() {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setFrequencyPenalty(0f);
        request = (ChatCompletionRequest) adapter.requestBodyConvert(new HashMap<>(), request, false);
        assertEquals(1.0f, request.getFrequencyPenalty());

        request.setFrequencyPenalty(3f);
        request = (ChatCompletionRequest) adapter.requestBodyConvert(new HashMap<>(), request, false);
        assertEquals(2.5f, request.getFrequencyPenalty());

        EmbeddingRequest embedding = new EmbeddingRequest();
        Object ans = adapter.requestBodyConvert(new HashMap<>(), embedding, false);
        assertEquals(ans, embedding);
    }

    @Test
    public void finishHandlerTest() throws IOException {
        String msg
            = "data: {\"created\":1757640729,\"usage\":{\"completion_tokens\":9,\"prompt_tokens\":79,\"total_tokens\":88,\"search_count\":0},\"model\":\"Baichuan3-Turbo\",\"id\":\"chatcmpl-M870B029C9W9oF8\",\"choices\":[{\"finish_reason\":\"stop\",\"delta\":{\"role\":\"assistant\",\"content\":\"？\"},\"index\":0}],\"request_id\":\"196ca80feca944c5a16525c53705d45d\",\"object\":\"chat.completion.chunk\"}";
        JSONObject chunk = JSONObject.parseObject(msg.substring("data: ".length()));
        TestSseEmitter emitter = new TestSseEmitter();

        adapter.finishHandler(emitter, new ModelApiLog(), msg, chunk);
        List<String> events = emitter.getMessages();
        Assertions.assertEquals(2, events.size());
        Assertions.assertEquals(
            " {\"created\":1757640729,\"usage\":{\"completion_tokens\":9,\"prompt_tokens\":79,\"total_tokens\":88,\"search_count\":0},\"model\":\"Baichuan3-Turbo\",\"id\":\"chatcmpl-M870B029C9W9oF8\",\"choices\":[],\"object\":\"chat.completion.chunk\"}",
            events.get(1));
    }
}
