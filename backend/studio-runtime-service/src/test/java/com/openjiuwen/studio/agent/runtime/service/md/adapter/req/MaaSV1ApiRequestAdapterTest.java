/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;

import com.openjiuwen.studio.agent.common.dto.run.Thinking;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class MaaSV1ApiRequestAdapterTest {
    @Test
    public void requestBodyConvertTest() {
        MaaSV1ApiRequestAdapter adapter = new MaaSV1ApiRequestAdapter();
        Assertions.assertEquals("maasv1", adapter.getName());

        Map<String, String> body = new HashMap<>();
        Assertions.assertEquals(body, adapter.requestBodyConvert(null, body, false));

        ChatCompletionRequest request = new ChatCompletionRequest();

        adapter.requestBodyConvert(null, request, false);
        Assertions.assertNull(request.getChatTemplateKwargs());

        request.setThinking(new Thinking());
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertEquals("{enable_thinking=true, thinking=true}", request.getChatTemplateKwargs().toString());

        request.setThinking(new Thinking());
        request.getThinking().setType("enabled");
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertEquals("{enable_thinking=true, thinking=true}", request.getChatTemplateKwargs().toString());

        request.setThinking(new Thinking());
        request.getThinking().setType("disabled");
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertEquals("{enable_thinking=false, thinking=false}", request.getChatTemplateKwargs().toString());
    }
}
