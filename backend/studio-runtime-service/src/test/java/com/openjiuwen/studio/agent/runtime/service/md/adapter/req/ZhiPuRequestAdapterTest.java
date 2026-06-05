/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * 添加类的描述
 *
 */
@Slf4j
public class ZhiPuRequestAdapterTest {
    private RequestAdapter adapter;

    @BeforeEach
    public void beforeEach() {
        adapter = new ZhiPuRequestAdapter();
    }

    @Test
    public void requestBodyConvertTest() {
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();

        chatCompletionRequest.setModel("glm-4v-flash");
        chatCompletionRequest.setMaxTokens(null);
        ChatCompletionRequest obj = (ChatCompletionRequest) adapter.requestBodyConvert(new HashMap<>(),
            chatCompletionRequest, false);
        assertEquals(1024, obj.getMaxTokens());

        chatCompletionRequest.setMaxTokens(2048);
        ChatCompletionRequest obj0 = (ChatCompletionRequest) adapter.requestBodyConvert(new HashMap<>(),
            chatCompletionRequest, false);
        assertEquals(1024, obj0.getMaxTokens());

        chatCompletionRequest.setModel("glm-4-flash");
        chatCompletionRequest.setMaxTokens(4096);
        ChatCompletionRequest obj1 = (ChatCompletionRequest) adapter.requestBodyConvert(new HashMap<>(),
            chatCompletionRequest, false);
        assertEquals(4095, obj1.getMaxTokens());

        chatCompletionRequest.setMaxTokens(null);
        ChatCompletionRequest obj2 = (ChatCompletionRequest) adapter.requestBodyConvert(new HashMap<>(),
            chatCompletionRequest, false);
        assertEquals(4095, obj2.getMaxTokens());

        chatCompletionRequest.setModel("glm-4.5");
        chatCompletionRequest.setMaxTokens(65536);
        ChatCompletionRequest obj3 = (ChatCompletionRequest) adapter.requestBodyConvert(new HashMap<>(),
            chatCompletionRequest, false);
        assertEquals(65536, obj3.getMaxTokens());
    }
}
