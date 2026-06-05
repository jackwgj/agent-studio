/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;


import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;

import com.openjiuwen.studio.agent.common.dto.run.Thinking;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaaSV2ApiRequestAdapterTest {
    @Test
    public void test() {
        Assertions.assertEquals("maasv2", new MaaSV2ApiRequestAdapter().getName());
    }

    @Test
    public void requestBodyConvertTest(){
        MaaSV2ApiRequestAdapter adapter = new MaaSV2ApiRequestAdapter();

        ChatCompletionRequest request = new ChatCompletionRequest();
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertNull(request.getThinking());

        request.setThinking(new Thinking());
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertNull(request.getThinking());

        request.setThinking(new Thinking());
        request.getThinking().setType("");
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertNull(request.getThinking());

        request.setThinking(new Thinking());
        request.getThinking().setType("disabled");
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertEquals("disabled", request.getThinking().getType());

        request.setThinking(new Thinking());
        request.getThinking().setType("enabled");
        adapter.requestBodyConvert(null, request, false);
        Assertions.assertEquals("enabled", request.getThinking().getType());
    }
}
