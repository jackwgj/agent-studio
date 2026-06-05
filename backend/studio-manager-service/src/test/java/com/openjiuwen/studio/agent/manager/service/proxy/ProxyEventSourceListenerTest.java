/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.proxy;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CountDownLatch;

class ProxyEventSourceListenerTest {
    @Test
    public void onEventTest() {
        SseEmitter emitter = new SseEmitter();
        ProxyEventSourceListener listener = new ProxyEventSourceListener("request", new CountDownLatch(1), emitter);

        EventSource source = new EventSource() {
            @Override
            public @NotNull Request request() {
                return null;
            }

            @Override
            public void cancel() {
            }
        };

        Response rsp = Mockito.mock(Response.class);

        listener.onOpen(source, rsp);

        listener.onEvent(source, "", "", "");
        listener.onClosed(source);
        listener.onEvent(source, "", "", "");
    }

    @Test
    public void onFailureTest() {
        SseEmitter emitter = new SseEmitter();
        ProxyEventSourceListener listener = new ProxyEventSourceListener("request", new CountDownLatch(1), emitter);

        EventSource source = new EventSource() {
            @Override
            public @NotNull Request request() {
                return null;
            }

            @Override
            public void cancel() {
            }
        };

        Response rsp = new Response.Builder().request(new Request.Builder().url("https://demo.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("test", "aaa")
            .body(ResponseBody.create("{}", MediaType.get("application/json")))
            .build();

        listener.onFailure(source, new Exception(), rsp);
        listener.onFailure(source, null, rsp);
        listener = new ProxyEventSourceListener("request", new CountDownLatch(1), emitter);
        listener.onFailure(source, new Exception(), null);
    }
}
