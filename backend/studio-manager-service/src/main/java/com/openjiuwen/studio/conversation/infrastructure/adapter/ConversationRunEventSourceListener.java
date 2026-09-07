/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.model.valueobject.ExecutionRef;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 对话运行 SSE 监听器（团队新协议，Phase 5）：事件原样转发前端，同时缓冲**完整输出边界**——
 * sub_done（子 Agent 完整文本，按 sub_execution_id 分组）、run_done（监督者完整文本）；流关闭/异常时
 * 一次性落库（事务性，全量成功或全部回滚）。
 *
 * <p>落库口径（只在完整输出边界落库）：run_done → t_conversation_run（execution_id 整句）、
 * sub_done → t_conversation_sub_run（sub_execution_id + agent_id 整句）。
 * 增量事件（message/reasoning/usage）、边界事件（run_start/sub_start/tool_call/tool_result）仅透传前端不落库；
 * user_message 不落（user 行已在发送前由 Java 落库，避免重复）。</p>
 *
 * <p>execution_id 由调用方确定（X-Execution-Id 下发引擎，引擎按头回显，2026-08-11 引擎已支持读头），
 * 落库统一使用本轮值，run/sub_run 分组精确。</p>
 */
@Slf4j
public class ConversationRunEventSourceListener extends EventSourceListener {

    private static final String EVENT_SUB_DONE = "sub_done";
    private static final String EVENT_RUN_DONE = "run_done";
    private static final String ROLE_ASSISTANT = "assistant";

    private static final String FIELD_SUB_EXECUTION_ID = "subExecutionId";
    private static final String FIELD_AGENT_ID = "agentId";
    private static final String FIELD_TEXT = "text";

    private final SseEmitter sseEmitter;
    private final CountDownLatch latch;
    private final String conversationId;
    private final String executionId;
    private final String modelDeploymentId;
    private final ConversationRepository conversationRepository;

    /** run_done 完整文本（监督者整轮回答，权威） */
    private String mainAnswer;
    /** sub_execution_id -> {agentId, text}（子 Agent 完整文本，权威） */
    private final Map<String, SubAnswer> subAnswers = new LinkedHashMap<>();
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
        log.info("Conversation team stream open: conversationId={}, executionId={}", conversationId, executionId);
    }

    @Override
    public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type,
                        @NotNull String data) {
        // 1. 原样转发前端（无事件名帧，前端按 data.event 区分）
        try {
            sseEmitter.send(SseEmitter.event().data(data).build());
        } catch (Throwable e) {
            log.warn("SSE send message fail, conversationId={}", conversationId, e);
        }

        // 2. 缓冲完整输出边界（仅 sub_done/run_done；增量/边界事件不落库）
        try {
            JSONObject json = JSON.parseObject(data);
            if (json == null) {
                return;
            }
            JSONObject dataObj = json.getJSONObject("data");
            String event = json.getString("event");
            switch (event == null ? "" : event) {
                case EVENT_SUB_DONE -> {
                    String subExecutionId = dataObj == null ? null : dataObj.getString(FIELD_SUB_EXECUTION_ID);
                    String text = dataObj == null ? null : dataObj.getString(FIELD_TEXT);
                    if (subExecutionId != null && text != null && !text.isBlank()) {
                        subAnswers.put(subExecutionId, new SubAnswer(dataObj.getString(FIELD_AGENT_ID), text));
                    }
                }
                case EVENT_RUN_DONE -> {
                    String text = dataObj == null ? null : dataObj.getString(FIELD_TEXT);
                    if (text != null && !text.isBlank()) {
                        mainAnswer = text;
                    }
                }
                default -> {
                    // user_message/run_start/message/reasoning/tool_call/tool_result/sub_start/usage 仅透传不落
                }
            }
        } catch (Throwable e) {
            log.warn("Failed to parse conversation team event, conversationId={}", conversationId, e);
        }
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        flush();
        try {
            sseEmitter.complete();
        } catch (Exception ignored) {
        }
        log.info("Conversation team stream closed: conversationId={}, executionId={}", conversationId, executionId);
    }

    @Override
    public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
        flush();
        try {
            sseEmitter.completeWithError(t == null ? new RuntimeException("conversation team stream failure") : t);
        } catch (Exception ignored) {
        }
        latch.countDown();
        log.error("Conversation team stream failure, conversationId={}, executionId={}", conversationId, executionId, t);
    }

    /**
     * 整轮结束一次性落库（appendMessages 按 ExecutionRef.subExecutionId 拆分路由，事务性）：
     * run_done → t_conversation_run、sub_done → t_conversation_sub_run。
     */
    private void flush() {
        if (flushed) {
            return;
        }
        flushed = true;
        List<ConversationMessage> rows = new ArrayList<>();
        if (mainAnswer != null && !mainAnswer.isBlank()) {
            rows.add(assistantMessage(null, null, mainAnswer, EVENT_RUN_DONE));
        }
        subAnswers.forEach((subExecutionId, sub) -> {
            if (sub.text != null && !sub.text.isBlank()) {
                rows.add(assistantMessage(subExecutionId, sub.agentId, sub.text, EVENT_SUB_DONE));
            }
        });
        if (!rows.isEmpty()) {
            try {
                conversationRepository.appendMessages(conversationId, rows);
                log.info("Conversation team run persisted: conversationId={}, executionId={}, rows={}",
                    conversationId, executionId, rows.size());
            } catch (Throwable e) {
                log.error("Failed to persist conversation team run, conversationId={}, executionId={}",
                    conversationId, executionId, e);
            }
        }
    }

    private ConversationMessage assistantMessage(String subExecutionId, String agentId, String content, String event) {
        return ConversationMessage.builder()
            .role(ROLE_ASSISTANT)
            .content(content)
            .executionRef(new ExecutionRef(executionId, subExecutionId, agentId))
            .modelDeploymentId(modelDeploymentId)
            .event(event)
            .build();
    }

    /** 子 Agent 完整输出（agentId + text） */
    private static final class SubAnswer {

        private final String agentId;
        private final String text;

        SubAnswer(String agentId, String text) {
            this.agentId = agentId;
            this.text = text;
        }
    }
}
