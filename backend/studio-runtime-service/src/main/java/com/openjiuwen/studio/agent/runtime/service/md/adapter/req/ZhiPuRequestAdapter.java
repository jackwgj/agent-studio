/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 添加类的描述
 *
 */
@Slf4j
@Component("zhiPuRequestAdapter")
public class ZhiPuRequestAdapter extends AbstractRequestAdapter {
    private static final Set<String> MODELS = Set.of("glm-4-plus", "glm-4-air", "glm-4-airx", "glm-4-flash",
        "glm-4-flashx");

    @Override
    public String getName() {
        return "zhipu";
    }

    @Override
    public Object requestBodyConvert(Map<String, String> headers, Object body, boolean stream) {
        if (body instanceof ChatCompletionRequest chatReq) {
            String model = chatReq.getModel();
            chatReq.setThinking(null);
            if (model.equals("glm-4v-flash")) {
                Integer maxTokens = chatReq.getMaxTokens();
                chatReq.setMaxTokens(maxTokens != null ? Math.min(maxTokens, 1024) : 1024);
            } else if (MODELS.contains(model)) {
                Integer maxTokens = chatReq.getMaxTokens();
                chatReq.setMaxTokens(maxTokens != null ? Math.min(maxTokens, 4095) : 4095);
            }
            return chatReq;
        } else {
            return body;
        }
    }
}
