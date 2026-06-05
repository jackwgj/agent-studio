/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.openjiuwen.studio.agent.common.dto.run.Thinking;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class QwenRequestAdapterTest {
    @Test
    public void requestBodyConvertTest() {
        Assertions.assertEquals("qwen", new QwenRequestAdapter().getName());

        QwenRequestAdapter adapter = new QwenRequestAdapter();
        Map<String, Object> body = new HashMap<>();
        Object ans = adapter.requestBodyConvert(new HashMap<>(), body, false);
        Assertions.assertEquals(body, ans);

        ans = adapter.requestBodyConvert(new HashMap<>(), body, true);
        Assertions.assertEquals("{stream_options={include_usage=true}}", ans.toString());

        ChatCompletionRequest request = new ChatCompletionRequest();
        Object bd = adapter.requestBodyConvert(new HashMap<>(), request, true);
        Assertions.assertEquals("{\"stream_options\":{\"include_usage\":true}}", JsonUtils.encode(bd));

        request.setThinking(new Thinking());
        bd = adapter.requestBodyConvert(new HashMap<>(), request, true);
        Assertions.assertEquals("{\"stream_options\":{\"include_usage\":true},\"enable_thinking\":true}", JsonUtils.encode(bd));

        request.setThinking(new Thinking());
        request.getThinking().setType("disabled");
        bd = adapter.requestBodyConvert(new HashMap<>(), request, true);
        Assertions.assertEquals("{\"stream_options\":{\"include_usage\":true},\"enable_thinking\":false}", JsonUtils.encode(bd));
    }
}
