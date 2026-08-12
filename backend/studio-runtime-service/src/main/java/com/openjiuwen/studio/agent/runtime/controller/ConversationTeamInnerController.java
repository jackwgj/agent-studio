/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.runtime.dto.ConversationTeamReq;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 团队对话转发端点：manager 直传团队参数，SSE 透传引擎 /v1/conversation/team。
 *
 * <p>纯转发（无 IR 生成、无事件解析）：引擎按 subAgentIds 加载各子 Agent 已有 IR 动态组装监督者 + handoff 工具。
 * 子 Agent 纯无状态（方案 B）；Java 不传 systemPrompt（监督者提示词固定引擎侧）。
 * 事件原样透传 manager，由 manager 监听器按新协议分流落三表（Phase 3）。
 *
 * <p>⚠️ DEPRECATED（2026-08-12）：官方 dev 架构已移除独立 Java runtime 层
 * （studio-runtime-service 不在 backend reactor，本模块已成孤儿）。manager 将改为直连引擎
 * 31014 的 {@code /v1/conversation/team}，本转发控制器不再需要。待 manager→引擎直连链路接通
 * 验证后删除（连同 DTO {@code ConversationTeamReq}）。</p>
 */
@Deprecated
@Validated
@RestController
@Slf4j
public class ConversationTeamInnerController {

    private static final String TEAM_API = "/v1/conversation/team";
    private static final long SSE_TIMEOUT_MS = 900_000L;
    private static final long CONNECT_TIMEOUT_SECONDS = 30L;

    @Resource
    private OkHttpUtils okHttpUtils;

    @Value("${jiuwen.base-url}")
    private String jiuwenBaseUrl;

    @ApiOperation(value = "run team conversation inner", nickname = "runTeamConversationInner",
        notes = "团队对话转发引擎 /v1/conversation/team，内部调用，SSE 透传", tags = {"ConversationTeam"})
    @RequestMapping(value = "/v1/inner/{project_id}/conversations/{conversation_id}/team",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE, consumes = {"application/json"}, method = RequestMethod.POST)
    public SseEmitter runTeamConversation(
        @PathVariable("project_id") String projectId,
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @PathVariable("conversation_id") String conversationId,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionId,
        @Valid @RequestBody ConversationTeamReq body) {
        // 组装引擎团队端点请求体（无 systemPrompt：监督者提示词固定引擎侧）
        Map<String, Object> reqBody = new LinkedHashMap<>();
        reqBody.put("conversationId", conversationId);
        reqBody.put("query", body.getQuery());
        reqBody.put("subAgentIds", body.getSubAgentIds());
        reqBody.put("modelDeploymentId", body.getModelDeploymentId());
        if (body.getConversationHistory() != null && !body.getConversationHistory().isEmpty()) {
            reqBody.put("conversationHistory", body.getConversationHistory());
        }
        String json = JSON.toJSONString(reqBody);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        // 透传 execution_id：引擎按 X-Execution-Id 头回显（2026-08-11 引擎已支持读头），保证 user 行与
        // run/sub_run 行 execution_id 一致（X-Task-Id 供引擎侧日志溯源）
        if (StringUtils.isNotBlank(executionId)) {
            headers.put("X-Execution-Id", executionId);
            headers.put("X-Task-Id", executionId);
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        CountDownLatch latch = new CountDownLatch(1);
        boolean started = okHttpUtils.stream(jiuwenBaseUrl + TEAM_API, headers, json,
            new TeamForwardListener(emitter, latch, conversationId, executionId));
        if (!started) {
            failEmitter(emitter, "engine team stream request failed: " + jiuwenBaseUrl + TEAM_API);
            return emitter;
        }
        try {
            if (!latch.await(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                failEmitter(emitter, "engine team stream connect timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failEmitter(emitter, "interrupted while waiting engine team stream connect");
        }
        return emitter;
    }

    private void failEmitter(SseEmitter emitter, String message) {
        try {
            emitter.completeWithError(new RuntimeException(message));
        } catch (Exception ignored) {
        }
        log.warn("Conversation team stream failed: {}, conversationId={}", message, emitter);
    }

    /** 引擎 SSE 事件原样转发（无事件名帧，data 透传）；流关闭/异常即结束 */
    private static final class TeamForwardListener extends EventSourceListener {

        private final SseEmitter emitter;
        private final CountDownLatch latch;
        private final String conversationId;
        private final String executionId;

        TeamForwardListener(SseEmitter emitter, CountDownLatch latch, String conversationId, String executionId) {
            this.emitter = emitter;
            this.latch = latch;
            this.conversationId = conversationId;
            this.executionId = executionId;
        }

        @Override
        public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
            latch.countDown();
        }

        @Override
        public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type,
                            @NotNull String data) {
            try {
                emitter.send(SseEmitter.event().data(data).build());
            } catch (Throwable e) {
                log.warn("SSE send fail, conversationId={}, executionId={}", conversationId, executionId, e);
            }
        }

        @Override
        public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
            latch.countDown();
            try {
                emitter.completeWithError(t == null ? new RuntimeException("engine team stream failure") : t);
            } catch (Exception ignored) {
            }
            log.warn("Conversation team stream failure, conversationId={}, executionId={}", conversationId,
                executionId, t);
        }

        @Override
        public void onClosed(@NotNull EventSource eventSource) {
            latch.countDown();
            try {
                emitter.complete();
            } catch (Exception ignored) {
            }
            log.info("Conversation team stream closed, conversationId={}, executionId={}", conversationId,
                executionId);
        }
    }
}
