/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;

/**
 * @Description SseEmitterUTF8
 * @DateTime 2023/6/30 11:25
 * @Version 1.0
 */
public class SseEmitterUTF8 extends SseEmitter {
    public SseEmitterUTF8(Long timeout) {
        super(timeout);
    }

    @Override
    protected void extendResponse(@NotNull ServerHttpResponse outputMessage) {
        super.extendResponse(outputMessage);
        HttpHeaders headers = outputMessage.getHeaders();
        headers.setContentType(new MediaType(MediaType.TEXT_EVENT_STREAM, StandardCharsets.UTF_8));
        headers.setCacheControl("no-cache");
        headers.set("X-Accel-Buffering", "no");
    }
}
