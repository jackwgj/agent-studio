/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.proxy;

import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.dto.ErrorRsp;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ProxyEventSourceListener extends EventSourceListener {
    private static final String REQUEST_ID = "request-id";

    private final SseEmitter sseEmitter;

    private final String requestId;

    private boolean openConnect;

    @Getter
    private ResponseEntity<Object> errorRsp;

    private final CountDownLatch latch;

    public ProxyEventSourceListener(String requestId, CountDownLatch latch, SseEmitter sseEmitter) {
        this.requestId = requestId;
        this.latch = latch;
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        sseEmitter.complete();
        log.info("ProxyEventSourceListener close.");
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type,
        @NotNull String data) {
        try {
            sseEmitter.send(SseEmitter.event().data(data).build());
        } catch (Throwable e) {
            log.error("SSE send message fail.", e);
        }
    }

    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        try {
            MDC.put(REQUEST_ID, requestId);
            this.errorRsp = createErrorRsp(t, response);
            this.openConnect = true;
            log.error("Fail handler stream event.", t);
            sseEmitter.complete();
        } catch (Throwable e) {
            log.warn("Fail handler exception. {}", e.getMessage());
        } finally {
            latch.countDown();
        }
    }

    private ResponseEntity<Object> createErrorRsp(Throwable throwable, Response response) throws IOException {
        if (this.openConnect) {
            log.error("Fail handler request.", throwable);
            return null;
        }
        if (response != null) {
            String rsp = response.body() == null ? "{}" : new String(response.body().bytes(), StandardCharsets.UTF_8);

            log.error("Request fail. rsp:{}", rsp);
            return ResponseEntity.status(response.code()).body(JsonUtils.decode(rsp, Object.class));
        }
        log.error("Fail handler request.", throwable);
        return ResponseEntity.status(500).body(new ErrorRsp().setErrorMsg("Internal error."));
    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        this.openConnect = true;
        MDC.put(REQUEST_ID, requestId);
        latch.countDown();
        log.info("Stream request open.");
    }
}
