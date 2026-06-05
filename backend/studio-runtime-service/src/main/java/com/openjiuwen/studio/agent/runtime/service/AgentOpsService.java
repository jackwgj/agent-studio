/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.bo.AgentMetadata;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.PublishUtils;
import com.openjiuwen.studio.agent.common.utils.UuidUtils;
import com.openjiuwen.studio.agent.common.dto.AgentExecutionInfo;
import com.openjiuwen.studio.agent.runtime.dto.AgentInteractionReq;
import com.openjiuwen.studio.agent.runtime.dto.ExtraProps;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenAgentEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenAgentEventData;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;
import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.runtime.dto.ModelProps;
import com.openjiuwen.studio.agent.runtime.dto.SessionInfo;
import com.openjiuwen.studio.agent.runtime.dto.Span;
import com.openjiuwen.studio.agent.runtime.dto.TokensProps;
import com.openjiuwen.studio.agent.runtime.dto.UploadSessionDataReq;
import com.openjiuwen.studio.agent.runtime.dto.UploadTraceDataRequest;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;
import com.openjiuwen.studio.agent.runtime.enums.JiuwenEventType;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.RequestResult;
import com.openjiuwen.studio.agent.runtime.model.TraceBaseInfo;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;
import com.openjiuwen.studio.agent.runtime.model.ops.TraceInfo;
import com.openjiuwen.studio.agent.runtime.service.md.ModelStorageService;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 与AgentOps交互
 *
 */
@Service
@Slf4j
public class AgentOpsService {
    private static final String TRACE_ROOT_SPAN_PREFIX = "trace_root_span_";

    @Value("${agent-ops.base-url:https://localhost:8080}")
    private String agentOpsBaseUrl;

    @Value("${agent-ops.trace-api:/ops/openapi/v2/trace}")
    private String agentOpsTraceApi;

    @Value("${agent-ops.session-api:/ops/openapi/v1/session}")
    private String agentOpsSessionApi;

    @Value("${agent-ops.agent-interaction-api:/ops/openapi/v1/agent-interaction}")
    private String agentOpsInteractionApi;

    @Resource
    private OkHttpUtils okHttpUtils;

    @Resource
    private ModelStorageService modelStorageService;

    @Resource
    private WorkflowIrCacheService irCacheService;

    @Resource
    private AgentIrCacheService agentIrCacheService;

    @Resource
    private PublishUtils publishUtils;

    @Resource
    private WorkflowIrCacheService workflowIrCacheService;

    @Resource
    private ObsService obsService;

    @Resource
    private RedisClient redisClient;

    @Resource
    @Qualifier("traceTaskExecutor")
    private ThreadPoolTaskExecutor traceTaskExecutor;

    private AgentMetadata getWorkflowMedata(String workflowId, ExecuteParams executeParams) {
        try {
            return publishUtils.getPublish(workflowId,
                executeParams.isTreasure() ? Constants.TREASURE_PUBLISH_VERSION : Constants.LATEST_PUBLISH_VERSION,
                obsService);
        } catch (Exception e) {
            log.error("get workflow metadata failed, workflowId: {}", workflowId);
        }

        try {
            WorkflowIrWrapper irWrapper = workflowIrCacheService.refreshWorkflowIr(executeParams,
                executeParams.getReleasedVersion());
            WorkflowIrMetadata metadata = irWrapper.getMetadata();

            return AgentMetadata.builder()
                .domainId(metadata.getDomainId())
                .projectId(metadata.getProjectId())
                .workspaceId(metadata.getWorkspaceId())
                .updatedAt(metadata.getUpdatedAt())
                .isContentReviewEnable(irWrapper.isContentReviewEnabled())
                .safetyBarrier(irWrapper.isSafetyBarrier())
                .agentId(executeParams.getWorkflowId())
                .versionId(executeParams.getReleasedVersion())
                .build();
        } catch (Exception e) {
            log.error("get workflow metadata from ir failed, workflowId: {}", workflowId);
        }
        return null;
    }

    /**
     * 处理代理操作跟踪，生成跟踪数据并调用AgentOps进行上传。
     *
     * @param executeParams 执行参数，包含会话ID、执行ID等信息
     * @param executionInfo 执行信息，包含状态、输入输出、开始结束时间等
     */
    public void agentTraceHandler(AgentExecuteParams executeParams, AgentExecutionInfo executionInfo) {
        try {
            if (StringUtils.isEmpty(executionInfo.getStatus())) {
                return;
            }
            TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(executeParams);

            // 构建Root节点
            UploadTraceDataRequest traceDataRequest = new UploadTraceDataRequest();
            Span spanRoot = new Span();
            String spanRootId = UuidUtils.getUUID();
            spanRoot.setSessionId(executeParams.getConversationId());
            spanRoot.setTraceId(executeParams.getExecutionId());
            spanRoot.setSpaceId(traceBaseInfo.getWorkspaceId());
            spanRoot.setParentSpanId("0");
            spanRoot.setSpanId(spanRootId);
            spanRoot.setSpanType("UserInput");
            spanRoot.setSpanName("UserInput");
            spanRoot.setDomainId(traceBaseInfo.getDomainId());
            spanRoot.setProjectId(traceBaseInfo.getProjectId());
            spanRoot.setUserId(executeParams.getUserId());
            spanRoot.setResourceId(Optional.ofNullable(executeParams.getAgentId()).orElse(""));
            spanRoot.setStatusCode(executionInfo.getStatus().contains("succeeded") ? "0" : "1");
            spanRoot.setInput(executionInfo.getInputs());
            if (Strings.CI.equals("failed", executionInfo.getStatus())) {
                spanRoot.setOutput(executionInfo.getErrorInfo());
            } else {
                spanRoot.setOutput(executionInfo.getOutputs());
            }

            spanRoot.setPlatform("AGENT");
            long startTime = Objects.nonNull(executionInfo.getStartTime())
                ? executionInfo.getStartTime()
                : System.currentTimeMillis();
            spanRoot.setStartTime(startTime);

            long endTime = Objects.nonNull(executionInfo.getEndTime())
                ? executionInfo.getEndTime()
                : System.currentTimeMillis();
            spanRoot.setDuration((int) (endTime - startTime));

            spanRoot.setCallType(executeParams.getTraceMode());

            // 构建子节点
            List<Span> spanList = new ArrayList<>();
            spanList.add(spanRoot);
            executionInfo.getInvokeList().forEach(invokeInfo -> {
                Span span = new Span();
                span.setSessionId(executeParams.getConversationId());
                span.setTraceId(executeParams.getExecutionId());
                span.setSpaceId(traceBaseInfo.getWorkspaceId());
                span.setParentSpanId(spanRootId);
                span.setSpanId(UuidUtils.getUUID());
                span.setSpanType(invokeInfo.getNodeType().toUpperCase(Locale.ROOT));
                span.setSpanName(span.getSpanType().equals("LLM") ? "大模型" : invokeInfo.getNodeName());
                span.setDomainId(traceBaseInfo.getDomainId());
                span.setProjectId(traceBaseInfo.getProjectId());
                span.setUserId(executeParams.getUserId());
                span.setResourceId(Optional.ofNullable(executeParams.getAgentId()).orElse(""));
                span.setStatusCode(invokeInfo.getNodeStatus().contains("succeeded") ? "0" : "1");
                span.setInput(JSONObject.toJSONString(invokeInfo.getInputs()));
                span.setOutput(JSONObject.toJSONString(invokeInfo.getOutputs()));
                span.setPlatform("AGENT");
                span.setStartTime(invokeInfo.getStartTime());
                span.setDuration((int) (invokeInfo.getEndTime() - invokeInfo.getStartTime()));
                if ("LLM".equalsIgnoreCase(invokeInfo.getNodeType())) {
                    String modelDeploymentId = invokeInfo.getModelDeploymentId();
                    if (StringUtils.isBlank(modelDeploymentId)) {
                        Object metaData = invokeInfo.getMetaData();
                        if (metaData instanceof Map) {
                            Object instanceAttributes = ((Map<?, ?>) metaData).get("instance_attributes");
                            if (instanceAttributes instanceof JSONObject) {
                                modelDeploymentId = ((JSONObject) instanceAttributes).getString("model");
                            }
                        }
                    }
                    ModelProps modelProps = queryModelProps(modelDeploymentId);
                    span.setModelProps(modelProps);
                    span.getModelProps().setError(invokeInfo.getErrorMessage());

                    // 解析token消耗
                    JSONObject outPutObj = JSON.parseObject(JSONObject.toJSONString(invokeInfo.getOutputs()));
                    String modelUsage = outPutObj.getString("model_usage");
                    TokensProps tokensProps = StringUtils.isNotEmpty(modelUsage) ? JSONObject.parseObject(modelUsage,
                        TokensProps.class) : new TokensProps();
                    span.setTokensProps(tokensProps);
                }
                span.setCallType(executeParams.getTraceMode());
                spanList.add(span);
            });

            traceDataRequest.setSpanList(spanList);
            // 调用AgentOps
            invokeAgentOpsTrace(traceDataRequest);
        } catch (Exception e) {
            log.error("agent trace handler error", e);
        }
    }

    /**
     * 处理LLM模型相关信息
     *
     * @param modelId 模型id
     */
    private ModelProps queryModelProps(String modelId) {
        ModelProps modelProps = new ModelProps();
        try {
            modelProps = modelStorageService.queryModelProps(modelId);
        } catch (Exception e) {
            log.error("queryModelProps error: ", e);
        }
        return modelProps;
    }

    public void agentSessionHandler(AgentExecuteParams executeParams, String resourceType) {
        try {
            TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(executeParams);
            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setDomainId(traceBaseInfo.getDomainId());
            sessionInfo.setProjectId(traceBaseInfo.getProjectId());
            sessionInfo.setSpaceId(traceBaseInfo.getWorkspaceId());
            sessionInfo.setSessionId(executeParams.getConversationId());
            sessionInfo.setUserId(executeParams.getUserId());
            Long startTime = executeParams.getStartTime() != 0
                ? executeParams.getStartTime()
                : System.currentTimeMillis();
            sessionInfo.setStartTime(startTime);
            sessionInfo.setResourceId(Optional.ofNullable(executeParams.getAgentId()).orElse(""));
            sessionInfo.setPlatform(resourceType);

            sessionInfo.setCallType(executeParams.getTraceMode());

            UploadSessionDataReq uploadSessionDataReq = new UploadSessionDataReq();
            uploadSessionDataReq.setSessionInfoList(List.of(sessionInfo));

            invokeAgentOpsSession(uploadSessionDataReq);
        } catch (Exception e) {
            log.error("agent session handler error", e);
        }
    }

    public String handlerWorkspaceId(boolean isAgent, String workflowId, String version) {
        try {
            String id;
            if (isAgent) {
                id = agentIrCacheService.getIrWorkspaceFromCache(workflowId, version);
            } else {
                id = irCacheService.getIrWorkspaceFromCache(workflowId, version);
            }
            return id == null ? "" : id;
        } catch (Exception e) {
            log.warn("Fail get workspaceId from ir. {}", e.getMessage());
            return "";
        }
    }

    private TraceBaseInfo buildTraceBaseInfo(AgentExecuteParams executeParams) {
        return buildTraceBaseInfo(executeParams.getDomainId(), executeParams.getOwnerProjectId(), executeParams.getOwnerWorkspaceId(), executeParams.getAgentId());
    }

    private TraceBaseInfo buildTraceBaseInfo(TraceInfo traceInfo) {
        return buildTraceBaseInfo(traceInfo.getDomainId(), traceInfo.getProjectId(), traceInfo.getWorkspaceId(), traceInfo.getAgentId());
    }

    private TraceBaseInfo buildTraceBaseInfo(String domainId, String projectId, String workspaceId, String id) {
        TraceBaseInfo traceBaseInfo = new TraceBaseInfo();
        AgentMetadata metadata = null;
        try {
            metadata = publishUtils.getPublish(id, Constants.LATEST_PUBLISH_VERSION, obsService);
        } catch (Exception e) {
            log.error("get agent/workflow metadata failed, id: {}", id);
        }

        if (metadata != null && StringUtils.isNotEmpty(metadata.getDomainId())) {
            traceBaseInfo.setDomainId(metadata.getDomainId());
            traceBaseInfo.setProjectId(metadata.getProjectId());
            traceBaseInfo.setWorkspaceId(metadata.getWorkspaceId());
        } else {
            traceBaseInfo.setDomainId(domainId);
            traceBaseInfo.setProjectId(projectId);
            traceBaseInfo.setWorkspaceId(workspaceId);
        }
        return traceBaseInfo;
    }

    private TraceBaseInfo buildTraceBaseInfo(ExecuteParams executeParams) {
        TraceBaseInfo traceBaseInfo = new TraceBaseInfo();
        AgentMetadata agentMetadata = getWorkflowMedata(executeParams.getWorkflowId(), executeParams);
        if (agentMetadata != null && StringUtils.isNotEmpty(agentMetadata.getDomainId())) {
            traceBaseInfo.setDomainId(agentMetadata.getDomainId());
            traceBaseInfo.setProjectId(agentMetadata.getProjectId());
            traceBaseInfo.setWorkspaceId(agentMetadata.getWorkspaceId());
        } else {
            traceBaseInfo.setDomainId(executeParams.getDomainId());
            traceBaseInfo.setProjectId(executeParams.getProjectId());
            traceBaseInfo.setWorkspaceId(executeParams.getWorkspaceId());
        }
        return traceBaseInfo;
    }

    public void workflowTraceHandler(WorkflowInstanceEntity workflowInstanceEntity, ExecuteParams executeParams, boolean runEnd) {
        try {
            if (runEnd) {
                String traceId = executeParams.getExecutionId();
                TraceInfo traceInfo = getTraceInfoFromCache(traceId);

                // 首span时需要记录traceInfo的基本信息
                if (StringUtils.isBlank(traceInfo.getExecutionId())) {
                    traceInfo.setDomainId(executeParams.getDomainId());
                    traceInfo.setProjectId(executeParams.getProjectId());
                    traceInfo.setWorkspaceId(executeParams.getWorkspaceId());
                    traceInfo.setAgentId(executeParams.getWorkflowId());
                    traceInfo.setConversationId(executeParams.getConversationId());
                    traceInfo.setExecutionId(executeParams.getExecutionId());
                    traceInfo.setUserId(executeParams.getUserId());
                    traceInfo.setTraceMode(executeParams.getTraceMode());
                    traceInfo.setLastBreakTime(executeParams.getStartTime());
                    traceInfo.setPlatform("WORKFLOW");

                    String rootSpanId = traceInfo.getExecutionId() + "root";
                    traceInfo.setRootSpanId(rootSpanId);
                }

                // 构造上报ops的span列表
                List<JiuwenEvent> eventList = getEventList(workflowInstanceEntity);
                List<Span> spanList = buildSpanList(eventList, traceInfo);

                Span userSpan = buildWorkflowRootSpan(traceInfo);
                spanList.add(userSpan);

                // 上报trace到ops
                UploadTraceDataRequest uploadTraceDataRequest = new UploadTraceDataRequest();
                uploadTraceDataRequest.setSpanList(spanList);
                invokeAgentOpsTrace(uploadTraceDataRequest);
            }
        } catch (Exception e) {
            log.error("workflow trace handler error", e);
        }
    }

    private List<JiuwenEvent> getEventList(WorkflowInstanceEntity workflowInstanceEntity) {
        return workflowInstanceEntity.getEventList()
            .stream()
            .filter(this::isWorkflowNodeMessage)
            .filter(this::isTracedEvent)
            .sorted(compareJiuwenEvent())
            .collect(Collectors.toList());
    }

    private Comparator<JiuwenEvent> compareJiuwenEvent() {
        return (o1, o2) -> {
            OffsetDateTime offsetStartTime1 = OffsetDateTime.parse(o1.getData().getStartTime());
            long startTime1 = offsetStartTime1.toInstant().toEpochMilli();

            OffsetDateTime offsetStartTime2 = OffsetDateTime.parse(o2.getData().getStartTime());
            long startTime2 = offsetStartTime2.toInstant().toEpochMilli();
            return (int) (startTime1 - startTime2);
        };
    }

    private boolean isTracedEvent(JiuwenEvent jiuwenEvent) {
        // 事件是否需要上报到ops
        JiuwenEventData jiuwenEventData = jiuwenEvent.getData();
        if (Strings.CI.equals("finish", jiuwenEventData.getStatus().toString()) && Objects.nonNull(
            jiuwenEventData.getEndTime())) {
            return true;
        }
        return false;
    }

    private boolean isWorkflowNodeMessage(JiuwenEvent jiuwenEvent) {
        if (Objects.equals(jiuwenEvent.getEvent(),
            JiuwenEventType.WORKFLOW_NODE_MESSAGE.name().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return false;
    }

    private List<Span> buildSpanList(List<JiuwenEvent> eventList, TraceInfo traceInfo) {
        return Optional.ofNullable(eventList)
            .orElse(Collections.emptyList())
            .stream()
            .sorted(compareJiuwenEvent())
            .map(jiuwenEvent -> buildSpan(traceInfo, jiuwenEvent))
            .collect(Collectors.toList());
    }

    private Span buildSpan(TraceInfo traceInfo, JiuwenEvent jiuwenEvent) {
        TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(traceInfo);
        JiuwenEventData jiuwenEventData = jiuwenEvent.getData();

        // 设置span的基本属性
        Span span = new Span();
        span.setSessionId(traceInfo.getConversationId());
        String traceId = Objects.nonNull(jiuwenEvent.getExecutionId())
            ? jiuwenEvent.getExecutionId()
            : traceInfo.getExecutionId();
        span.setTraceId(traceId);
        span.setSpaceId(traceBaseInfo.getWorkspaceId());
        span.setSpanId(UuidUtils.getUUID());
        span.setSpanType(jiuwenEventData.getComponentType());
        span.setSpanName(jiuwenEventData.getComponentName());
        span.setDomainId(traceBaseInfo.getDomainId());
        span.setProjectId(traceBaseInfo.getProjectId());
        span.setUserId(Optional.ofNullable(traceInfo.getUserId()).orElse(""));
        span.setResourceId(Optional.ofNullable(traceInfo.getAgentId()).orElse(""));
        String status = getSpanStatus(jiuwenEventData.getStatus().toString());
        span.setStatusCode(status);
        span.setPlatform(traceInfo.getPlatform());
        span.setCallType(traceInfo.getTraceMode());

        // 设置span的时间属性
        long startTime = getTime(jiuwenEventData.getStartTime());
        span.setStartTime(startTime);
        long endTime = getTime(jiuwenEventData.getEndTime());
        span.setDuration((int) (endTime - startTime));

        // 设置span的输入和输出
        handleIo(jiuwenEventData, span);

        // 设置span相关的模型属性
        handleModelProps(jiuwenEventData, span);

        // 设置根span
        String parentSpanId = getParentSpanId(traceInfo, jiuwenEventData);
        span.setParentSpanId(parentSpanId);

        // 更新traceInfo
        updateTraceInfo(traceInfo, jiuwenEventData, span);
        return span;
    }

    private void handleModelProps(JiuwenEventData jiuwenEventData, Span span) {
        if ("LLM".equalsIgnoreCase(jiuwenEventData.getComponentType())) {
            JSONObject outPutObj = JSON.parseObject(JSONObject.toJSONString(jiuwenEventData.getOutputs()));

            String modelId = outPutObj.getString("deploymentId");
            ModelProps modelProps = queryModelProps(modelId);
            span.setModelProps(modelProps);

            // 解析token消耗
            TokensProps tokensProps = new TokensProps();
            tokensProps.setInputTokens(StringUtils.isEmpty(outPutObj.getString("input_tokens"))
                ? 0
                : Integer.parseInt(outPutObj.getString("input_tokens")));
            tokensProps.setOutputTokens(StringUtils.isEmpty(outPutObj.getString("output_tokens"))
                ? 0
                : Integer.parseInt(outPutObj.getString("output_tokens")));
            tokensProps.setTotalTokens(StringUtils.isEmpty(outPutObj.getString("total_tokens"))
                ? 0
                : Integer.parseInt(outPutObj.getString("total_tokens")));
            span.setTokensProps(tokensProps);

            List<ExtraProps> extra = new ArrayList<>();
            ExtraProps metadataProps = new ExtraProps();
            metadataProps.setKey("metadata");
            metadataProps.setValue(getMetaData(jiuwenEventData));
            extra.add(metadataProps);
            span.setExtra(extra);
        }
    }

    private Map<String, Object> getMetaData(JiuwenEventData jiuwenEventData) {
        Map<String, Object> metaData = jiuwenEventData.getMetaData();
        if (Objects.nonNull(metaData)) {
            return metaData;
        }
        metaData = new HashMap<>();
        List<Map<String, Object>> invokeDataList = jiuwenEventData.getOnInvokeData();
        for (Map<String, Object> invokeData : invokeDataList) {
            for (Map.Entry<String, Object> entry : invokeData.entrySet()) {
                metaData.put(entry.getKey(), entry.getValue());
            }
        }
        return metaData;
    }

    private void updateTraceInfo(TraceInfo traceInfo, JiuwenEventData jiuwenEventData, Span span) {
        Map<String, Object> outputs = jiuwenEventData.getOutputs();
        traceInfo.setLastOutput(JsonUtils.toJson(outputs));

        if (Objects.isNull(traceInfo.getFirstInput())) {
            Map<String, Object> inputs = jiuwenEventData.getInputs();
            Object userInput = inputs.get("query");
            String input;
            if (Objects.isNull(userInput)) {
                input = JsonUtils.toJson(inputs);
            } else {
                input = JsonUtils.toJson(userInput);
            }
            traceInfo.setFirstInput(input);
        }

        String componentId = jiuwenEventData.getComponentId();
        if (Objects.nonNull(componentId)) {
            traceInfo.getSpanIdMap().put(componentId, span.getSpanId());
        }
    }

    private String getParentSpanId(TraceInfo traceInfo, JiuwenEventData jiuwenEventData) {
        String parentSpanId = traceInfo.getRootSpanId();
        String parentNodeId = jiuwenEventData.getParentNodeId();
        if (MapUtils.isNotEmpty(traceInfo.getSpanIdMap()) && StringUtils.isNotEmpty(
            traceInfo.getSpanIdMap().get(parentNodeId))) {
            parentSpanId = traceInfo.getSpanIdMap().get(parentNodeId);
        }
        return parentSpanId;
    }

    private void handleIo(JiuwenEventData jiuwenEventData, Span span) {
        if (Strings.CI.equals(jiuwenEventData.getComponentType(), "Questioner") || Strings.CI.equals(
            jiuwenEventData.getComponentType(), "jiuwen.LLMReAct")) {
            List<Map<String, Object>> userInvokeData = getUserInvokeData(jiuwenEventData);
            span.setInput(JsonUtils.toJson(userInvokeData));

            List<Map<String, Object>> outputInvokeData = getOutputInvokeData(jiuwenEventData);
            span.setOutput(JsonUtils.toJson(outputInvokeData));
        } else {
            span.setInput(JsonUtils.toJson(jiuwenEventData.getInputs()));
            span.setOutput(JsonUtils.toJson(jiuwenEventData.getOutputs()));
        }
    }

    /**
     * 业界调用链span状态通常只需显示失败的
     * @param status
     * @return
     */
    private String getSpanStatus(String status) {
        if (status.contains("succeeded") || status.contains("finish")) {
            return "0";
        } else if (status.contains("wait") || status.contains("start") || status.contains("running")) {
            return "0";
        }
        return "1";
    }

    private Span buildWorkflowRootSpan(TraceInfo traceInfo) {
        TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(traceInfo);
        Span rootSpan = new Span();
        rootSpan.setSessionId(traceInfo.getConversationId());
        rootSpan.setTraceId(traceInfo.getExecutionId());
        rootSpan.setSpaceId(traceBaseInfo.getWorkspaceId());
        rootSpan.setParentSpanId("0");
        rootSpan.setDomainId(traceBaseInfo.getDomainId());
        rootSpan.setProjectId(traceBaseInfo.getProjectId());
        rootSpan.setSpanId(traceInfo.getRootSpanId());
        rootSpan.setSpanType("UserInput");
        rootSpan.setSpanName("UserInput");
        rootSpan.setUserId(traceInfo.getUserId());
        rootSpan.setResourceId(Optional.ofNullable(traceInfo.getAgentId()).orElse(""));
        rootSpan.setStatusCode("0");
        rootSpan.setInput(traceInfo.getFirstInput());
        rootSpan.setOutput(traceInfo.getLastOutput());
        rootSpan.setPlatform(traceInfo.getPlatform());
        rootSpan.setStartTime(traceInfo.getLastBreakTime());
        rootSpan.setDuration((int) (System.currentTimeMillis() - traceInfo.getLastBreakTime()));
        rootSpan.setCallType(traceInfo.getTraceMode());
        return rootSpan;
    }

    public void workflowSessionHandler(ExecuteParams executeParams) {
        try {
            TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(executeParams);
            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setDomainId(traceBaseInfo.getDomainId());
            sessionInfo.setProjectId(traceBaseInfo.getProjectId());
            sessionInfo.setSpaceId(traceBaseInfo.getWorkspaceId());
            sessionInfo.setSessionId(executeParams.getConversationId());
            sessionInfo.setUserId(executeParams.getUserId());

            Long startTime = Objects.nonNull(executeParams.getStartTime())
                ? executeParams.getStartTime()
                : System.currentTimeMillis();
            sessionInfo.setStartTime(startTime);
            sessionInfo.setResourceId(Optional.ofNullable(executeParams.getWorkflowId()).orElse(""));
            sessionInfo.setPlatform("WORKFLOW");

            sessionInfo.setCallType(executeParams.getTraceMode());

            UploadSessionDataReq uploadSessionDataReq = new UploadSessionDataReq();
            uploadSessionDataReq.setSessionInfoList(List.of(sessionInfo));
            invokeAgentOpsSession(uploadSessionDataReq);
        } catch (Exception e) {
            log.error("workflow session handler error", e);
        }
    }

    public void multiAgentTraceHandler(AgentExecuteParams executeParams, JiuwenEvent jiuwenEvent, boolean runEnd) {
        // 如果agent运行结束，清理缓存并上报UserSpan节点
        if (runEnd) {
            traceTaskExecutor.execute(() -> {
                uploadControllerSpanToOps(executeParams);
            });
            return;
        }

        // 判断事件是否需要上报ops
        if(!isTracedEvent(jiuwenEvent)){
            return;
        }

        // 将jiuwen事件添加到队列
        String traceId = executeParams.getExecutionId();
        TraceInfo traceInfo = getTraceInfoFromCache(traceId);
        traceInfo.getJiuwenEventList().add(jiuwenEvent);

        // 首Span时需要记录traceInfo的基本信息
        if (StringUtils.isBlank(traceInfo.getExecutionId())) {
            traceInfo.setDomainId(executeParams.getDomainId());
            traceInfo.setProjectId(executeParams.getProjectId());
            traceInfo.setWorkspaceId(executeParams.getWorkspaceId());
            traceInfo.setAgentId(executeParams.getAgentId());
            traceInfo.setConversationId(executeParams.getConversationId());
            traceInfo.setExecutionId(executeParams.getExecutionId());
            traceInfo.setUserId(executeParams.getUserId());
            traceInfo.setTraceMode(executeParams.getTraceMode());
            traceInfo.setLastBreakTime(executeParams.getStartTime());
            traceInfo.setPlatform("CONTROLLER");

            String rootSpanId = traceInfo.getExecutionId() + "root";
            traceInfo.setRootSpanId(rootSpanId);
        }
        redisClient.set(getTraceKey(traceId), JsonUtils.toJson(traceInfo), Duration.ofMinutes(10));
    }

    public void multiAgentTraceHandler(AgentExecuteParams executeParams, JiuwenAgentEvent jiuwenAgentEvent) {
        JiuwenAgentEventData eventData = jiuwenAgentEvent.getData();
        String traceId = executeParams.getExecutionId();
        TraceInfo traceInfo = getTraceInfoFromCache(traceId);
        if (StringUtils.isEmpty(eventData.getEndTime()) || traceInfo.isControllerNodeUpload()) {
            return;
        }
        traceInfo.setControllerNodeUpload(true);
        redisClient.set(getTraceKey(traceId), JsonUtils.toJson(traceInfo), Duration.ofMinutes(10));

        TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(executeParams);
        Span controllerSpan = new Span();
        controllerSpan.setSessionId(executeParams.getConversationId());
        controllerSpan.setTraceId(executeParams.getExecutionId());
        controllerSpan.setSpaceId(traceBaseInfo.getWorkspaceId());

        String rootSpanId = executeParams.getExecutionId() + "root";
        controllerSpan.setParentSpanId(rootSpanId);
        controllerSpan.setDomainId(traceBaseInfo.getDomainId());
        controllerSpan.setProjectId(traceBaseInfo.getProjectId());
        controllerSpan.setSpanId(UuidUtils.getUUID());
        controllerSpan.setSpanType("AgentController");
        controllerSpan.setSpanName("AgentController");

        try {
            JSONObject outPutObj = JSON.parseObject(JSONObject.toJSONString(eventData.getOutputs()));
            String modelUsage = outPutObj.getString("model_usage");
            TokensProps tokensProps = StringUtils.isNotEmpty(modelUsage)
                ? JSONObject.parseObject(modelUsage, TokensProps.class) : new TokensProps();
            controllerSpan.setTokensProps(tokensProps);

            Map<String, Object> metaData = eventData.getMetaData();
            Object instanceAttributes = ((Map<?, ?>) metaData).get("instance_attributes");
            if (instanceAttributes instanceof JSONObject) {
                String modelId = ((JSONObject) instanceAttributes).getString("model");
                ModelProps modelProps = queryModelProps(modelId);
                controllerSpan.setModelProps(modelProps);
                controllerSpan.getModelProps().setError(eventData.getError());
            }
        } catch (Exception e) {
            log.warn("assemble agent controller model info error", e);
        }
        controllerSpan.setUserId(executeParams.getUserId());
        controllerSpan.setResourceId(Optional.ofNullable(executeParams.getAgentId()).orElse(""));

        String status = Objects.isNull(eventData.getError()) ? "0" : "1";
        controllerSpan.setStatusCode(status);
        controllerSpan.setInput(JsonUtils.toJson(eventData.getInputs()));
        controllerSpan.setOutput(JsonUtils.toJson(eventData.getOutputs()));
        controllerSpan.setPlatform("CONTROLLER");

        LocalDateTime startDateTime = LocalDateTime.parse(eventData.getStartTime(),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        long startTime = startDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        controllerSpan.setStartTime(executeParams.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.parse(eventData.getEndTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        long endTime = endDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        controllerSpan.setDuration((int) (endTime - startTime));
        controllerSpan.setCallType(executeParams.getTraceMode());

        List<Span> spanList = new ArrayList<>();
        spanList.add(controllerSpan);

        // 上报trace到ops
        UploadTraceDataRequest uploadTraceDataRequest = new UploadTraceDataRequest();
        uploadTraceDataRequest.setSpanList(spanList);
        invokeAgentOpsTrace(uploadTraceDataRequest);
    }

    private TraceInfo getTraceInfoFromCache(String traceId) {
        TraceInfo traceInfo;
        String json = redisClient.get(getTraceKey(traceId));
        if (StringUtils.isEmpty(json)) {
            traceInfo = new TraceInfo();
            traceInfo.setJiuwenEventList(new ArrayList<>());
            return traceInfo;
        }
        traceInfo = JsonUtils.json2Obj(json, new TypeReference<>() {});
        return traceInfo;
    }

    private void uploadControllerSpanToOps(AgentExecuteParams executeParams) {
        try {
            TraceInfo traceInfo = getTraceInfoFromCache(executeParams.getExecutionId());
            if (CollectionUtils.isEmpty(traceInfo.getJiuwenEventList())) {
                return;
            }
            List<Span> spanList = buildSpanList(traceInfo.getJiuwenEventList(), traceInfo);

            Span userSpan = buildWorkflowRootSpan(traceInfo);
            spanList.add(userSpan);

            // 上报trace到ops
            UploadTraceDataRequest uploadTraceDataRequest = new UploadTraceDataRequest();
            uploadTraceDataRequest.setSpanList(spanList);
            invokeAgentOpsTrace(uploadTraceDataRequest);
        } catch (Exception e) {
            log.error("uploadControllerSpanToOps error", e);
        } finally {
            // 清理缓存
            String traceKey = getTraceKey(executeParams.getExecutionId());
            redisClient.delete(traceKey);
        }
    }

    private List<Map<String, Object>> getUserInvokeData(JiuwenEventData jiuwenEventData) {
        List<Map<String, Object>> userOrAssistantData = new ArrayList<>();
        List<Map<String, Object>> onInvokeData = jiuwenEventData.getOnInvokeData();
        for (Map<String, Object> map : onInvokeData) {
            if (map.containsKey("user")) {
                userOrAssistantData.add(map);
            }
        }
        return userOrAssistantData;
    }

    private List<Map<String, Object>> getOutputInvokeData(JiuwenEventData jiuwenEventData) {
        List<Map<String, Object>> outputInvokeData = new ArrayList<>();
        List<Map<String, Object>> onInvokeData = jiuwenEventData.getOnInvokeData();
        for (Map<String, Object> map : onInvokeData) {
            if (map.containsKey("assistant")) {
                outputInvokeData.add(map);
            }
        }
        outputInvokeData.add(jiuwenEventData.getOutputs());
        return outputInvokeData;
    }

    private String getTraceKey(String traceId) {
        return TRACE_ROOT_SPAN_PREFIX + traceId;
    }

    public long getTime(String timeString) {
        long time = System.currentTimeMillis();
        if (StringUtils.isNotEmpty(timeString)) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(timeString);
            time = offsetDateTime.toInstant().toEpochMilli();
        }
        return time;
    }

    /**
     * 调用Agent Ops Trace接口，上传跟踪数据。
     *
     * @param traceDataRequest 包含跟踪信息的请求对象
     */
    private void invokeAgentOpsTrace(UploadTraceDataRequest traceDataRequest) {
        RequestResult requestResult = okHttpUtils.call(agentOpsBaseUrl + agentOpsTraceApi, OkHttpUtils.Method.POST,
            null, JSON.toJSONString(traceDataRequest));

        if (!requestResult.isSuccess()) {
            // 如果请求失败，记录根span和请求结果
            log.error("invoke agent ops trace failed, span: {}, requestResult: {}",
                traceDataRequest.getSpanList().get(0), requestResult);
        }
    }

    /**
     * 调用Agent Ops Session接口，上报会话信息。
     *
     * @param uploadSessionDataReq 包含会话信息的请求对象
     */
    private void invokeAgentOpsSession(UploadSessionDataReq uploadSessionDataReq) {
        RequestResult requestResult = okHttpUtils.call(agentOpsBaseUrl + agentOpsSessionApi, OkHttpUtils.Method.POST,
            null, JSON.toJSONString(uploadSessionDataReq));

        if (!requestResult.isSuccess()) {
            // 如果请求失败，记录根session和请求结果
            log.error("invoke agent ops session failed, session: {}, requestResult: {}",
                uploadSessionDataReq.getSessionInfoList().get(0), requestResult);
        }
    }

    public void uploadAgentInteraction(AgentExecuteParams executeParams) {
        try {
            TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(executeParams);
            AgentInteractionReq agentInteractionReq = new AgentInteractionReq();
            agentInteractionReq.setDomainId(traceBaseInfo.getDomainId());
            agentInteractionReq.setProjectId(traceBaseInfo.getProjectId());
            agentInteractionReq.setSpaceId(traceBaseInfo.getWorkspaceId());
            agentInteractionReq.setSessionId(executeParams.getConversationId());
            agentInteractionReq.setTraceId(executeParams.getExecutionId());
            agentInteractionReq.setSpanId(UuidUtils.getUUID());
            agentInteractionReq.setUserId(executeParams.getUserId());
            agentInteractionReq.setResourceId(executeParams.getAgentId());
            agentInteractionReq.setStartTime(System.currentTimeMillis());

            handInputAndOutput(executeParams, agentInteractionReq);
            traceTaskExecutor.execute(() -> {
                invokeAgentOpsInteraction(agentInteractionReq);
            });
        } catch (Exception e) {
            log.error("uploadAgentInteraction error", e);
        }
    }

    private void handInputAndOutput(AgentExecuteParams executeParams, AgentInteractionReq inputAndOutput) {
        List<Message> messages = executeParams.getMessages();
        if (Strings.CI.equals("controller", executeParams.getExecuteType())) {
            Message input;
            Message output;
            int size = messages.size();
            if (size >= 2) {
                input = messages.get(size - 2);
            } else if (size == 1) {
                input = messages.get(0);
            } else {
                input = new Message();
            }
            // 获取多条消息输出
            List<Message> controllerMessages = executeParams.getControllerMessages();
            output = handleOutput(controllerMessages);

            Message newMessage = new Message();
            BeanUtils.copyProperties(input, newMessage);
            Map<String, Object> inputs = executeParams.getInputs();
            if (inputs == null) {
                inputs = new HashMap<>();
                inputs.put("query", executeParams.getQuery());
            }
            newMessage.setContent(JsonUtils.toJson(inputs));
            inputAndOutput.setInput(JsonUtils.toJson(newMessage));
            inputAndOutput.setOutput(JsonUtils.toJson(output));
        } else {
            if (CollectionUtils.isNotEmpty(messages)) {
                Message message = messages.get(0);
                Message newMessage = new Message();
                BeanUtils.copyProperties(message, newMessage);
                Map<String, Object> inputs = executeParams.getInputs();
                if (inputs == null) {
                    inputs = new HashMap<>();
                    inputs.put("query", executeParams.getQuery());
                }
                newMessage.setContent(JsonUtils.toJson(inputs));
                inputAndOutput.setInput(JsonUtils.toJson(newMessage));
            }
            if (CollectionUtils.isNotEmpty(messages) && messages.size() >= 2) {
                inputAndOutput.setOutput(JsonUtils.toJson(executeParams.getMessages().get(1)));
            }
        }
    }

    private Message handleOutput(List<Message> controllerMessages) {
        if (CollectionUtils.isEmpty(controllerMessages)) {
            return new Message();
        }
        Message defaultMessage = controllerMessages.get(controllerMessages.size() - 1);
        StringBuilder outputBuilder = new StringBuilder();
        for (Message message : controllerMessages) {
            if ("assistant".equals(message.getRole())) {
                outputBuilder.append(message.getContent());
            }
        }
        Message message = new Message();
        BeanUtils.copyProperties(defaultMessage, message);
        message.setContent(outputBuilder.toString());
        return message;
    }

    public void uploadWorkflowInteraction(ExecuteParams executeParams) {
        try {
            TraceBaseInfo traceBaseInfo = buildTraceBaseInfo(executeParams);
            AgentInteractionReq inputAndOutput = new AgentInteractionReq();
            inputAndOutput.setDomainId(traceBaseInfo.getDomainId());
            inputAndOutput.setProjectId(traceBaseInfo.getProjectId());
            inputAndOutput.setSpaceId(traceBaseInfo.getWorkspaceId());
            inputAndOutput.setSessionId(executeParams.getConversationId());
            inputAndOutput.setTraceId(executeParams.getExecutionId());
            inputAndOutput.setSpanId(UuidUtils.getUUID());
            inputAndOutput.setUserId(executeParams.getUserId());
            inputAndOutput.setResourceId(executeParams.getWorkflowId());
            inputAndOutput.setStartTime(System.currentTimeMillis());

            List<Message> messages = executeParams.getMessages();
            if (CollectionUtils.isNotEmpty(messages)) {
                Message message = messages.get(0);
                Message newMessage = new Message();
                BeanUtils.copyProperties(message, newMessage);
                newMessage.setContent(JsonUtils.toJson(executeParams.getInputs()));
                inputAndOutput.setInput(JsonUtils.toJson(newMessage));
            }
            if (CollectionUtils.isNotEmpty(messages) && messages.size() >= 2) {
                if (messages.size() == 2) {
                    inputAndOutput.setOutput(JsonUtils.toJson(executeParams.getMessages().get(1)));
                } else {
                    Message defaultMessage = messages.get(1);
                    // 对于多个输出的情况，进行叠加
                    StringBuilder output = new StringBuilder();
                    for (int index = 1; index < messages.size(); index++) {
                        Message message = messages.get(index);
                        if ("assistant".equals(message.getRole())) {
                            output.append(messages.get(index).getContent());
                        }
                    }
                    if ("assistant".equals(defaultMessage.getRole()) && StringUtils.isNotBlank(output.toString())) {
                        Message message = new Message();
                        BeanUtils.copyProperties(defaultMessage, message);
                        message.setContent(output.toString());
                        inputAndOutput.setOutput(JsonUtils.toJson(message));
                    } else {
                        inputAndOutput.setOutput(JsonUtils.toJson(defaultMessage));
                    }
                }

            }
            traceTaskExecutor.execute(() -> {
                invokeAgentOpsInteraction(inputAndOutput);
            });
        } catch (Exception e) {
            log.error("uploadWorkflowInteraction error", e);
        }
    }

    private void invokeAgentOpsInteraction(AgentInteractionReq inputAndOutput) {
        RequestResult requestResult = okHttpUtils.call(agentOpsBaseUrl + agentOpsInteractionApi, OkHttpUtils.Method.POST,
            null, JSON.toJSONString(inputAndOutput));

        if (!requestResult.isSuccess()) {
            // 如果请求失败，记录根session和请求结果
            log.error("invoke agent ops input and output failed, inputAndOutput: {}, requestResult: {}",
                inputAndOutput.toString(), requestResult);
        }
    }
}

