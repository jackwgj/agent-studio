/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.sse.TestSseEmitter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class MoonshotRequestAdapterTest {
    @Test
    public void finishHandlerTest() throws IOException {
        MoonshotRequestAdapter adapter = new MoonshotRequestAdapter();

        Assertions.assertEquals("moonshot", adapter.getName());

        String msg
            = "data: {\"created\":1757748829,\"model\":\"moonshot-v1-8k\",\"id\":\"chatcmpl-68c51e5d1e91f778fa9c00de\",\"choices\":[{\"finish_reason\":\"stop\",\"usage\":{\"completion_tokens\":15,\"prompt_tokens\":8,\"total_tokens\":23},\"delta\":{},\"index\":0}],\"system_fingerprint\":\"fpv0_ff52a3ef\",\"request_id\":\"53dc6e762e794d8ab13c27c6b3e90530\",\"object\":\"chat.completion.chunk\"}";

        JSONObject chunk = JSONObject.parseObject(msg.substring(msg.indexOf("{")));

        adapter.finishHandler(new TestSseEmitter(), new ModelApiLog(), msg, null);
        adapter.finishHandler(new TestSseEmitter(), new ModelApiLog(), msg, new JSONObject());

        TestSseEmitter emitter = new TestSseEmitter();

        adapter.finishHandler(emitter, new ModelApiLog(), msg, chunk);

        Assertions.assertEquals(
            " {\"created\":1757748829,\"usage\":{\"completion_tokens\":15,\"prompt_tokens\":8,\"total_tokens\":23},\"model\":\"moonshot-v1-8k\",\"id\":\"chatcmpl-68c51e5d1e91f778fa9c00de\",\"choices\":[{\"finish_reason\":\"stop\",\"delta\":{},\"index\":0}],\"system_fingerprint\":\"fpv0_ff52a3ef\",\"object\":\"chat.completion.chunk\"}",
            emitter.getMessages().get(0));
    }
}
