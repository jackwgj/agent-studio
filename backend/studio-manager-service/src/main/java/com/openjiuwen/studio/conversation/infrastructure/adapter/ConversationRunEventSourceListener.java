/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.model.valueobject.ExecutionRef;
import com.openjiuwen.studio.conversation.domain.model.valueobject.ToolRef;
import com.openjiuwen.studio.conversation.domain.repository.ConversationRepository;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import lombok.extern.slf4j.Slf4j;

import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 对话运行 SSE 监听器：事件原样转发前端（与平台代理同一协议），同时缓冲解析——
 * 主 agent 输出缓冲整轮，子 agent 消息（data.nodeType=Agent）按 nodeName 分组缓冲，
 * 工具调用（plugin_end）收集为独立消息行；done/error/关闭时一次性落库（禁止逐 token 分段写入）。
 *
 * <p>execution_id 由调用方确定（X-Execution-Id 下发引擎），事件不携带，落库统一使用本轮值；
 * 事件字段取值（nodeType/nodeName/plugin_end 载荷）以真实运行验证为准（Task 3.4）。</p>
 */
@Slf4j
public class ConversationRunEventSourceListener extends EventSourceListener {

    // TODO 事件类型构建为枚举类型
    private static final String ROLE_ASSISTANT = "assistant";
    private static final String ROLE_TOOL = "tool";
    private static final String EVENT_MESSAGE = "message";
    private static final String EVENT_PLUGIN_END = "plugin_end";
    private static final String EVENT_SUMMARY_RESPONSE = "summary_response";
    private static final String EVENT_DONE = "done";
    private static final String EVENT_END = "end";
    private static final String EVENT_ERROR = "error";
    private static final String EVENT_EXCEPTION = "exception";
    private static final String NODE_TYPE_AGENT = "Agent";

    private final SseEmitter sseEmitter;
    private final CountDownLatch latch;
    private final String conversationId;
    private final String executionId;
    private final String modelDeploymentId;
    private final ConversationRepository conversationRepository;

    private final StringBuilder mainAnswer = new StringBuilder();
    private final Map<String, StringBuilder> subAnswers = new LinkedHashMap<>();
    private final List<ConversationMessage> toolMessages = new ArrayList<>();
    private volatile boolean flushed;

    public ConversationRunEventSourceListener(SseEmitter sseEmitter, CountDownLatch latch, String conversationId,
                                              String executionId, String modelDeploymentId,
                                              ConversationRepository conversationRepository) {
        this.sseEmitter = sseEmitter;
        this.latch = latch;
        this.conversationId = conversationId;
        this.executionId = executionId;
        this.modelDeploymentId = modelDeploymentId;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        latch.countDown();
        log.info("Conversation run stream open: conversationId={}, executionId={}", conversationId, executionId);
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type,
                        @NotNull String data) {
        // 1. 原样转发前端（与平台代理同一协议：无事件名的 JSON 帧，前端按 data.event 区分）
        try {
            sseEmitter.send(SseEmitter.event().data(data).build());
        } catch (Throwable e) {
            log.warn("SSE send message fail, conversationId={}", conversationId, e);
        }

        // 2. 缓冲解析（落库用）
        try {
            JSONObject json = JSON.parseObject(data);
            if (json == null) {
                return;
            }
            String event = json.getString("event");
            JSONObject dataObj = json.getJSONObject("data");
            switch (event == null ? "" : event) {
                case EVENT_MESSAGE -> {
                    // AgentEvent: 根 content；WorkflowRunStreamRsp: data.text + nodeType/nodeName
                    String text = dataObj == null ? null : dataObj.getString("text");
                    String nodeType = dataObj == null ? null : dataObj.getString("nodeType");
                    String nodeName = dataObj == null ? null : dataObj.getString("nodeName");
                    String answer = firstNonBlank(text, json.getString("content"));
                    if (answer == null || answer.isEmpty()) {
                        return;
                    }
                    if (NODE_TYPE_AGENT.equals(nodeType) && !isBlank(nodeName)) {
                        // 子 agent 消息：按 nodeName 分组缓冲
                        subAnswers.computeIfAbsent(nodeName, k -> new StringBuilder()).append(answer);
                    } else {
                        mainAnswer.append(answer);
                    }
                }
                case EVENT_PLUGIN_END -> toolMessages.add(buildToolMessage(json, dataObj));
                case EVENT_SUMMARY_RESPONSE -> {
                    String answer = firstNonBlank(
                        dataObj == null ? null : toText(dataObj.get("answer")), json.getString("content"));
                    if (answer != null && !answer.isEmpty() && mainAnswer.isEmpty()) {
                        mainAnswer.append(answer);
                    }
                }
                case EVENT_DONE -> flush(EVENT_DONE);
                case EVENT_END -> flush(EVENT_DONE);
                case EVENT_ERROR -> flush(EVENT_ERROR);
                case EVENT_EXCEPTION -> flush(EVENT_ERROR);
                default -> {
                    // start/plugin_start/statistic_data/node_* 等事件仅透传，不落库
                }
            }
        } catch (Throwable e) {
            log.warn("Failed to parse conversation run event, conversationId={}", conversationId, e);
        }
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        if (!flushed) {
            flush(EVENT_DONE);
        }
        sseEmitter.complete();
        log.info("Conversation run stream closed: conversationId={}, executionId={}", conversationId, executionId);
    }

    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        sendErrorEvent(t, response);
        if (!flushed) {
            flush(EVENT_ERROR);
        }
        sseEmitter.complete();
        latch.countDown();
        log.error("Conversation run stream failure, conversationId={}, executionId={}", conversationId, executionId,
            t);
    }

    /**
     * 整轮结束一次性落库：主 agent assistant 行 + 子 agent assistant 行（sub_execution_id=nodeName 分组）
     * + 工具调用消息行，execution_id 统一为本轮值
     */
    private void flush(String event) {
        if (flushed) {
            return;
        }
        flushed = true;
        List<ConversationMessage> rows = new ArrayList<>();
        if (!mainAnswer.isEmpty()) {
            rows.add(assistantMessage(null, null, mainAnswer.toString(), event));
        }
        subAnswers.forEach((name, buffer) -> {
            if (!buffer.isEmpty()) {
                rows.add(assistantMessage(name, name, buffer.toString(), event));
            }
        });
        rows.addAll(toolMessages);
        if (!rows.isEmpty()) {
            conversationRepository.appendMessages(conversationId, rows);
            log.info("Conversation run persisted: conversationId={}, executionId={}, rows={}",
                conversationId, executionId, rows.size());
        }
    }

    private ConversationMessage assistantMessage(String agentId, String subExecutionId, String content, String event) {
        return ConversationMessage.builder()
            .role(ROLE_ASSISTANT)
            .content(content)
            .executionRef(new ExecutionRef(executionId, subExecutionId, agentId))
            .modelDeploymentId(modelDeploymentId)
            .event(event)
            .build();
    }

    private ConversationMessage buildToolMessage(JSONObject json, JSONObject dataObj) {
        String toolId = firstNonBlank(
            dataObj == null ? null : dataObj.getString("nodeName"),
            dataObj == null ? null : dataObj.getString("componentId"),
            json.getString("nodeName"));
        String content = firstNonBlank(
            json.getString("content"),
            dataObj == null ? null : toText(dataObj.get("outputs")),
            dataObj == null ? null : toText(dataObj.get("answer")));
        String args = dataObj == null ? null : toText(dataObj.get("inputs"));
        return ConversationMessage.builder()
            .role(ROLE_TOOL)
            .content(content)
            .toolRef(new ToolRef(toolId, args))
            .executionRef(new ExecutionRef(executionId, null, null))
            .event(EVENT_PLUGIN_END)
            .build();
    }

    private void sendErrorEvent(@Nullable Throwable t, @Nullable Response response) {
        try {
            String errorMsg = "Internal error.";
            if (response != null && response.body() != null) {
                String rsp = new String(response.body().bytes(), StandardCharsets.UTF_8);
                errorMsg = rsp.contains("error_code") ? rsp : "HTTP " + response.code() + ": " + rsp;
            } else if (t != null) {
                errorMsg = t.getMessage();
            }
            String errorEvent = String.format(
                "{\"event\":\"error\",\"data\":{\"code\":\"SERVER_INTERNAL_ERROR\",\"message\":\"%s\",\"errorMsg\":\"%s\"}}",
                errorMsg, errorMsg);
            sseEmitter.send(SseEmitter.event().name("error").data(errorEvent).build());
        } catch (Throwable e) {
            log.warn("Failed to send error event to frontend: {}", e.getMessage());
        }
    }

    private String toText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String s) {
            return s;
        }
        return JSON.toJSONString(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
