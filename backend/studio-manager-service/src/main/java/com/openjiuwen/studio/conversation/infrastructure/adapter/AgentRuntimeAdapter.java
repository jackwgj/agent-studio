/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.adapter;

import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.ControllerIR;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.ControllerManagementService;
import com.openjiuwen.studio.conversation.application.dto.SendMessageCmd;
import com.openjiuwen.studio.conversation.domain.model.Conversation;
import com.openjiuwen.studio.conversation.domain.repository.ConversationRepository;

import com.alibaba.fastjson2.JSON;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSources;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 运行时防腐层（ACL）：封装对 runtime 运行链路的调用（inner 会话端点）、引擎 SSE 事件 → 领域消息转换与落库。
 *
 * <p>模型切换：按 (projectId, modelDeploymentId) 生成/获取预烘焙模型的多 Agent IR（复用平台
 * {@link ControllerManagementService#generateConversationIr}），IR 元数据注入 projectId/workspaceId 后重传 OBS，
 * 经 inner 端点按 agentId 直接调用 runtime——无需写 release，无需运行时请求期 patch。</p>
 *
 * <p>execution_id：每轮由调用方生成（X-Execution-Id 请求头），引擎事件原样携带，run/sub_run 分组精确。</p>
 */
@Slf4j
@Component
public class AgentRuntimeAdapter {

    private static final long SSE_TIMEOUT = 900_000L;
    private static final long CONNECT_TIMEOUT_SECONDS = 30L;

    @Value("${agent_runtime_endpoint:http://127.0.0.1:31113}")
    private String runtimeEndpoint;

    /** 团队子 Agent ID 列表（内置常量，POC：团队变更时改此处，不再走 yml 配置） */
    private static final String TEAM_AGENT_IDS =
        "d321fa88-a768-4b63-8d68-13cd743c6903,8dafdc64-2c52-40b5-9b24-49894314b763";

    /** 监督者系统提示词（内置常量，POC） */
    private static final String SYSTEM_PROMPT =
        "你是一个智能对话助手，根据用户意图从团队中选择最合适的专业 Agent 处理任务。若无法匹配任何 Agent，直接告知用户并提供建议。";

    private final ControllerManagementService controllerManagementService;
    private final MgObsService mgObsService;
    private final ConversationRepository conversationRepository;
    private final OkHttpClientUtils okHttpClientUtils;
    private final ObjectMapper objectMapper;

    /**
     * 预烘焙 IR 缓存：key = projectId_modelDeploymentId，value = IR agentId（OBS 路径约定 agent/ir/{agentId}/{agentId}.json）
     */
    private final Map<String, String> conversationIrCache = new ConcurrentHashMap<>();

    public AgentRuntimeAdapter(ControllerManagementService controllerManagementService,
                               MgObsService mgObsService,
                               ConversationRepository conversationRepository,
                               OkHttpClientUtils okHttpClientUtils,
                               ObjectMapper objectMapper) {
        this.controllerManagementService = controllerManagementService;
        this.mgObsService = mgObsService;
        this.conversationRepository = conversationRepository;
        this.okHttpClientUtils = okHttpClientUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 发起一轮对话运行（SSE 流式返回；事件缓冲到整轮结束一次性落库）
     *
     * @param conversation   会话聚合
     * @param cmd            发送消息命令（query/model_deployment_id）
     * @param histories      全量历史（平台 Message 列表，经 histories 钩子注入引擎）
     * @param executionId    本轮 execution_id（调用方生成，随 X-Execution-Id 请求头下发）
     * @param requestHeaders 当前请求头（转发给 runtime，认证所需的 X-Auth-Token 由 IAM 上下文补齐）
     * @return SSE 流
     */
    public SseEmitter run(Conversation conversation, SendMessageCmd cmd, List<Message> histories,
                          String executionId, HttpHeaders requestHeaders) {
        String agentId = ensureConversationIr(conversation.getProjectId(), conversation.getWorkspaceId(),
            cmd.getModelDeploymentId());

        String url = runtimeEndpoint + "/v1/inner/" + conversation.getProjectId() + "/agents/" + agentId
            + "/conversations/" + conversation.getConversationId() + "?workspace_id=" + conversation.getWorkspaceId();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", cmd.getQuery());
        body.put("model_deployment_id", cmd.getModelDeploymentId());
        body.put("enable_history", true);
        body.put("histories", histories);

        Request.Builder builder = new Request.Builder()
            .url(url)
            .post(RequestBody.create(toJson(body), MediaType.parse("application/json; charset=utf-8")));
        copyRequestHeaders(builder, requestHeaders);
        builder.addHeader("X-Execution-Id", executionId);

        // SseEmitter 是 Spring MVC 提供的一个组件，用于在 HTTP 协议上实现 SSE（Server-Sent Events，服务器发送事件），即服务器向浏览器/客户端单向、持续、流式地推送数据。
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);

        CountDownLatch latch = new CountDownLatch(1);
        EventSources.createFactory(okHttpClientUtils.getHttpClient())
            .newEventSource(builder.build(),
                new ConversationRunEventSourceListener(sseEmitter, latch, conversation.getConversationId(),
                    executionId, cmd.getModelDeploymentId(), conversationRepository));
        try {
            latch.await(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting conversation run stream connect.", e);
        }
        log.debug("Conversation run stream started: conversationId={}, executionId={}",
            conversation.getConversationId(), executionId);
        return sseEmitter;
    }

    /**
     * 把当前请求头转发给 runtime（manager 统一模式，参照 AgentServiceProxyService.stream），
     * 并补齐 runtime 认证所需 X-Auth-Token（POC 模式按 userId|projectId 解析，取自 IAM 上下文）。
     */
    void copyRequestHeaders(Request.Builder builder, HttpHeaders requestHeaders) {
        requestHeaders.forEach((key, value) -> {
            // X-Auth-Token 以 IAM 上下文为准，跳过外部传入值，避免重复头
            if (CommonConstant.X_AUTH_TOKEN.equalsIgnoreCase(key) || value == null || value.isEmpty()) {
                return;
            }
            builder.addHeader(key, String.join(",", value));
        });
        builder.addHeader(CommonConstant.X_AUTH_TOKEN, RequestContextUtils.getRequestAuthToken());
    }

    /**
     * 生成/获取预烘焙模型的多 Agent IR（per (project, model) 缓存）
     */
    private String ensureConversationIr(String projectId, String workspaceId, String modelDeploymentId) {
        String cacheKey = projectId + "_" + modelDeploymentId;
        String cachedAgentId = conversationIrCache.get(cacheKey);
        if (cachedAgentId != null) {
            return cachedAgentId;
        }

        List<String> teamAgentIds = parseTeamAgentIds(TEAM_AGENT_IDS);
        if (teamAgentIds.isEmpty()) {
            throw new IllegalStateException("TEAM_AGENT_IDS is empty");
        }

        ControllerIR ir = controllerManagementService.generateConversationIr(teamAgentIds, modelDeploymentId,
            SYSTEM_PROMPT);
        // IR 元数据注入租户/空间，满足 inner 端点的归属校验（getAgentMetadata 读 metadata.projectId）
        ir.getMetadata().put("projectId", projectId);
        ir.getMetadata().put("workspaceId", workspaceId);
        // runtime 发布态 IR 读取约定：version=null → 缓存 key = {agentId}_published → OBS 文件名 {agentId}_published.json
        mgObsService.uploadObsFile(ir.getAgentId(), ir.getAgentId() + "_" + CommonConstant.AGENT_PUBLISHED,
            CommonConstant.AGENT, JSON.toJSONString(ir), CommonConstant.Workflow.IR);

        conversationIrCache.put(cacheKey, ir.getAgentId());
        log.info("Conversation IR generated and uploaded: projectId={}, modelDeploymentId={}, agentId={}",
            projectId, modelDeploymentId, ir.getAgentId());
        return ir.getAgentId();
    }

    private List<String> parseTeamAgentIds(String idsStr) {
        if (StringUtils.isBlank(idsStr)) {
            return Collections.emptyList();
        }
        return Arrays.stream(idsStr.split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .toList();
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            log.error("Failed to serialize conversation run request body.", e);
            return "{}";
        }
    }
}
