/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("maaSV2ApiRequestAdapter")
public class MaaSV2ApiRequestAdapter extends AbstractRequestAdapter {
    @Override
    public String getName() {
        return "maasv2";
    }

    @Override
    public Object requestBodyConvert(Map<String, String> headers, Object body, boolean stream) {
        if (body instanceof ChatCompletionRequest chat) {
            if (chat.getThinking() == null) {
                return body;
            }
            String type = chat.getThinking().getType();
            if (!"enabled".equals(type) && !"disabled".equals(type)) {
                chat.setThinking(null);
            }
        }
        return body;
    }
}
