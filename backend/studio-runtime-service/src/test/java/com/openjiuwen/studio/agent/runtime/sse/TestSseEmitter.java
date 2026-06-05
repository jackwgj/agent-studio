/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.sse;

import lombok.Getter;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestSseEmitter extends SseEmitter {
    @Getter
    private List<String> messages = new ArrayList<>();

    @Override
    public void send(Object object) throws IOException {
        super.send(object);
        messages.add(object.toString());
    }
}
