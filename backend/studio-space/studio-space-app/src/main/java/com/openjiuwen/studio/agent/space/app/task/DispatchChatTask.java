/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.task;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderDispatchVo;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderRuntimeReq;
import com.openjiuwen.studio.agent.space.app.constant.RedisKeyConstant;
import com.openjiuwen.studio.agent.space.app.enums.DRTaskStatusType;
import com.openjiuwen.studio.agent.space.app.handler.DREventSourceListener;
import com.openjiuwen.studio.agent.space.app.handler.DREventSourceListenerFactory;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.app.util.EncodingUtil;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拉起问答任务类
 */
@Slf4j
@Component
public class DispatchChatTask {
    @Resource
    private RedisClient redisClient;

    @Resource
    private DRTaskHandler drTaskHandler;

    @Resource
    private OkHttpClientUtils okHttpClientUtils;

    @Resource
    DREventSourceListenerFactory drEventSourceListenerFactory;

    @Value("${agent.runtime.endpoint}")
    private String agentRuntimeEndPoint;

    ConcurrentHashMap<String, EventSource> sseMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void register() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("[DispatchChatTask] cleanup start size: {}", sseMap.size());
            for (Map.Entry<String, EventSource> entry : sseMap.entrySet()) {
                if (entry.getValue() != null) {
                    log.info("[DispatchChatTask] cleanup task: {}", entry.getKey());
                    entry.getValue().cancel();
                }
            }
            log.info("[DispatchChatTask] cleanup end");
        }));
    }

    /**
     * 分发问答任务
     *
     * @param AgentBuilderDispatchVo 分发任务入参
     */
    public void createChatTask(AgentBuilderDispatchVo AgentBuilderDispatchVo) {
        String taskId = AgentBuilderDispatchVo.getTaskId();

        int nextStatus = AgentBuilderDispatchVo.getTaskStatus() == DRTaskStatusType.INITIATING.getStatus()
            ? DRTaskStatusType.GENERATE_QUESTIONS.getStatus()
            : DRTaskStatusType.PLAN_RUNNING.getStatus();
        boolean update = drTaskHandler.updateTaskStatus(taskId, nextStatus);

        AgentBuilderDispatchVo.setTaskStatus(nextStatus);
        if (!update) {
            log.warn("[createChatTask] task status update fail taskId: {} nextStatus: {}", taskId, nextStatus);
            return;
        }

        Map<String, String> headers = AppCommonUtils.getCommonMapHeaders();

        AgentBuilderRuntimeReq AgentBuilderRuntimeReq = new AgentBuilderRuntimeReq().setInputs(AgentBuilderDispatchVo.getInputs());

        RequestBody body = RequestBody.create(JSON.toJSONString(AgentBuilderRuntimeReq),
            MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder().url(getAgentUrl(AgentBuilderDispatchVo))
            .post(body)
            .headers(Headers.of(headers))
            .build();

        DREventSourceListener listener = drEventSourceListenerFactory.create(AgentBuilderDispatchVo, () -> {
            sseMap.remove(taskId);
            redisClient.unlockWithId(String.format(RedisKeyConstant.DR_SSE_LOCK, taskId), taskId);
        });

        EventSource source = null;
        try {
            source = EventSources.createFactory(okHttpClientUtils.getHttpClient()).newEventSource(request, listener);
            sseMap.put(taskId, source);
            OkHttpClient okHttpClient = okHttpClientUtils.getHttpClient();
        } catch (Exception e) {
            log.error("Failed to create EventSource for task: {}", taskId, e);
            // 确保回调被调用，清理资源
            listener.onFailure(source, e, null);
            throw e;
        }
    }

    private String getAgentUrl(AgentBuilderDispatchVo AgentBuilderDispatchVo) {
        String encodedAgentId = EncodingUtil.encodeURIComponent(AgentBuilderDispatchVo.getAgentId());
        String encodedTaskId = EncodingUtil.encodeURIComponent(AgentBuilderDispatchVo.getTaskId());

        String agentUrl;
        if (AgentBuilderDispatchVo.getRunType() == 0) {
            String encodedProjectId = EncodingUtil.encodeURIComponent(AuthorizationContextHolder.projectId());
            agentUrl = String.format("%s/v1/%s/agents/%s/conversations/%s?agent_type=%s", agentRuntimeEndPoint,
                encodedProjectId, encodedAgentId, encodedTaskId, "deepresearch");
        } else {
            String encodedWorkspaceId = EncodingUtil.encodeURIComponent(AgentBuilderDispatchVo.getWorkspaceId());
            agentUrl = String.format("%s/v1/agents/chat/%s?workspace_id=%s&conversation_id=%s", agentRuntimeEndPoint,
                encodedAgentId, encodedWorkspaceId, encodedTaskId);
        }

        return agentUrl;
    }
}
