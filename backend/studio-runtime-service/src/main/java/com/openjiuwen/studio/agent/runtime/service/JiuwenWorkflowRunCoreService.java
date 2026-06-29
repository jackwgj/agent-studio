/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.REQUEST_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.TASK_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.X_REQUEST_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.X_TASK_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.Agent.INVOKE_HEADER_KEY;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.AgentStringUtils;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.ConversationHistory;
import com.openjiuwen.studio.agent.runtime.dto.ErrorEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenComponentExecutionRequest;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenExecutionRequest;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenParams;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenParamsLlmExtraConfigs;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenPluginConfig;
import com.openjiuwen.studio.agent.runtime.dto.LongTermMemoryRuntime;
import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.runtime.dto.MessageEvent;
import com.openjiuwen.studio.agent.common.dto.run.PluginConfig;
import com.openjiuwen.studio.agent.common.dto.agent.Status;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunInfo;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunStreamRsp;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;
import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsChannel;
import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventEntity;
import com.openjiuwen.studio.agent.runtime.entity.analytics.AnalyticsEventType;
import com.openjiuwen.studio.agent.runtime.enums.JiuwenEventType;
import com.openjiuwen.studio.agent.runtime.enums.WorkflowRunStatus;
import com.openjiuwen.studio.agent.runtime.enums.WorkflowStreamEventEnum;
import com.openjiuwen.studio.agent.runtime.model.Conversation;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.model.WorkflowRunResult;
import com.openjiuwen.studio.agent.runtime.model.memory.LongTermMemoryRefreshEvent;
import com.openjiuwen.studio.agent.runtime.sensitive.SensitiveTrieUtils;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;
import com.openjiuwen.studio.agent.runtime.service.memory.UserMemoryCacheMgmtService;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.runtime.utils.AlarmLogUtil;
import com.openjiuwen.studio.agent.runtime.utils.EnvVariablesUtils;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;
import com.openjiuwen.studio.agent.runtime.utils.PerformUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.utils.SseEmitterUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 九问工作流运行接口
 *
 */
@Slf4j
@Component("JiuwenWorkflowRunCoreService")
public class JiuwenWorkflowRunCoreService implements IWorkflowRunCoreService {
    private static final String QUERY_FIELD = "query";

    private static final String PARAMS_FIELD = "params";

    /**
     * 任务型工作流query参数为空时mock字段
     */
    private static final String QUERY_DEFAULT = "default";

    @Value("${jiuwen.base-url}")
    private String baseUrl;

    @Value("${jiuwen.run-api:/v1/orchestration/ir/execute}")
    private String runApi;

    @Value("${jiuwen.node-execute-api:/v1/orchestration/ir/component/%s/execute}")
    private String nodeExecuteApi;

    @Value("${workflow.sse-timeout-milliseconds}")
    private long workflowSseTimeoutMilliSec;

    @Value("${jiuwen.model-params:}")
    private String modelParams;

    @Value(
        "${jiuwen.exclude-headers:Content-Type,Content-Length,Host,User-Agent,Accept,Accept-Encoding,Connection,X-Auth-Token,X-Workspace-Id,X-Owner-Domain-Id,X-Owner-Project-Id}")
    private String excludeHeaders;

    @Value("${log.mask:true}")
    private boolean isMask;

    @Value("${heartbeat.pool-size:150}")
    private int heartbeatThreadPoolSize;

    @Value("${heartbeat.interval-seconds:50}")
    private long heartbeatInterval;

    @Value("${heartbeat.switch-mode:debug}")
    private String heartbeatSwitchMode;


    @Value("${lite.api-code.enabled:false}")
    private boolean isApiKeyCheckEnabled;

    private Set<String> excludeHeaderSet;

    @Autowired
    private WorkflowInstanceService workflowInstanceService;

    @Autowired
    private WorkflowIrCacheService workflowIrService;

    @Autowired
    private OkHttpUtils okHttpUtils;

    @Autowired
    private JiuwenEventProcessor eventProcessor;

    @Autowired
    private AnalyticsEventService analyticsEventService;

    @Autowired
    private ConversationManagementService conversationManagementService;

    @Autowired
    private AgentOpsService opsService;

    @Resource
    private UserMemoryCacheMgmtService userMemoryCacheMgmtService;

    @Autowired
    private RuntimeModelApiLogService apiLogService;

    private ExecutorService heartbeatThreadPool;

    @Autowired
    private I18nUtil i18nUtil;

    @Autowired
    private RuntimeI18nService runtimeI18nService;

    @Autowired
    private EnvVariablesUtils variablesUtils;

    @Autowired
    private AlarmLogUtil alarmLogUtil;

    @PostConstruct
    private void init() {
        if (!StringUtils.isEmpty(excludeHeaders)) {
            excludeHeaderSet = Arrays.stream(excludeHeaders.split(","))
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        } else {
            excludeHeaderSet = new HashSet<>();
        }
        heartbeatThreadPool = Executors.newFixedThreadPool(heartbeatThreadPoolSize);

        if (isApiKeyCheckEnabled) {
            excludeHeaderSet.add("authorization");
        }
    }

    /**
     * 是否打开心跳
     *
     * @param executeParams
     * @return
     */
    private boolean isOpenHeartbeat(ExecuteParams executeParams) {
        if (!StringUtils.isEmpty(heartbeatSwitchMode) && executeParams.isStream()) {
            if (Constant.Heartbeat.ALWAYS.equalsIgnoreCase(heartbeatSwitchMode)) {
                return true;
            }
            if (Constant.Heartbeat.DEBUG.equalsIgnoreCase(heartbeatSwitchMode) && executeParams.isDebug()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 运行工作流
     *
     * @param executeParams 运行参数
     * @return 返回运行结果
     */
    @Override
    public WorkflowRunResult runWorkflow(ExecuteParams executeParams, Conversation conversation) {
        // 1.创建工作流实例，用于持久化信息
        WorkflowInstanceEntity instance = initWorkflowInst(executeParams);

        // 创建工作流运行信息，承载工作流运行信息，用于事件发送
        WorkflowRunInfo workflowRunInfo = new WorkflowRunInfo();
        workflowRunInfo.setStartTime(instance.getStartTime());

        // 初始化工作流运行结果
        WorkflowRunResult result = new WorkflowRunResult();
        result.setWorkflowRunInfo(workflowRunInfo);
        result.setInstance(instance);

        // 2.发送请求和处理流式事件
        CountDownLatch latch = new CountDownLatch(1);
        boolean success = requestAndProcess(executeParams, conversation, result, latch);

        // 3.如果非流式，等待请求完成
        if (!executeParams.isStream() && success) {
            try {
                // 异步请求超时时间单独控制
                if (executeParams.getAsyncTaskParamHolder() != null && executeParams.getAsyncTaskParamHolder()
                    .isAsync()) {
                    latch.await(executeParams.getAsyncTaskParamHolder().getTimeout(), TimeUnit.MILLISECONDS);
                } else {
                    latch.await(workflowSseTimeoutMilliSec, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                throw new AgentStudioException(StudioError.WORKFLOW_RUN_TIMEOUT);
            }

            // 记录分析事件
            addWorkflowExec(AnalyticsEventType.COMPLETE, executeParams);
        }
        return result;
    }

    private boolean requestAndProcess(ExecuteParams executeParams, Conversation conversation, WorkflowRunResult result,
        CountDownLatch latch) {
        List<Message> messageList = conversation.getMessageList();

        // 敏感词输入匹配
        if (handleInputSensitiveMatch(executeParams, result, latch)) {
            return true;
        }

        // 记录输入输出文本
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        if (apiLog == null) {
            apiLog = new ModelApiLog();
        }
        apiLog.setStartTime(executeParams.getStartTime())
            .setUserId(executeParams.getUserId())
            .setDomainId(executeParams.getDomainId())
            .setModelId(executeParams.getWorkflowId())
            .setModelName(Constant.AppType.WORKFLOW)
            .setRequestId(MDC.get(REQUEST_ID));
        if (executeParams.getInputs().containsKey(Constant.Jiuwen.USER_MSG_FIELD)) {
            apiLog.setInput(executeParams.getInputs().get(Constant.Jiuwen.USER_MSG_FIELD).toString());
        } else {
            apiLog.setInput("");
        }
        executeParams.setApiLog(apiLog);

        // 1.构建请求参数以及url
        String url;
        String body;
        if (executeParams.isNodeExecute()) {
            url = baseUrl + String.format(nodeExecuteApi, executeParams.getNodeId());
            JiuwenComponentExecutionRequest jiuwenComponentExecutionRequest = buildJiuWenComponentExecutionRequest(
                executeParams, messageList);
            executeParams.setIrPath(jiuwenComponentExecutionRequest.getIrPath());
            body = JSONObject.toJSONString(jiuwenComponentExecutionRequest);
        } else {
            url = baseUrl + runApi;
            JiuwenExecutionRequest jiuwenExecutionRequest = buildJiuwenRequest(executeParams, messageList);
            jiuwenExecutionRequest.setDialogueCount(Math.max(1L, conversation.getDialogueCount()));
            executeParams.setIrPath(jiuwenExecutionRequest.getIrPath());
            body = JSONObject.toJSONString(jiuwenExecutionRequest);
        }

        // 2.构建请求头
        Map<String, String> header = buildHeader(executeParams);

        // 记录引擎开始时间
        PerformUtils.record(executeParams, Constant.Performance.ENGINE_START, System.currentTimeMillis());

        // 异步请求使用单独的连接池，并保存连接信息方便释放
        if (executeParams.getAsyncTaskParamHolder() != null && executeParams.getAsyncTaskParamHolder().isAsync()) {
            return okHttpUtils.stream(url, header, body, getEventListener(executeParams, result, latch),
                executeParams.getAsyncTaskParamHolder());
        } else {
            return okHttpUtils.stream(url, header, body, getEventListener(executeParams, result, latch));
        }
    }

    private EventSourceListener getEventListener(ExecuteParams executeParams, WorkflowRunResult result,
        CountDownLatch latch) {
        WorkflowInstanceEntity instance = result.getInstance();
        WorkflowRunInfo workflowRunInfo = result.getWorkflowRunInfo();

        SseEmitter sseEmitter = executeParams.getSseEmitter();
        String xAuthToken = RequestContextUtils.getRequestAuthToken();
        String language = RequestHeaderHolderUtils.getRequestLanguage();
        Map<String, String> curHeaders = RequestContextUtils.getHeaders();
        String requestId = MDC.get(REQUEST_ID);
        String taskId = MDC.get(TASK_ID);
        return new EventSourceListener() {
            private boolean isClose;

            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                long start = System.currentTimeMillis();
                MDC.put(REQUEST_ID, requestId);
                MDC.put(TASK_ID, taskId);
                log.info("onopen");
                RequestContextUtils.setRequestAuthTokenAndUserId(xAuthToken, executeParams.getProjectId(),
                    executeParams.getUserId());
                RequestContextUtils.setHeaders(curHeaders);
                RequestHeaderHolderUtils.setRequestLanguage(language);
                PerformUtils.accumulation(executeParams, Constant.Performance.MANAGER,
                    System.currentTimeMillis() - start);

                sendMemoryRefreshEvent(executeParams, sseEmitter);

                // 发送心跳
                if (isOpenHeartbeat(executeParams)) {
                    executeParams.setHeartbeatFuture(heartbeatThreadPool.submit(() -> {
                        try {
                            while (System.currentTimeMillis() - start < workflowSseTimeoutMilliSec) {
                                Thread.sleep(heartbeatInterval * 1000L);
                                WorkflowRunStreamRsp heartBeatEvent = new WorkflowRunStreamRsp().setEvent(
                                        WorkflowStreamEventEnum.HEARTBEAT.name().toLowerCase(Locale.ROOT))
                                    .setCreatedTime(System.currentTimeMillis());
                                SseEmitterUtils.sendEvent(sseEmitter, heartBeatEvent);
                            }
                        } catch (Exception e) {
                            log.info("heartbeat end: {}.", e.getMessage());
                        }
                    }));
                }

            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type,
                @NotNull String data) {
                long start = System.currentTimeMillis();
                MDC.put(REQUEST_ID, requestId);
                MDC.put(TASK_ID, taskId);
                PerformUtils.recordFirstEvent(executeParams, Constant.Performance.FIRST_EVENT, start);
                JiuwenEventType eventType = eventProcessor.process(data, executeParams, result, eventSource);
                PerformUtils.accumulation(executeParams, Constant.Performance.MANAGER,
                    System.currentTimeMillis() - start);
            }

            /**
             * 事件异常处理，处理异常会走这里
             *
             * @param eventSource event类容
             * @param t 异常
             * @param response 响应
             */
            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t,
                @Nullable Response response) {
                long start = System.currentTimeMillis();
                RequestContextUtils.setRequestAuthTokenAndUserId(xAuthToken, executeParams.getProjectId(),
                    executeParams.getUserId());
                RequestContextUtils.setHeaders(curHeaders);
                executeParams.setSuccess(-1);
                PerformUtils.recordFirstEvent(executeParams, Constant.Performance.FIRST_EVENT, start);
                MDC.put(REQUEST_ID, requestId);
                MDC.put(TASK_ID, taskId);

                if (executeParams.isCanceled()) {
                    closed("canceled.");
                } else {
                    failure(t, response);
                }

                // 添加事件
                addWorkflowExec(AnalyticsEventType.FAIL, executeParams);

                long end = System.currentTimeMillis();
                PerformUtils.accumulation(executeParams, Constant.Performance.MANAGER, end - start);
                PerformUtils.record(executeParams, Constant.Performance.ENGINE_END, end);
                PerformUtils.record(executeParams, Constant.Performance.END, end);
                PerformUtils.workflowLog(executeParams);
                MDC.put(REQUEST_ID, null);

                ModelApiLog apiLog = executeParams.getApiLog();
                apiLog.setReason(workflowRunInfo.getErrorMessage()).setStatus("failed");
                apiLogService.saveSecurityCenterLog(apiLog);
                RequestHeaderHolderUtils.clear();
            }

            /**
             * 流式接口结束，正常结束时会走到这里
             *
             * @param eventSource 事件源JiuwenEventProcessor
             */
            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                if (isClose) {
                    log.info("Jiuwen request is closed.");
                } else {
                    sendMemoryRefreshEvent(executeParams, sseEmitter);
                    doClose("onClosed");
                }
                eventProcessor.processWorkflowInteraction(executeParams);
                RequestHeaderHolderUtils.clear();
            }

            private void doClose(String eventType) {
                long start = System.currentTimeMillis();
                PerformUtils.recordFirstEvent(executeParams, Constant.Performance.FIRST_EVENT, start);
                MDC.put(REQUEST_ID, requestId);
                MDC.put(TASK_ID, taskId);
                closed(eventType);

                // 添加事件
                addWorkflowExec(AnalyticsEventType.COMPLETE, executeParams);
                long end = System.currentTimeMillis();
                PerformUtils.accumulation(executeParams, Constant.Performance.MANAGER, end - start);
                PerformUtils.record(executeParams, Constant.Performance.ENGINE_END, end);
                PerformUtils.record(executeParams, Constant.Performance.END, end);
                PerformUtils.workflowLog(executeParams);
                MDC.put(REQUEST_ID, null);

                ModelApiLog apiLog = executeParams.getApiLog();
                apiLog.setReason(executeParams.isCanceled() ? "canceled by content review" : "")
                    .setStatus(executeParams.isCanceled() ? "failed" : "success");
                apiLogService.saveSecurityCenterLog(apiLog);
            }

            private void failure(@Nullable Throwable t, Response response) {
                ErrorEvent errorEvent = null;
                try {
                    log.error("process failed.", t);
                    errorEvent = runtimeI18nService.createErrorEvent(t, response);
                    errorEvent.setWorkflowId(executeParams.getWorkflowId());

                    Status status = WorkflowRunStatus.FAILED.getStatus();
                    if (t instanceof AgentStudioException) {
                        if (((AgentStudioException) t).getErrorCode() == StudioError.CLIENT_ABORT_REQUEST) {
                            status = WorkflowRunStatus.ABORTED.getStatus();
                        }
                    }

                    // 错误信息设置到实例表
                    instance.setStatus(status.getDesc());
                    instance.setErrorInfo(errorEvent.getMessage());

                    // 错误信息记录到WorkflowRunInfo
                    workflowRunInfo.setStatus(status);
                    workflowRunInfo.setErrorCode(errorEvent.getCode());
                    workflowRunInfo.setErrorMessage(errorEvent.getMessage());

                    // 非单节点调测模式，保存instance
                    if (!executeParams.isNodeExecute()) {
                        eventProcessor.saveInstance(workflowRunInfo, instance, executeParams, true);
                    }
                    alarmLogUtil.logAlarm("WORKFLOW", "workflow process failed.", workflowRunInfo.getErrorMessage());
                } catch (Throwable e) {
                    log.error("on failure failed.", e);
                    if (errorEvent == null) {
                        errorEvent = runtimeI18nService.createErrEvent(e, StudioError.JIUWEN_CONNECTION_EXCEPTION);
                    }
                    throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
                } finally {
                    if (executeParams.isStream()) {
                        try {
                            // 发送error事件
                            eventProcessor.sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                                WorkflowStreamEventFactory.error(errorEvent));
                            // 发送工作流结束事件
                            eventProcessor.processWorkflowEnd(executeParams, workflowRunInfo);
                            eventProcessor.sendDoneEvt(executeParams);
                            sseEmitter.completeWithError(t);
                        } catch (Exception e){
                            log.warn("Event processor exception. {}", e.getMessage());
                        }
                        if (executeParams.getHeartbeatFuture() != null) {
                            executeParams.getHeartbeatFuture().cancel(true);
                        }
                    } else {
                        latch.countDown();
                    }
                    conversationManagementService.updateConversation(executeParams, false);
                    logResult(workflowRunInfo);
                }
            }

            private void closed(String eventType) {
                try {
                    log.info("closed. eventType:{}", eventType);
                    // 非单节点调测模式，非done事件引发的关闭连接，在此保存instance和会话
                    if (!executeParams.isNodeExecute()) {
                        eventProcessor.saveInstance(workflowRunInfo, instance, executeParams, result.isTaskEnd());
                    }
                    conversationManagementService.updateConversation(executeParams, result.isWorkflowEnd());
                } catch (Exception e) {
                    log.error("on closed failed.", e);
                    throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
                } finally {
                    if (executeParams.isStream()) {
                        eventProcessor.sendDoneEvt(executeParams);
                        // 检测执行结果，失败则设置一个错误值
                        if (!executeParams.isNodeExecute() && Objects.equals(workflowRunInfo.getStatus().getCode(),
                            WorkflowRunStatus.FAILED.getStatus().getCode())) {
                            log.error("workflow run failed: {}", workflowRunInfo.getErrorMessage());
                            SseEmitterUtils.setFailureOnSseEmitter(sseEmitter,
                                new Throwable(workflowRunInfo.getErrorMessage()));
                        }
                        sseEmitter.complete();
                        if (executeParams.getHeartbeatFuture() != null) {
                            executeParams.getHeartbeatFuture().cancel(true);
                        }
                    } else {
                        latch.countDown();
                    }
                    logResult(workflowRunInfo);
                }
            }
        };
    }

    /**
     * 发送记忆更新事件
     *
     * @param executeParams
     * @param sseEmitter
     */
    private void sendMemoryRefreshEvent(ExecuteParams executeParams, SseEmitter sseEmitter) {
        try {
            String userId = executeParams.getUserId();
            String memoryRepoId = executeParams.getMemoryRepoId();
            String conversationId = executeParams.getConversationId();

            LongTermMemoryRefreshEvent longTermMemoryRefreshEvent
                = userMemoryCacheMgmtService.getStoredLongTermMemoryRefreshEvent(userId, memoryRepoId, conversationId);

            if (longTermMemoryRefreshEvent != null) {
                log.info("Exists stored memory refresh event. Send it.");
                sseEmitter.send(longTermMemoryRefreshEvent);
                userMemoryCacheMgmtService.deleteStoredLongTermMemoryRefreshEvent(userId, memoryRepoId, conversationId);
            }
        } catch (Exception e) {
            log.error("Send stored memory refresh event failed.", e);
        }
    }

    private boolean handleInputSensitiveMatch(ExecuteParams executeParams, WorkflowRunResult result,
        CountDownLatch latch) {
        SensitiveTrieUtils trieUtils = executeParams.getSensitiveTrieUtils();

        // 支持敏感词匹配条件（敏感词工具不为空 || 启用敏感词匹配 || query参数不为空）
        if (trieUtils == null || !trieUtils.isInputMatchEnabled() || !executeParams.getInputs()
            .containsKey(Constant.Jiuwen.USER_MSG_FIELD)) {
            return false;
        }
        WorkflowRunInfo workflowRunInfo = result.getWorkflowRunInfo();
        Pair<Integer, String> matchResult = trieUtils.handleSensitiveMatch(
            executeParams.getInputs().get(Constant.Jiuwen.USER_MSG_FIELD).toString(), true);

        // 敏感词匹配成功，构造 response 数据
        if (matchResult.getLeft() > 0) {
            if (!executeParams.isStream()) {
                Map<String, Object> map = new HashMap<>();
                map.put(Constant.Jiuwen.END_NODE_DEFAULT_OUTPUT_FIELD, matchResult.getRight());
                workflowRunInfo.setOutputs(map);
                workflowRunInfo.setStatus(WorkflowRunStatus.SUCCEEDED.getStatus());
                latch.countDown();
            } else {
                // 流式接口，发送匹配成功的事件
                MessageEvent messageEvent = new MessageEvent();
                messageEvent.setText(matchResult.getRight());
                messageEvent.setIndex(0);
                messageEvent.setNodeId("").setNodeType("").setNodeName("");
                messageEvent.setCreatedTime(System.currentTimeMillis());
                eventProcessor.sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                    WorkflowStreamEventFactory.message(messageEvent));

                messageEvent.setIsFinished(true);
                messageEvent.setText("");
                eventProcessor.sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                    WorkflowStreamEventFactory.message(messageEvent));

                workflowRunInfo.setEndTime(System.currentTimeMillis());
                workflowRunInfo.setStatus(WorkflowRunStatus.SUCCEEDED.getStatus());
                eventProcessor.sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                    WorkflowStreamEventFactory.workflowFinished(workflowRunInfo));

                eventProcessor.sendDoneEvt(executeParams);
                executeParams.getSseEmitter().complete();
            }
            return true;
        }
        return false;
    }

    private Map<String, String> buildHeader(ExecuteParams executeParams) {
        Map<String, String> header = new HashMap<>();
        header.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
        String authToken = RequestContextUtils.getRequestAuthToken();
        header.put(Constant.X_AUTH_TOKEN, authToken);
        header.put(X_REQUEST_ID, MDC.get(REQUEST_ID));
        if (!StringUtils.isEmpty(MDC.get(TASK_ID))) {
            header.put(X_TASK_ID, MDC.get(TASK_ID));
        }
        
        header.put("X-Workspace-Id", StringUtils.isNotEmpty(executeParams.getOwnerWorkspaceId())
            ? executeParams.getOwnerWorkspaceId()
            : opsService.handlerWorkspaceId(false, executeParams.getWorkflowId(), executeParams.getReleasedVersion()));
        header.put(Constant.OWNER_PROJECT_ID,
            executeParams.getOwnerProjectId() == null ? "" : executeParams.getOwnerProjectId());

        header.put("X-Owner-Domain-Id", executeParams.getOwnerDomainId()==null? "":executeParams.getOwnerDomainId());
        HttpHeaders httpHeaders = executeParams.getHttpHeaders();
        if (httpHeaders != null) {
            httpHeaders.forEach((key, value) -> {
                if (!CollectionUtils.isEmpty(value) && !excludeHeaderSet.contains(key.toUpperCase(Locale.ROOT))) {
                    header.put(key, String.join(",", value));
                }
            });
        }
        if (header.keySet().stream().noneMatch(key -> key.equalsIgnoreCase("x-call-type"))) {
            header.put("x-call-type", executeParams.getTraceMode());
        }
        if (header.keySet().stream().noneMatch(key -> key.equalsIgnoreCase(Constant.CUSTOM_USER_ID))) {
            header.put(Constant.CUSTOM_USER_ID, RequestContextUtils.getRequestUserId());
        }
        // 上报OpsAgent数据需要在调用engine时，使能debug
        if (executeParams.isTrace()) {
            header.put(INVOKE_HEADER_KEY.toLowerCase(Locale.ROOT), Constant.Common.INVOKE_MOD_DEBUG);
        }
        return header;
    }

    private void logResult(WorkflowRunInfo workflowRunInfo) {
        long cost = -1L;
        if (workflowRunInfo.getEndTime() != null && workflowRunInfo.getStartTime() != null
            && workflowRunInfo.getEndTime() > workflowRunInfo.getStartTime()) {
            cost = workflowRunInfo.getEndTime() - workflowRunInfo.getStartTime();
        }
        log.info("Workflow run status:{}, cost:{}.", JSONObject.toJSONString(workflowRunInfo.getStatus()), cost);
    }

    private WorkflowInstanceEntity initWorkflowInst(ExecuteParams executeParams) {
        WorkflowInstanceEntity workflowInstance = new WorkflowInstanceEntity();
        workflowInstance.setWorkflowId(executeParams.getWorkflowId());
        workflowInstance.setProjectId(executeParams.getProjectId());
        workflowInstance.setConversationId(executeParams.getConversationId());
        workflowInstance.setInputs(executeParams.getInputs());
        workflowInstance.setStatus(WorkflowRunStatus.RUNNING.getStatus().getDesc());
        workflowInstance.setStartTime(executeParams.getStartTime());
        workflowInstance.setEventList(new ArrayList<>());
        return workflowInstance;
    }

    private JiuwenExecutionRequest buildJiuwenRequest(ExecuteParams executeParams, List<Message> messageList) {
        JiuwenExecutionRequest jiuwenExecutionRequest = new JiuwenExecutionRequest();

        // 设置会话id
        jiuwenExecutionRequest.setConversationId(executeParams.getConversationId());

        // 设置ir路径
        if (!StringUtils.isEmpty(executeParams.getReleasedVersion())) {
            jiuwenExecutionRequest.setIrPath(workflowIrService.getIrPathWithVersion(executeParams.getWorkflowId(),
                executeParams.getReleasedVersion()));
        } else {
            jiuwenExecutionRequest.setIrPath(workflowIrService.getIrPath(executeParams.getWorkflowId()));
        }

        // 设置用户query，若请求参数缺少query字段，设置mock的query值，后续节点不引用query，保证工作流可运行。
        Object query = executeParams.getInputs().get(Constant.Jiuwen.USER_MSG_FIELD);
        if (query != null && StringUtils.isNotBlank(query.toString())) {
            jiuwenExecutionRequest.setQuery(query.toString());
        } else {
            jiuwenExecutionRequest.setQuery(QUERY_DEFAULT);
        }

        // 设计接口模式
        jiuwenExecutionRequest.setResponseMode(Constant.Jiuwen.RESPONSE_MODE_STREAMING);

        // 设置user_id
        jiuwenExecutionRequest.setUserId(Optional.ofNullable(executeParams.getInputsUserId())
            .filter(userId -> !userId.isEmpty())
            .orElse(executeParams.getUserId()));

        // 设置参数
        JiuwenParams jiuwenParams = new JiuwenParams();

        // 传递历史会话开关
        jiuwenParams.setEnableHistory(executeParams.isEnableHistory());
        jiuwenExecutionRequest.setParams(jiuwenParams);

        // 设置历史对话
        setConversationHis(messageList, jiuwenParams);

        // 设置全局参数
        setGlobalParams(executeParams, jiuwenParams);

        // 设置环境变量
        setEnvironmentVariables(executeParams, jiuwenParams);

        JSONObject req = null;
        if (isMask) {
            req = JSONObject.from(jiuwenExecutionRequest);
            req.put(QUERY_FIELD, AgentStringUtils.mask(jiuwenExecutionRequest.getQuery()));
            req.put(PARAMS_FIELD, AgentStringUtils.MASK_STR);
        }
        log.info("JiuWen request param: {}",
            req == null ? JSONObject.toJSONString(jiuwenExecutionRequest) : req.toString());

        // 设置模型参数
        setLlmParams(jiuwenParams);

        // 设置插件参数
        setPluginConfigs(executeParams, jiuwenParams);

        // 设置当前会话是否启用用户画像记忆
        jiuwenParams.setAppId(executeParams.getWorkflowId());
        jiuwenParams.setEnableMemoryRetrieve(Optional.ofNullable(executeParams.getLongTermMemoryRuntime())
            .map(LongTermMemoryRuntime::isEnableRetrieve)
            .orElse(false));
        jiuwenParams.setEnableMemoryExtract(Optional.ofNullable(executeParams.getLongTermMemoryRuntime())
            .map(LongTermMemoryRuntime::isEnableExtract)
            .orElse(false));

        return jiuwenExecutionRequest;
    }

    private JiuwenComponentExecutionRequest buildJiuWenComponentExecutionRequest(ExecuteParams executeParams,
        List<Message> messageList) {
        JiuwenComponentExecutionRequest jiuwenComponentExecutionRequest = new JiuwenComponentExecutionRequest();

        // 设置会话id
        jiuwenComponentExecutionRequest.setConversationId(executeParams.getConversationId());

        // 设置ir路径
        jiuwenComponentExecutionRequest.setIrPath(workflowIrService.getIrPath(executeParams.getWorkflowId()));

        // 设置用户输入
        jiuwenComponentExecutionRequest.setInputs(executeParams.getInputs());

        // 设置参数
        JiuwenParams jiuwenParams = new JiuwenParams();
        jiuwenComponentExecutionRequest.setParams(jiuwenParams);

        // 设置历史对话
        setConversationHis(messageList, jiuwenParams);

        // 设置userId
        jiuwenComponentExecutionRequest.setUserId(Optional.ofNullable(executeParams.getInputsUserId())
            .filter(userId -> !userId.isEmpty())
            .orElse(executeParams.getUserId()));

        // 设置全局参数
        setGlobalParams(executeParams, jiuwenParams);

        // 设置环境变量
        setEnvironmentVariables(executeParams, jiuwenParams);

        log.info("JiuWen component request param: {}", JSON.toJSONString(jiuwenComponentExecutionRequest));

        // 设置模型参数
        setLlmParams(jiuwenParams);

        // 设置插件参数
        setPluginConfigs(executeParams, jiuwenParams);

        jiuwenParams.setAppId(executeParams.getWorkflowId());

        return jiuwenComponentExecutionRequest;
    }

    private void setLlmParams(JiuwenParams jiuwenParams) {
        JiuwenParamsLlmExtraConfigs llmExtraConfigs = new JiuwenParamsLlmExtraConfigs();
        jiuwenParams.setLlmExtraConfigs(llmExtraConfigs);
        Map<String, Object> modelMap = JSONObject.parseObject(modelParams);
        llmExtraConfigs.setModelMap(modelMap);
    }

    private void setConversationHis(List<Message> messageList, JiuwenParams jiuwenParams) {
        List<ConversationHistory> historyList = new ArrayList<>();
        messageList.forEach(h -> {
            ConversationHistory history = new ConversationHistory();
            history.setRole(h.getRole());
            history.setContent(h.getContent());
            historyList.add(history);
        });
        jiuwenParams.setConversationHistory(historyList);
    }

    private void setPluginConfigs(ExecuteParams executeParams, JiuwenParams jiuwenParams) {
        List<PluginConfig> pluginConfigs = executeParams.getPluginConfigs();
        if (!CollectionUtils.isEmpty(pluginConfigs)) {
            List<JiuwenPluginConfig> jiuwenPluginConfigs = new ArrayList<>();
            jiuwenParams.setPluginConfigs(jiuwenPluginConfigs);
            pluginConfigs.forEach(pluginConfig -> {
                JiuwenPluginConfig jiuwenPluginConfig = new JiuwenPluginConfig();
                jiuwenPluginConfig.setPluginId(pluginConfig.getPluginId());
                jiuwenPluginConfig.setConfig(pluginConfig.getConfig());
                jiuwenPluginConfigs.add(jiuwenPluginConfig);
            });
        }
    }

    private void setGlobalParams(ExecuteParams executeParams, JiuwenParams jiuwenParams) {
        Map<String, Object> globalVariables = new HashMap<>();
        executeParams.getInputs().forEach((k, v) -> {
            if (!k.equals(Constant.Jiuwen.USER_MSG_FIELD)) {
                globalVariables.put(k, v);
            }
        });
        jiuwenParams.setGlobalVariables(globalVariables);
    }

    private void addWorkflowExec(AnalyticsEventType eventType, ExecuteParams executeParams) {
        analyticsEventService.addEvent(AnalyticsEventEntity.builder()
            .eventType(eventType.toString())
            .appType(Constant.AppType.WORKFLOW)
            .appId(executeParams.getWorkflowId())
            .projectId(executeParams.getProjectId())
            .userId(executeParams.getUserId())
            .channel(AnalyticsChannel.API.getText())
            .build());
    }

    /**
     * 设置环境变量
     *
     * @param executeParams
     * @param jiuwenParams
     */
    private void setEnvironmentVariables(ExecuteParams executeParams, JiuwenParams jiuwenParams) {
        Map<String, Object> envVars = variablesUtils.getEnvironmentVariables(executeParams);
        Object secretKeys = envVars.remove("_secretEnvKeys");
        jiuwenParams.setEnvironmentVariables(new HashMap<>(envVars));
        if (secretKeys instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> keys = (List<String>) secretKeys;
            jiuwenParams.setSecretEnvKeys(keys);
        }
    }
}
