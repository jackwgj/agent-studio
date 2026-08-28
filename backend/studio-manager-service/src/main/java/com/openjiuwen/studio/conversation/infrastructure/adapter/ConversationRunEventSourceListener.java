/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.openjiuwen.studio.conversation.domain.model.ConversationMessage;
import com.openjiuwen.studio.conversation.domain.model.ConversationWorkflowNode;
import com.openjiuwen.studio.conversation.domain.model.valueobject.ExecutionRef;
import com.openjiuwen.studio.conversation.domain.model.valueobject.FileRef;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 对话运行 SSE 监听器（按轮持久化，团队新协议）：
 * Java 对事件透明——只做「转发前端 + 按 canonical 运行身份持久化」，不解释 Agent 业务内容。
 *
 * <p>入库粒度 = 每次 LLM 调用（一轮）：每轮 reasoning 行（event=reasoning）+ message 行（event=message），
 * 工具一次调用合并一行（role=tool，event=tool_call，content=结果/异常，tool_args=参数，tool_id=toolName）。
 * run_start/skill_activated/error/run_end 作为运行树状态落库；user_message 不落（user 行发送前已落）；
 * runId/parentRunId/toolId 路由，只有收到 canonical tool_result 的工具调用才落库。</p>
 *
 * <p>轮边界 = 事件类型（机械规则）：tool_call / run_end / error 到达即结算当前轮；
 * created_at 按到达序（base+seq）单调递增，供读侧"先调用先渲染"。</p>
 */
@Slf4j
public class ConversationRunEventSourceListener extends EventSourceListener {

    private static final String EVENT_MESSAGE = "message";
    private static final String EVENT_REASONING = "reasoning";
    private static final String EVENT_RUN_START = "run_start";
    private static final String EVENT_TOOL_CALL = "tool_call";
    private static final String EVENT_TOOL_RESULT = "tool_result";
    private static final String EVENT_WORKFLOW_NODE = "workflow_node";
    private static final String EVENT_RUN_END = "run_end";
    private static final String EVENT_ERROR = "error";
    private static final String EVENT_SKILL_ACTIVATED = "skill_activated";
    private static final String EVENT_ARTIFACT = "artifact";
    private static final String SSE_DONE_MARKER = "[DONE]";

    private static final String FIELD_RUN_ID = "runId";
    private static final String FIELD_PARENT_RUN_ID = "parentRunId";
    private static final String FIELD_EXECUTION_TYPE = "executionType";
    private static final String FIELD_AGENT_ID = "agentId";
    private static final String FIELD_TOOL_CALL_ID = "toolId";
    private static final String FIELD_TOOL_NAME = "toolName";
    private static final String FIELD_ARGUMENTS = "arguments";
    private static final String FIELD_RESULT = "result";
    private static final String FIELD_DELTA = "delta";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_OBJECT_KEY = "objectKey";
    private static final String FIELD_FILE_NAME = "fileName";
    private static final String FIELD_SIZE = "size";
    private static final String FIELD_MEDIA_TYPE = "mediaType";
    private static final String FIELD_CHECKSUM = "checksum";
    private static final String FIELD_EXECUTION_ID = "executionId";

    private static final String ROLE_ASSISTANT = "assistant";
    private static final String ROLE_TOOL = "tool";

    private final SseEmitter sseEmitter;
    private final CountDownLatch latch;
    private final String conversationId;
    private final String executionId;
    private final String modelDeploymentId;
    private final ConversationRepository conversationRepository;
    private final List<ConversationWorkflowNode> workflowNodes = new ArrayList<>();

    /** 当前轮缓冲（key = canonical runId） */
    private final Map<String, RoundBuffer> currentRounds = new LinkedHashMap<>();
    /** 已结算轮（按结算序） */
    private final List<RoundBuffer> settledRounds = new ArrayList<>();
    /** 工具调用缓冲（toolCallId → 调用），tool_result 按 toolCallId 配对回填 */
    private final Map<String, ToolInvocation> toolInvocations = new LinkedHashMap<>();
    /** 已上传正式产物；事件来自受信 Runtime，持久化时只采用当前监听器 executionId。 */
    private final List<ArtifactInvocation> artifacts = new ArrayList<>();
    /** 运行树控制事件，供刷新后恢复根/子状态、Skill 与错误。 */
    private final List<ScoredRow> controlEvents = new ArrayList<>();
    /** 到达序计数器（每事件 +1），created_at = base + seq */
    private long arrivalSeq;
    private final long baseTime = System.currentTimeMillis();
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
        // 1. 原样转发前端（不带事件名帧，前端按 data.event 区分）
        try {
            sseEmitter.send(SseEmitter.event().data(data).build());
        } catch (Throwable e) {
            log.warn("SSE send message fail, conversationId={}", conversationId, e);
        }
        arrivalSeq++;

        // 2. 机械缓冲：事件类型即落行边界（不分析事件内容）
        try {
            JSONObject json = JSON.parseObject(data);
            if (json == null) {
                return;
            }
            JSONObject dataObj = json.getJSONObject("data");
            String event = json.getString("event");
            if (dataObj == null) {
                dataObj = new JSONObject();
            }
            // Canonical adapters may carry identity at envelope level; keep data routing deterministic.
            copyIfAbsent(dataObj, FIELD_RUN_ID, json.getString(FIELD_RUN_ID));
            copyIfAbsent(dataObj, FIELD_PARENT_RUN_ID, json.getString(FIELD_PARENT_RUN_ID));
            copyIfAbsent(dataObj, FIELD_EXECUTION_TYPE, json.getString(FIELD_EXECUTION_TYPE));
            copyIfAbsent(dataObj, FIELD_EXECUTION_ID, json.getString(FIELD_EXECUTION_ID));
            copyIfAbsent(dataObj, "workflowId", json.getString("workflowId"));
            copyIfAbsent(dataObj, "nodeId", json.getString("nodeId"));
            copyIfAbsent(dataObj, "eventIndex", json.getLong("index"));
            log.info("Conversation team event received: conversationId={}, runId={}, event={}, "
                    + "parentRunId={}, toolId={}, agentId={}, toolName={}",
                conversationId,
                executionId,
                event,
                dataObj == null ? null : dataObj.getString(FIELD_PARENT_RUN_ID),
                dataObj == null ? null : dataObj.getString(FIELD_TOOL_CALL_ID),
                dataObj == null ? null : dataObj.getString(FIELD_AGENT_ID),
                dataObj == null ? null : dataObj.getString(FIELD_TOOL_NAME));
            switch (event == null ? "" : event) {
                case EVENT_RUN_START -> bufferControlEvent(EVENT_RUN_START, dataObj);
                case EVENT_MESSAGE -> {
                    String delta = dataObj == null ? null : dataObj.getString(FIELD_DELTA);
                    if (delta != null && !delta.isBlank()) {
                        roundOf(dataObj).appendMessage(delta, arrivalSeq);
                    }
                }
                case EVENT_REASONING -> {
                    String content = dataObj == null ? null : dataObj.getString(FIELD_CONTENT);
                    if (content != null && !content.isBlank()) {
                        roundOf(dataObj).appendReasoning(content, arrivalSeq);
                    }
                }
                case EVENT_TOOL_CALL -> {
                    // 该轮 LLM 决定调工具 → 本轮输出结束，结算当前轮
                    settleRound(keyOf(dataObj));
                    String callId = dataObj == null ? null : dataObj.getString(FIELD_TOOL_CALL_ID);
                    if (callId != null && !callId.isBlank()) {
                        toolInvocations.put(callId, new ToolInvocation(
                            callId,
                            dataObj.getString(FIELD_TOOL_NAME),
                            argsToJson(dataObj),
                            keyOf(dataObj),
                            dataObj == null ? null : dataObj.getString(FIELD_AGENT_ID),
                            dataObj == null ? null : dataObj.getString(FIELD_PARENT_RUN_ID),
                            dataObj == null ? null : dataObj.getString(FIELD_EXECUTION_TYPE),
                            dataObj == null ? null : dataObj.getString("workflowId"),
                            dataObj == null ? null : dataObj.getString("nodeId"),
                            dataObj == null ? null : dataObj.getLong("eventIndex"),
                            arrivalSeq));
                    }
                }
                case EVENT_TOOL_RESULT -> {
                    String callId = dataObj.getString(FIELD_TOOL_CALL_ID);
                    ToolInvocation invocation = toolInvocations.get(callId);
                    if (invocation != null) {
                        invocation.result = dataObj.getString(FIELD_RESULT);
                    }
                }
                case EVENT_ARTIFACT -> bufferArtifact(dataObj);
                case EVENT_WORKFLOW_NODE -> {
                    workflowNodes.add(toWorkflowNode(dataObj));
                    bufferControlEvent(EVENT_WORKFLOW_NODE, dataObj);
                }
                case EVENT_SKILL_ACTIVATED -> bufferControlEvent(EVENT_SKILL_ACTIVATED, dataObj);
                case EVENT_RUN_END -> {
                    settleRound(keyOf(dataObj));
                    bufferControlEvent(EVENT_RUN_END, dataObj);
                }
                case EVENT_ERROR -> {
                    log.warn("Conversation team error event: conversationId={}, executionId={}, data={}",
                        conversationId, executionId, data);
                    settleRound(keyOf(dataObj));
                    bufferControlEvent(EVENT_ERROR, dataObj);
                }
                default -> {
                    // user_message/run_start/usage 仅透传不落
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
            // 浏览器公共 SSE 客户端只把 [DONE]/event=done 识别为正常终态；必须在落库完成后发送，
            // 避免前端提前释放输入框并在上一轮消息尚未持久化时启动下一轮。
            sseEmitter.send(SseEmitter.event().data(SSE_DONE_MARKER).build());
        } catch (Throwable e) {
            // 客户端主动断开时终止标记可能无法发送，不应影响服务端收口。
            log.debug("SSE done marker send failed, conversationId={}, executionId={}",
                conversationId, executionId, e);
        }
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
     * 结算当前轮：把轮内 reasoning/message 移入已结算列表（空轮跳过）。
     * 后续 flush 按到达序构造行；该 key 的下一批 delta 自然开启新一轮。
     */
    private void settleRound(String key) {
        RoundBuffer round = currentRounds.remove(key);
        if (round != null && round.hasContent()) {
            settledRounds.add(round);
        }
    }

    /** 结算所有未结算轮（流关闭/异常时把已接收的部分内容也落库） */
    private void settleAllCurrentRounds() {
        new ArrayList<>(currentRounds.keySet()).forEach(this::settleRound);
    }

    private RoundBuffer roundOf(JSONObject dataObj) {
        String key = keyOf(dataObj);
        return currentRounds.computeIfAbsent(key,
            k -> new RoundBuffer(k,
                dataObj.getString(FIELD_PARENT_RUN_ID),
                dataObj.getString(FIELD_EXECUTION_TYPE),
                dataObj.getString("workflowId"),
                dataObj.getString("nodeId"),
                dataObj.getLong("eventIndex"),
                dataObj.getString(FIELD_AGENT_ID)));
    }

    private String keyOf(JSONObject dataObj) {
        if (dataObj == null) {
            return executionId;
        }
        String runId = dataObj.getString(FIELD_RUN_ID);
        return runId == null || runId.isBlank() ? executionId : runId;
    }

    private void copyIfAbsent(JSONObject target, String key, String value) {
        if (value != null && !target.containsKey(key)) {
            target.put(key, value);
        }
    }

    private void copyIfAbsent(JSONObject target, String key, Long value) {
        if (value != null && !target.containsKey(key)) {
            target.put(key, value);
        }
    }

    private ConversationWorkflowNode toWorkflowNode(JSONObject data) {
        return ConversationWorkflowNode.builder()
            .conversationId(conversationId)
            .toolId(data.getString(FIELD_TOOL_CALL_ID))
            .parentRunId(data.getString(FIELD_PARENT_RUN_ID))
            .workflowId(data.getString("workflowId"))
            .nodeId(data.getString("nodeId"))
            .nodeName(data.getString("nodeName"))
            .nodeType(data.getString("nodeType"))
            .nodeIndex(data.getInteger("nodeIndex"))
            .status(data.getString("status"))
            .inputContent(jsonText(data.get("input")))
            .outputContent(jsonText(data.get("output")))
            .errorCode(data.getString("errorCode"))
            .errorMessage(data.getString("errorMessage"))
            .build();
    }

    private String jsonText(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof String ? (String) value : JSON.toJSONString(value);
    }

    private String argsToJson(JSONObject dataObj) {
        if (dataObj == null) {
            return null;
        }
        Object args = dataObj.get(FIELD_ARGUMENTS);
        return args == null ? null : JSON.toJSONString(args);
    }

    private void bufferArtifact(JSONObject dataObj) {
        if (dataObj == null) {
            return;
        }
        String objectKey = dataObj.getString(FIELD_OBJECT_KEY);
        String fileName = dataObj.getString(FIELD_FILE_NAME);
        Long size = dataObj.getLong(FIELD_SIZE);
        String mediaType = dataObj.getString(FIELD_MEDIA_TYPE);
        String checksum = dataObj.getString(FIELD_CHECKSUM);
        String payloadExecutionId = dataObj.getString(FIELD_EXECUTION_ID);
        if (objectKey == null || objectKey.isBlank() || fileName == null || fileName.isBlank()
            || size == null || size < 0 || mediaType == null || mediaType.isBlank()
            || checksum == null || !checksum.matches("[0-9a-f]{64}")) {
            log.warn("Ignore incomplete artifact event: conversationId={}, executionId={}",
                conversationId, executionId);
            return;
        }
        if (payloadExecutionId != null && !payloadExecutionId.isBlank()
            && !executionId.equals(payloadExecutionId)) {
            log.warn("Ignore artifact with mismatched execution ownership: conversationId={}, executionId={}",
                conversationId, executionId);
            return;
        }
        artifacts.add(new ArtifactInvocation(
            new FileRef(objectKey, fileName, size, mediaType, checksum, executionId),
            keyOf(dataObj), dataObj.getString(FIELD_PARENT_RUN_ID), dataObj.getString(FIELD_AGENT_ID),
            executionTypeOf(dataObj), dataObj.getString("workflowId"), dataObj.getString("nodeId"),
            dataObj.getLong("eventIndex"), arrivalSeq));
    }

    private void bufferControlEvent(String event, JSONObject dataObj) {
        String content = JSON.toJSONString(dataObj == null ? new JSONObject() : dataObj);
        ConversationMessage message = ConversationMessage.builder()
            .role(ROLE_ASSISTANT)
            .content(content)
            .executionRef(new ExecutionRef(keyOf(dataObj),
                dataObj == null ? null : dataObj.getString(FIELD_PARENT_RUN_ID),
                dataObj == null ? null : dataObj.getString(FIELD_AGENT_ID), executionTypeOf(dataObj)))
            .workflowId(dataObj == null ? null : dataObj.getString("workflowId"))
            .nodeId(dataObj == null ? null : dataObj.getString("nodeId"))
            .eventIndex(dataObj == null ? null : dataObj.getLong("eventIndex"))
            .modelDeploymentId(modelDeploymentId)
            .event(event)
            .createdAt(new Date(baseTime + arrivalSeq))
            .build();
        controlEvents.add(new ScoredRow(arrivalSeq, message));
    }

    private String executionTypeOf(JSONObject dataObj) {
        String executionType = dataObj == null ? null : dataObj.getString(FIELD_EXECUTION_TYPE);
        return executionType == null || executionType.isBlank() ? "agent" : executionType;
    }

    /**
     * 整轮结束/异常一次性批量落库（按 canonical runId/parentRunId 路由，事务性）。
     * created_at 按到达序（base+seq）单调递增。
     */
    private void flush() {
        if (flushed) {
            return;
        }
        flushed = true;
        settleAllCurrentRounds();
        List<ScoredRow> scored = new ArrayList<>();
        scored.addAll(controlEvents);
        for (RoundBuffer round : settledRounds) {
            if (round.reasoning != null && !round.reasoning.isBlank()) {
                scored.add(new ScoredRow(round.reasoningSeq,
                    buildMessage(round, ROLE_ASSISTANT, round.reasoning, EVENT_REASONING, round.reasoningSeq)));
            }
            if (round.message != null && !round.message.isBlank()) {
                scored.add(new ScoredRow(round.messageSeq,
                    buildMessage(round, ROLE_ASSISTANT, round.message, EVENT_MESSAGE, round.messageSeq)));
            }
        }
        for (ToolInvocation invocation : toolInvocations.values()) {
            log.info("Conversation team tool invocation before flush: conversationId={}, runId={}, "
                    + "parentRunId={}, toolId={}, agentId={}, toolName={}, resultPresent={}",
                conversationId,
                invocation.runId,
                invocation.parentRunId,
                invocation.toolCallId,
                invocation.agentId,
                invocation.toolName,
                invocation.result != null && !invocation.result.isBlank());
            if (invocation.result != null && !invocation.result.isBlank()) {
                scored.add(new ScoredRow(invocation.seq, buildToolMessage(invocation)));
            }
        }
        for (ArtifactInvocation artifact : artifacts) {
            scored.add(new ScoredRow(artifact.seq, buildArtifactMessage(artifact)));
        }
        scored.sort(Comparator.comparingLong(s -> s.seq));
        List<ConversationMessage> rows = new ArrayList<>();
        scored.forEach(scoredRow -> rows.add(scoredRow.message));
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
        if (!workflowNodes.isEmpty()) {
            try {
                conversationRepository.appendWorkflowNodes(conversationId, workflowNodes);
            } catch (Throwable e) {
                log.error("Failed to persist conversation workflow nodes, conversationId={}", conversationId, e);
            }
        }
    }

    private ConversationMessage buildMessage(RoundBuffer round, String role, String content, String event, long seq) {
        return ConversationMessage.builder()
            .role(role)
            .content(content)
            .executionRef(new ExecutionRef(round.runId, round.parentRunId, round.agentId, round.executionType))
            .workflowId(round.workflowId)
            .nodeId(round.nodeId)
            .eventIndex(round.eventIndex)
            .modelDeploymentId(modelDeploymentId)
            .event(event)
            .createdAt(new Date(baseTime + seq))
            .build();
    }

    private ConversationMessage buildToolMessage(ToolInvocation invocation) {
        String content = invocation.result;
        return ConversationMessage.builder()
            .role(ROLE_TOOL)
            .content(content)
            .toolRef(new ToolRef(invocation.toolCallId, invocation.toolName, invocation.argsJson))
            .executionRef(new ExecutionRef(invocation.runId, invocation.parentRunId, invocation.agentId,
                invocation.executionType == null ? "agent" : invocation.executionType))
            .workflowId(invocation.workflowId)
            .nodeId(invocation.nodeId)
            .eventIndex(invocation.eventIndex)
            .modelDeploymentId(modelDeploymentId)
            .event(EVENT_TOOL_RESULT)
            .createdAt(new Date(baseTime + invocation.seq))
            .build();
    }

    private ConversationMessage buildArtifactMessage(ArtifactInvocation artifact) {
        return ConversationMessage.builder()
            .role(ROLE_ASSISTANT)
            .fileRefs(List.of(artifact.fileRef))
            .executionRef(new ExecutionRef(artifact.runId, artifact.parentRunId, artifact.agentId,
                artifact.executionType))
            .workflowId(artifact.workflowId)
            .nodeId(artifact.nodeId)
            .eventIndex(artifact.eventIndex)
            .modelDeploymentId(modelDeploymentId)
            .event(EVENT_ARTIFACT)
            .createdAt(new Date(baseTime + artifact.seq))
            .build();
    }

    /** 一轮（一次 LLM 调用）的缓冲：reasoning/message 增量累加 + 首个增量到达序 */
    private static final class RoundBuffer {

        private final String key;
        private final String runId;
        private final String parentRunId;
        private final String executionType;
        private final String workflowId;
        private final String nodeId;
        private final Long eventIndex;
        private final String agentId;
        private String reasoning;
        private String message;
        private long reasoningSeq = -1;
        private long messageSeq = -1;

        RoundBuffer(String key, String parentRunId, String executionType, String workflowId,
                    String nodeId, Long eventIndex, String agentId) {
            this.key = key;
            this.runId = key;
            this.parentRunId = parentRunId;
            this.executionType = executionType == null ? "agent" : executionType;
            this.workflowId = workflowId;
            this.nodeId = nodeId;
            this.eventIndex = eventIndex;
            this.agentId = agentId;
        }

        void appendReasoning(String content, long seq) {
            if (reasoningSeq < 0) {
                reasoningSeq = seq;
            }
            reasoning = reasoning == null ? content : reasoning + content;
        }

        void appendMessage(String content, long seq) {
            if (messageSeq < 0) {
                messageSeq = seq;
            }
            message = message == null ? content : message + content;
        }

        boolean hasContent() {
            return (reasoning != null && !reasoning.isBlank()) || (message != null && !message.isBlank());
        }
    }

    /** 一次工具调用：tool_call 注册（toolCallId 配对），tool_result 回填 result */
    private static final class ToolInvocation {

        private final String toolCallId;
        private final String toolName;
        private final String argsJson;
        private final String runId;
        private final String agentId;
        private final String parentRunId;
        private final String executionType;
        private final String workflowId;
        private final String nodeId;
        private final Long eventIndex;
        private final long seq;
        private String result;

        ToolInvocation(String toolCallId, String toolName, String argsJson, String runId, String agentId,
                       String parentRunId, String executionType, String workflowId, String nodeId,
                       Long eventIndex, long seq) {
            this.toolCallId = toolCallId;
            this.toolName = toolName;
            this.argsJson = argsJson;
            this.runId = runId;
            this.agentId = agentId;
            this.parentRunId = parentRunId;
            this.executionType = executionType;
            this.workflowId = workflowId;
            this.nodeId = nodeId;
            this.eventIndex = eventIndex;
            this.seq = seq;
        }
    }

    private static final class ArtifactInvocation {
        private final FileRef fileRef;
        private final String runId;
        private final String parentRunId;
        private final String agentId;
        private final String executionType;
        private final String workflowId;
        private final String nodeId;
        private final Long eventIndex;
        private final long seq;

        ArtifactInvocation(FileRef fileRef, String runId, String parentRunId, String agentId,
                           String executionType, String workflowId, String nodeId, Long eventIndex, long seq) {
            this.fileRef = fileRef;
            this.runId = runId;
            this.parentRunId = parentRunId;
            this.agentId = agentId;
            this.executionType = executionType;
            this.workflowId = workflowId;
            this.nodeId = nodeId;
            this.eventIndex = eventIndex;
            this.seq = seq;
        }
    }

    /** 到达序 + 待落库消息（flush 时按 seq 排序，created_at = base + seq） */
    private static final class ScoredRow {

        private final long seq;
        private final ConversationMessage message;

        ScoredRow(long seq, ConversationMessage message) {
            this.seq = seq;
            this.message = message;
        }
    }
}
