/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunStreamRsp;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * sse对象处理工具类
 *
 */
@Slf4j
public class SseEmitterUtils {

    public static void sendEvent(SseEmitter sseEmitter, WorkflowRunStreamRsp event) throws IOException {
        if (event.getCreatedTime() == null) {
            event.setCreatedTime(System.currentTimeMillis());
        }
        sseEmitter.send(event, MediaType.APPLICATION_JSON);
    }

    public static Throwable getFailureOnSseEmitter(SseEmitter sseEmitter) {
        Throwable failure = null;
        try {
            Field failureFiled = SseEmitter.class.getSuperclass().getDeclaredField("failure");
            failureFiled.setAccessible(true);
            failure = (Throwable) failureFiled.get(sseEmitter);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            log.error("getFailureOnSseEmitter error: ", exception);
        }
        return failure;
    }

    public static void setFailureOnSseEmitter(SseEmitter sseEmitter, Throwable throwable) {
        try {
            Field failureFiled = SseEmitter.class.getSuperclass().getDeclaredField("failure");
            failureFiled.setAccessible(true);
            failureFiled.set(sseEmitter, throwable);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            log.error("setFailureOnSseEmitter error: ", exception);
        }
    }

    public static Runnable getCompletionOnSseEmitter(SseEmitter sseEmitter, String name) {
        Runnable callback;
        try {
            Field failureFiled = SseEmitter.class.getSuperclass().getDeclaredField(name);
            failureFiled.setAccessible(true);
            callback = (Runnable) failureFiled.get(sseEmitter);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            callback = null;
        }
        return callback;
    }
}
