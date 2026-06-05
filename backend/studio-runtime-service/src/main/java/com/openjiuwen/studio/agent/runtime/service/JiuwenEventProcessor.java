/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.TASK_ID;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.ErrorEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventDataInnerError;
import com.openjiuwen.studio.agent.common.dto.agent.Message;
import com.openjiuwen.studio.agent.runtime.dto.MessageEvent;
import com.openjiuwen.studio.agent.runtime.dto.NodeMessage;
import com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfo;
import com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfoInnerError;
import com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfoInnerErrorErrorBody;
import com.openjiuwen.studio.agent.common.dto.agent.Status;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunInfo;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunStreamRsp;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;
import com.openjiuwen.studio.agent.runtime.enums.JiuwenEventType;
import com.openjiuwen.studio.agent.runtime.enums.MessageRole;
import com.openjiuwen.studio.agent.runtime.enums.WorkflowRunStatus;
import com.openjiuwen.studio.agent.runtime.event.SensitiveMessageEvent;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;
import com.openjiuwen.studio.agent.runtime.model.WorkflowRunResult;
import com.openjiuwen.studio.agent.runtime.sensitive.SensitiveTrie;
import com.openjiuwen.studio.agent.runtime.sensitive.SensitiveTrieUtils;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelApiLogService;
import com.openjiuwen.studio.agent.runtime.service.memory.UserMemoryCacheMgmtService;
import com.openjiuwen.studio.agent.runtime.utils.AlarmLogUtil;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.utils.SseEmitterUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSource;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Jiuwen流式响应Event处理
 *
 */
@Component
@Slf4j
public class JiuwenEventProcessor {
    private static final String USER_FIELDS = "userFields";

    private static final String SYSTEM_FIELDS = "systemFields";

    private static final String ENVIRONMENT_FIELDS = "environmentFields";

    // 节点首次运行时间；如大模型节点，表示真正请求的时间
    private static final String STARTUP_TIME = "requestStartTime";

    // 节点首次响应时间；如大模型节点，表示首token时间
    private static final String PREFILL_TIME = "firstTokenTime";

    // 提问器历史输入key值
    private static final List<String> MESSAGE_ROLE = List.of("user", "assistant", "system");

    // 异常节点做处理的node_id
    private static final String JIUWEN_EXCEPTION_NODE_ID = "jiuwen_exception_node_id";

    // 异常节点的data字段
    private static final String DATA_EXCEPTION = "dataException";

    // 流式消息组件的type
    private static final String JIUWEN_STREAM_TRANSFORM = "jiuwen.streamTransform";

    // 交互式节点（提问器，input）
    private static final List<String> INTERACTION_NODE = List.of(NodeType.QUESTIONER.getEiType(),
        NodeType.INPUT.getEiType(), NodeType.QA.getEiType());

    private static final List<JiuwenEventType> TO_RECORD_EVENTS = Arrays.asList(JiuwenEventType.START,
        JiuwenEventType.WORKFLOW_START, JiuwenEventType.DONE, JiuwenEventType.WORKFLOW_END,
        JiuwenEventType.WORKFLOW_NODE_MESSAGE, JiuwenEventType.EXCEPTION);

    private List<String> logEvents;

    private static int maxMsgLength = 10000;

    @Autowired
    private WorkflowInstanceService instanceService;

    @Autowired
    private WorkflowIrCacheService workflowIrService;

    @Autowired
    private ConversationManagementService conversationManagementService;

    @Value("${workflow.log-event:}")
    private String workflowLogEvent;

    @Autowired
    private RuntimeModelApiLogService apiLogService;

    @Resource
    private UserMemoryCacheMgmtService userMemoryCacheMgmtService;

    @Resource
    @Qualifier("traceTaskExecutor")
    private ThreadPoolTaskExecutor traceTaskExecutor;

    @Value("${redis.max_debug_msg_size:10000}")
    private int maxDebugMsgLength;

    @Resource
    private AgentOpsService agentOpsService;

    @Autowired
    private KnowledgeBaseFileInfoService knowledgeBaseFileInfoService;

    @Autowired
    private RuntimeI18nService i18nService;

    @Autowired
    private AlarmLogUtil alarmLogUtil;

    @Value("${workflow.finish-keep-text:false}")
    private boolean finishKeepText;

    @PostConstruct
    public void postConstruct() {
        JiuwenEventProcessor.maxMsgLength = maxDebugMsgLength;
    }

    /**
     * 九问节点类型转换为EI节点类型
     *
     * @param eventData 九问事件
     * @return NodeRunInfo
     */
    public static NodeRunInfo convertNodeRunInfo(JiuwenEventData eventData) {
        NodeRunInfo nodeRun = new NodeRunInfo();
        nodeRun.setNodeId(eventData.getComponentId());
        nodeRun.setParentNodeId(eventData.getParentNodeId());
        nodeRun.setExecutionId(eventData.getTraceId());
        nodeRun.setNodeName(eventData.getComponentName());
        NodeType nodeType = NodeType.fromInsight(eventData.getComponentType());

        // 知识库类型后台插件逻辑实现，映射为插件类型
        nodeRun.setNodeType(nodeType == NodeType.KNOWLEDGE_REPO ? NodeType.PLUGIN.getEiType() : nodeType.getEiType());
        nodeRun.setParentWorkflowId(eventData.getAgentParentInvokeId());
        nodeRun.setInputs(parseParams(eventData.getInputs()));

        nodeRun.setOutputs(parseParams(eventData.getOutputs()));
        nodeRun.setMetadata(eventData.getMetaData());
        nodeRun.setAgentId(eventData.getAgentId());
        nodeRun.setMemory(eventData.getMemory());

        // 循环节点相关参数
        nodeRun.setLoopNodeId(eventData.getLoopNodeId());
        nodeRun.setLoopIndex(eventData.getLoopIndex());

        // onInvokeData字段处理
        if (!CollectionUtils.isEmpty(eventData.getOnInvokeData())) {
            convertJiuwenInvokeData(nodeRun, eventData.getOnInvokeData());
        }

        // 时间日期转换格式
        if (StringUtils.isNotEmpty(eventData.getStartTime())) {
            OffsetDateTime offsetStartTime = OffsetDateTime.parse(eventData.getStartTime());
            nodeRun.setStartTime(offsetStartTime.toInstant().toEpochMilli());
        }
        if (StringUtils.isNotEmpty(eventData.getEndTime())) {
            OffsetDateTime offsetEndTime = OffsetDateTime.parse(eventData.getEndTime());
            nodeRun.setEndTime(offsetEndTime.toInstant().toEpochMilli());
        }

        // 时间日期转换格式. 调测接口真正启动的时间
        if (!Objects.isNull(eventData.getOutputs()) && !Objects.isNull(eventData.getOutputs().get(STARTUP_TIME))
            && StringUtils.isNotEmpty(eventData.getOutputs().get(STARTUP_TIME).toString())) {
            nodeRun.setStartupTime(
                OffsetDateTime.parse(eventData.getOutputs().get(STARTUP_TIME).toString()).toInstant().toEpochMilli());
        }

        // 时间日期转换格式. 预热时间，如大模型节点表示首token时间
        if (!Objects.isNull(eventData.getOutputs()) && !Objects.isNull(eventData.getOutputs().get(PREFILL_TIME))
            && StringUtils.isNotEmpty(eventData.getOutputs().get(PREFILL_TIME).toString())) {
            nodeRun.setPrefillTime(
                OffsetDateTime.parse(eventData.getOutputs().get(PREFILL_TIME).toString()).toInstant().toEpochMilli());
        }

        // 节点状态从九问返回值解析转换
        switch (eventData.getStatus()) {
            case START: {
                nodeRun.setStatus(new Status().setCode(0).setDesc("succeeded"));
                nodeRun.setNodeStatus(NodeRunInfo.NodeStatusEnum.STARTED);
                break;
            }
            case ERROR: {
                nodeRun.setNodeStatus(NodeRunInfo.NodeStatusEnum.FINISHED);
                if (eventData.getInnerError() != null) {
                    handleInnerError(nodeRun, eventData.getInnerError());
                } else {
                    nodeRun.setStatus(new Status().setCode(eventData.getError().getErrorCode()).setDesc("failed"));
                    nodeRun.setErrorMessage(eventData.getError().getMessage());
                }
                break;
            }
            case RUNNING: {
                nodeRun.setStatus(new Status().setCode(1).setDesc("waiting"));
                nodeRun.setNodeStatus(NodeRunInfo.NodeStatusEnum.WAIT);
                break;
            }
            case FINISH: {
                nodeRun.setStatus(new Status().setCode(0).setDesc("succeeded"));
                nodeRun.setNodeStatus(NodeRunInfo.NodeStatusEnum.FINISHED);
                handleInnerError(nodeRun, eventData.getInnerError());
                break;
            }
            default: {
                log.error(String.format("unexpect status type received: %s", eventData.getStatus()));
                nodeRun.setStatus(new Status().setCode(-1).setDesc("unknown type received"));
                nodeRun.setNodeStatus(NodeRunInfo.NodeStatusEnum.FINISHED);
                nodeRun.setErrorMessage(eventData.getError().getMessage());
            }
        }
        return nodeRun;
    }

    private static void handleInnerError(NodeRunInfo nodeRun, JiuwenEventDataInnerError innerError) {
        if (innerError == null) {
            return;
        }
        NodeRunInfoInnerError nodeRunInfoInnerError = new NodeRunInfoInnerError().setIsSuccess(
            innerError.isIsSuccess());
        if (Boolean.FALSE.equals(nodeRunInfoInnerError.isIsSuccess())) {
            nodeRun.setStatus(new Status().setCode(WorkflowRunStatus.FAILED.getStatus().getCode()).setDesc("failed"));
            nodeRun.setErrorMessage(innerError.getErrorBody().getErrorMessage());
            nodeRunInfoInnerError.setErrorBody(
                new NodeRunInfoInnerErrorErrorBody().setErrorCode(innerError.getErrorBody().getErrorCode())
                    .setErrorMessage(innerError.getErrorBody().getErrorMessage()));
        }
        nodeRun.setInnerError(nodeRunInfoInnerError);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseParams(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        if (params == null) {
            return result;
        }
        // 解析userFields和systemFields，平铺于inputs和outputs下
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if ((Objects.equals(entry.getKey(), USER_FIELDS) || Objects.equals(entry.getKey(), SYSTEM_FIELDS)
                || Objects.equals(entry.getKey(), ENVIRONMENT_FIELDS)) && entry.getValue() instanceof Map) {
                Map<String, Object> obj = (Map<String, Object>) entry.getValue();
                result.putAll(obj);
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    static void valueClipping(Object data) {
        if (!(data instanceof List)) {
            return;
        }
        listValueClipping((List<Object>) data);
    }

    @SuppressWarnings("unchecked")
    private static void listValueClipping(List<Object> invokeData) {
        int idx = 0;
        for (Object value : invokeData) {
            if (value instanceof List) {
                listValueClipping((List<Object>) value);
            } else if (value instanceof Map) {
                mapValueClipping((Map<String, Object>) value);
            } else if (value instanceof String) {
                invokeData.set(idx, StringUtils.abbreviate((String) value, maxMsgLength));
            }
            ++idx;
        }
    }

    @SuppressWarnings("unchecked")
    private static void mapValueClipping(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List) {
                listValueClipping((List<Object>) value);
            } else if (value instanceof Map) {
                mapValueClipping((Map<String, Object>) value);
            } else if (value instanceof String) {
                entry.setValue(StringUtils.abbreviate((String) value, maxMsgLength));
            }
        }
    }

    private static void convertJiuwenInvokeData(NodeRunInfo nodeRunInfo, List<Map<String, Object>> invokeData) {
        List<Message> messages = new ArrayList<>();
        Map<String, Object> metadata = nodeRunInfo.getMetadata() != null ? nodeRunInfo.getMetadata() : new HashMap<>();
        valueClipping(invokeData);
        for (Map<String, Object> invokeDatum : invokeData) {
            for (Map.Entry<String, Object> entry : invokeDatum.entrySet()) {
                if (MESSAGE_ROLE.contains(entry.getKey())) {
                    // message相关信息(提问器、消息节点)置于message字段下
                    messages.add(new Message().setRole(entry.getKey())
                        .setContent(entry.getValue() != null ? entry.getValue().toString() : StringUtils.EMPTY));
                } else {
                    // 非message相关信息包括大模型原始回复置于metadata中
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }
        }
        nodeRunInfo.setMessages(messages);
        if (!metadata.isEmpty()) {
            nodeRunInfo.setMetadata(metadata);
        }
    }

    /**
     * 初始化事件
     */
    @PostConstruct
    public void init() {
        logEvents = Arrays.stream(workflowLogEvent.split(Constant.SEPARATOR)).toList();
    }

    /**
     * 事件处理
     *
     * @param eventStr 事件数据
     * @param executeParams 执行参数
     * @param result 执行结果
     */
    public JiuwenEventType process(@NotNull String eventStr, ExecuteParams executeParams, WorkflowRunResult result,
        EventSource eventSource) {
        WorkflowInstanceEntity instance = result.getInstance();
        WorkflowRunInfo workflowRunInfo = result.getWorkflowRunInfo();
        // 针对异常节点转化结构
        eventStr = processOriginEvent(eventStr);
        JiuwenEvent eventObj = JSONObject.parseObject(eventStr, JiuwenEvent.class);
        executeParams.setExecutionId(eventObj.getExecutionId());
        String event = eventObj.getEvent();
        JiuwenEventType eventType;
        try {
            eventType = JiuwenEventType.valueOf(event.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            log.error("not support event:{}.", event);
            return JiuwenEventType.NOT_EXIST;
        }

        // 如果是exception事件，data内的参数不定，需要做预处理
        Map<String, String> dataMap = new HashMap<>();
        if (JiuwenEventType.EXCEPTION.equals(eventType)) {
            dataMap = processExceptionNodeData(eventObj);
        }

        // 记录九问事件，方便定位问题
        recordEvent(eventObj, eventType, instance);

        // error 日志信息屏蔽jiuwen
        if (instance !=null && StringUtils.isNotBlank(instance.getErrorInfo())) {
            instance.setErrorInfo(instance.getErrorInfo().replaceAll("(?i)(九问|jiuwen)", "Runtime"));
        }
        JiuwenEventData eventData = eventObj.getData();
        switch (eventType) {
            // 1.start Event不处理
            case START -> {
                processStart(executeParams, eventObj, result);
            }
            // 2.workflow_start event处理
            case WORKFLOW_START -> {
                processWorkflowStart(executeParams, workflowRunInfo);
            }
            // 3.message事件处理
            case MESSAGE -> {
                // 九问message事件转EI message事件
                processMessage(executeParams, eventObj);
                SensitiveTrieUtils.MatchResultWrapper resultWrapper = processSensitiveMatch(executeParams, eventObj,
                    false);

                // 存在兜底回复，主动结束流数据
                if (resultWrapper != null && (resultWrapper.getOp() & SensitiveTrie.SensitiveOp.REPLY.getCode()) > 0) {
                    processCancelStream(executeParams, eventObj, result);
                    eventSource.cancel();
                }
            }
            // 4.message_end事件处理
            case MESSAGE_END -> {
                processSensitiveMatch(executeParams, eventObj, true);
                processMessageEnd(executeParams, eventObj, result);
            }
            // 5.error事件处理
            case ERROR -> {
                result.setTaskEnd(true);
                // 九问messageEvent也返回message事件，状态为finished
                processError(executeParams, eventData, result);

                // 异常后九问不再发送workflow_finished,这里补一个
                processWorkflowEnd(executeParams, workflowRunInfo);

                ModelApiLog apiLog = executeParams.getApiLog();
                apiLog.setReason(workflowRunInfo.getErrorMessage()).setStatus("failed");
                apiLogService.saveSecurityCenterLog(apiLog);
            }
            // 5.workflow_end事件处理
            case WORKFLOW_END -> {
                result.setTaskEnd(true);
                result.setWorkflowEnd(true);
                processWorkflowEnd(executeParams, workflowRunInfo, result);
            }
            // 6.done事件处理
            case DONE -> {
                processDone(executeParams, result);
            }
            // 7.工作流调试信息事件处理
            case WORKFLOW_NODE_MESSAGE -> {
                // 九问workflow node message事件转EI workflow node message事件
                processWorkflowNodeMessage(executeParams, eventData, result);

                // 异步实时存储insight事件
                if (executeParams.getAsyncTaskParamHolder() != null && executeParams.getAsyncTaskParamHolder().isAsync()
                    && executeParams.isDebug()) {
                    instanceService.saveInsightMessage(result.getInstance(), executeParams.getReleasedVersion(), true);
                }
            }
            // 8.工作流异常节点信息适配
            case EXCEPTION -> {
                result.setTaskEnd(true);
                // 重新匹配数据
                processExceptionNode(executeParams, dataMap);
            }
            // 8.对话历史的统一消息处理
            case INTERMEDIATE_MESSAGE -> {
                processIntermediateMessage(eventObj, executeParams);
            }
            default -> log.info("not process event{}", event);
        }
        return eventType;
    }

    /**
     * 处理异常节点的初始信息
     */
    private Map<String, String> processExceptionNodeData(JiuwenEvent eventObj) {
        Map<String, String> dataMap =
                JsonUtils.json2Obj(eventObj.getDataException(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});

        if (dataMap == null || dataMap.isEmpty()) {
            return new HashMap<>();
        }

        dataMap.remove(JIUWEN_EXCEPTION_NODE_ID);
        return dataMap;
    }

    /**
     * 对话历史的统一消息处理
     *
     * @param eventObj eventObj
     * @param executeParams executeParams
     */
    private void processIntermediateMessage(JiuwenEvent eventObj, ExecuteParams executeParams) {
        List<Object> messageList = JsonUtils.objectToClass(eventObj.getData().getAnswer());
        ObjectMapper objectMapper = new ObjectMapper();
        List<Message> messages = new ArrayList<>();
        for (Object msg : messageList) {
            Message convertMsg = objectMapper.convertValue(msg, Message.class);
            messages.add(convertMsg);
        }
    }

    /**
     * 发送事件
     *
     * @param workflowId 工作流id
     * @param sseEmitter 流式接口对象
     * @param eventObj 流式事件对象
     */
    public void sendEvent(String workflowId, SseEmitter sseEmitter, WorkflowRunStreamRsp eventObj) {
        try {
            SseEmitterUtils.sendEvent(sseEmitter, eventObj);
        } catch (IOException e) {
            String errorInfo = String.format("when workflow: %s run, stream call execute error.", workflowId);
            log.error(errorInfo, e);
            throw new AgentStudioException(StudioError.CLIENT_ABORT_REQUEST);
        }
    }

    /**
     * 发送done事件
     *
     * @param executeParams 执行参数
     */
    public void sendDoneEvt(ExecuteParams executeParams) {
        if (!executeParams.isDone()) {
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(), WorkflowStreamEventFactory.end());
            executeParams.setDone(true);
        }
    }

    /**
     * 记录九问事件
     *
     * @param jiuwenEvent 九问事件
     * @param eventType 九问事件类型
     * @param instance 工作流实例
     */
    @SuppressWarnings("unchecked")
    void recordEvent(JiuwenEvent jiuwenEvent, JiuwenEventType eventType, WorkflowInstanceEntity instance) {
        // 九问原始事件日志打印
        if (logEvents.contains(eventType.toString().toUpperCase(Locale.ROOT))) {
            log.info("Receive eventType:[{}]", eventType);
        }
        if (TO_RECORD_EVENTS.contains(eventType)) {
            List<JiuwenEvent> eventList = instance.getEventList();
            if (eventList == null) {
                eventList = new ArrayList<>();
                instance.setEventList(eventList);
            }
            try {
                if ("jiuwen.setVariable".equals(jiuwenEvent.getData().getComponentType())) {
                    Map<String, Object> userFields = (Map<String, Object>) jiuwenEvent.getData()
                        .getInputs()
                        .get("userFields");
                    for (Map.Entry<String, Object> entry : userFields.entrySet()) {
                        if (entry.getValue() == null) {
                            entry.setValue("<null>");
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Fix set variable input fail.");
            }
            eventList.add(jiuwenEvent);
        }
    }

    /**
     * 处理开始事件
     *
     * @param eventObj 九问事件对象
     * @param result 运行结果
     */
    private void processStart(ExecuteParams executeParams, JiuwenEvent eventObj, WorkflowRunResult result) {
        WorkflowInstanceEntity instance = result.getInstance();
        if (StringUtils.isEmpty(eventObj.getExecutionId())) {
            log.warn("start event executionId is null!");
        }
        String instId = StringUtils.isEmpty(eventObj.getExecutionId()) ? MDC.get(TASK_ID) : eventObj.getExecutionId();
        instance.setId(instId);
        WorkflowInstanceEntity instanceEntity = instanceService.getCache(instId, executeParams.getReleasedVersion());
        if (instanceEntity == null && !executeParams.isNodeExecute()) { // 不存在保存
            // 非单节点调测模式触发保存
            instanceService.save(instance, executeParams);
        } else if (instanceEntity != null) { // 存在信息复制到instance
            instanceService.copy(instanceEntity, instance);
            WorkflowRunInfo workflowRunInfo = result.getWorkflowRunInfo();
            workflowRunInfo.setStartTime(instanceEntity.getStartTime());
            instance.setStatus(WorkflowRunStatus.RUNNING.getStatus().getDesc());
        }
        result.setInstance(instance);
    }

    /**
     * 处理workflow_start事件
     *
     * @param executeParams 执行参数
     * @param workflowRunInfo 工作流运行信息
     */
    private void processWorkflowStart(ExecuteParams executeParams, WorkflowRunInfo workflowRunInfo) {
        if (executeParams.isStream()) {
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                WorkflowStreamEventFactory.workflowStarted(workflowRunInfo));
        }

        // 上报会话数据
        if (executeParams.isTrace() && !executeParams.isOldConversation()) {
            traceTaskExecutor.execute(() -> {
                executeParams.setOldConversation(true);
                agentOpsService.workflowSessionHandler(executeParams);
            });
        }
    }

    /**
     * 处理消息事件
     *
     * @param executeParams 执行参数
     * @param eventObj 九问事件
     */
    private void processMessage(ExecuteParams executeParams, JiuwenEvent eventObj) {
        MessageEvent messageEvent = new MessageEvent();
        WorkflowIrWrapper ir = workflowIrService.getWorkflowIr(executeParams.getWorkflowId(),
            executeParams.getReleasedVersion());
        JiuwenEventData eventData = eventObj.getData();
        String text = extractAnswer(eventData);
        NodeType nodeType = NodeType.fromJiuwen(eventData.getNodeType());
        messageEvent.setText(text);
        messageEvent.setReasoningContent(eventData.getThink());
        messageEvent.setOriginal(eventData.getOriginalMessage());
        messageEvent.setIndex(eventObj.getIndex());
        messageEvent.setNodeId(eventData.getNodeId());
        messageEvent.setNodeType(nodeType.getEiType());
        messageEvent.setNodeName(getEventNodeName(ir, eventData));
        messageEvent.setCreatedTime(eventObj.getCreatedTime());
        messageEvent.setWorkflowId(executeParams.getWorkflowId());
        messageEvent.setWorkflowName(ir.getWorkflowName());

        // 判断历史会话是否为空
        if (eventData.isEnableHistory() != null) {
            messageEvent.setEnableHistory(eventData.isEnableHistory());
        }
        if (executeParams.isStream()) {
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                WorkflowStreamEventFactory.message(messageEvent));
        }
        String nodeId = eventData.getParentNodeId() == null
            ? eventData.getNodeId()
            : eventData.getParentNodeId() + "_" + eventData.getNodeId();
        StringBuilder msgBuilder = executeParams.getMessageMap()
            .computeIfAbsent(nodeId, k -> new StringBuilder());
        msgBuilder.append(text);

        executeParams.getApiLog()
            .getOutputMap()
            .computeIfAbsent(eventData.getNodeId(), k -> new StringBuilder())
            .append(text);
    }

    /**
     * 敏感词匹配
     *
     * @param executeParams 执行参数
     * @param eventObj 引擎返回数据
     */
    public SensitiveTrieUtils.MatchResultWrapper processSensitiveMatch(ExecuteParams executeParams,
        JiuwenEvent eventObj, boolean isFinished) {
        SensitiveTrieUtils trieUtils = executeParams.getSensitiveTrieUtils();
        if (trieUtils == null || !trieUtils.isOutputMatchEnabled() || !executeParams.isStream()) {
            return null;
        }
        JiuwenEventData eventData = eventObj.getData();
        SensitiveTrieUtils.MatchResultWrapper resultWrapper;

        // 先处理 think 的匹配
        String thinkId = eventData.getNodeId() + "_think";
        boolean isHandleThink = true;
        if (!isFinished && StringUtils.isNotEmpty(eventData.getThink())) {
            resultWrapper = trieUtils.handleOutputSensitiveMatch(eventData.getThink(), thinkId, false);
        } else if (trieUtils.isMatchingWithId(thinkId)) {
            // 结束 think 的匹配
            resultWrapper = trieUtils.handleOutputSensitiveMatch("", thinkId, true);
        } else {
            isHandleThink = false;
            String text = isFinished ? "" : extractAnswer(eventData);
            resultWrapper = trieUtils.handleOutputSensitiveMatch(text, eventData.getNodeId(), isFinished);
        }

        if (resultWrapper.getText() != null && resultWrapper.getOp() > 0) {
            WorkflowIrWrapper ir = workflowIrService.getWorkflowIr(executeParams.getWorkflowId(),
                executeParams.getReleasedVersion());
            SensitiveMessageEvent messageEvent = new SensitiveMessageEvent();
            messageEvent.setText(isHandleThink ? "" : resultWrapper.getText());
            messageEvent.setReasoningContent(isHandleThink ? resultWrapper.getText() : null);
            messageEvent.setOffset(resultWrapper.getOffset());
            messageEvent.setIndex(eventObj.getIndex());
            messageEvent.setNodeId(eventData.getNodeId());
            messageEvent.setNodeType(convertNodeType(eventData.getNodeType()));
            messageEvent.setNodeName(getEventNodeName(ir, eventData));
            messageEvent.setCreatedTime(eventObj.getCreatedTime());
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                WorkflowStreamEventFactory.sensitive(messageEvent));
        }
        return resultWrapper;
    }

    /**
     * 取消流请求处理
     *
     * @param executeParams 执行参数
     * @param eventObj 引擎返回数据
     * @param result 运行结果收集
     */
    public void processCancelStream(ExecuteParams executeParams, JiuwenEvent eventObj, WorkflowRunResult result) {
        JiuwenEventData eventData = eventObj.getData();
        WorkflowRunInfo workflowRunInfo = result.getWorkflowRunInfo();

        processMessageEnd(executeParams, eventObj, result);
        processWorkflowEnd(executeParams, workflowRunInfo);
        eventData.setStatus(JiuwenEventData.StatusEnum.FINISH);
        eventData.setComponentId(eventData.getNodeId());
        eventData.setComponentName(eventData.getNodeName());
        eventData.setComponentType(NodeType.toInsight(eventData.getNodeType()));
        processWorkflowNodeMessage(executeParams, eventData, result);
        executeParams.setCanceled(true);
    }

    /**
     * 处理message_end
     *
     * @param executeParams 执行参数
     * @param eventObj 九问事件数据
     * @param result 执行结果
     */
    void processMessageEnd(ExecuteParams executeParams, JiuwenEvent eventObj, WorkflowRunResult result) {
        WorkflowRunInfo workflowRunInfo = result.getWorkflowRunInfo();
        WorkflowIrWrapper ir = workflowIrService.getWorkflowIr(executeParams.getWorkflowId(),
            executeParams.getReleasedVersion());
        WorkflowInstanceEntity instance = result.getInstance();
        JiuwenEventData eventData = eventObj.getData();

        // 九问messageEvent也返回message事件，状态为finished
        MessageEvent messageEvent = new MessageEvent();
        String text = extractAnswer(eventData);
        messageEvent.setSummary(text);
        messageEvent.setText(finishKeepText ? text : "");

        String nodeId = eventData.getParentNodeId() == null
            ? eventData.getNodeId()
            : eventData.getParentNodeId() + "_" + eventData.getNodeId();

        if (Boolean.TRUE.equals(eventObj.isIsStructMessage())) {
            String origin = extractOriginAnswer(eventData);
            messageEvent.setOrigin(origin);

            StringBuilder msgBuilder = new StringBuilder(origin);
            executeParams.getMessageMap().put(nodeId, msgBuilder);
        }
        messageEvent.setNodeId(eventData.getNodeId());
        messageEvent.setNodeType(convertNodeType(eventData.getNodeType()));
        messageEvent.setNodeName(getEventNodeName(ir, eventData));
        messageEvent.setWorkflowName(ir.getWorkflowName());
        messageEvent.setWorkflowId(executeParams.getWorkflowId());
        messageEvent.setCreatedTime(eventObj.getCreatedTime());
        // 判断历史会话是否为空
        if (eventData.isEnableHistory() != null) {
            messageEvent.setEnableHistory(eventData.isEnableHistory());
            if (workflowRunInfo.getMetadata() == null) {
                workflowRunInfo.setMetadata(new HashMap<>());
            }
            workflowRunInfo.getMetadata().put("enable_history", eventData.isEnableHistory());
        }
        messageEvent.setIsFinished(true);

        // 助手回答记录到对话历史
        StringBuilder msgBuilder = executeParams.getMessageMap()
            .computeIfAbsent(nodeId, k -> new StringBuilder());
        Message assistantMsg = new Message();
        String finalMsg = msgBuilder.toString();
        // 添加相同子工作流存在内部节点ID相同的问题，需要清理已缓存的数据
        msgBuilder.setLength(0);

        if (executeParams.isStream()) {
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                WorkflowStreamEventFactory.message(messageEvent));
        }

        // 流式消息节点, 会话历史仅记录最后一帧数据
        if (JIUWEN_STREAM_TRANSFORM.equals(eventObj.getData().getNodeType())) {
            finalMsg = eventObj.getData().getAnswer().toString();
        }

        // 历史会话是否写入
        Boolean isHistoryEnabled = messageEvent.isEnableHistory();
        if (!StringUtils.isEmpty(finalMsg) && (isHistoryEnabled == null || isHistoryEnabled)) {
            assistantMsg.setRole(MessageRole.ASSISTANT.name().toLowerCase(Locale.ROOT))
                .setContent(finalMsg)
                .setExecutionId(eventObj.getExecutionId())
                .setNodeId(eventData.getNodeId())
                .setCreateTime(eventObj.getCreatedTime());
            executeParams.getMessages().add(assistantMsg);
        }

        // 结束节点特殊处理
        if (messageEvent.getNodeType().equalsIgnoreCase(NodeType.END.getEiType())) {
            // 非流式敏感词匹配
            SensitiveTrieUtils trieUtils = executeParams.getSensitiveTrieUtils();
            if (!executeParams.isStream() && trieUtils != null && trieUtils.isOutputMatchEnabled()) {
                finalMsg = trieUtils.handleSensitiveMatch(finalMsg, false).getRight();
            }
            Map<String, Object> map = new HashMap<>();
            map.put(Constant.Jiuwen.END_NODE_DEFAULT_OUTPUT_FIELD, finalMsg);
            map.putAll(parseParams(eventData.getOutputs()));
            instance.setOutputs(map);
            workflowRunInfo.setOutputs(map);
        } else {
            NodeMessage nodeMessage = new NodeMessage();
            nodeMessage.setNodeId(messageEvent.getNodeId());
            nodeMessage.setNodeName(messageEvent.getNodeName());
            nodeMessage.setNodeType(messageEvent.getNodeType());

            if (Boolean.TRUE.equals(eventObj.isIsStructMessage())) {
                text = extractAnswer(eventData);
                String origin = extractOriginAnswer(eventData);
                nodeMessage.setContent(text);
                nodeMessage.setOrigin(origin);
                nodeMessage.setRole(MessageRole.ASSISTANT.name().toLowerCase(Locale.ROOT));
            } else {
                if (!StringUtils.isEmpty(finalMsg)) {
                    nodeMessage.setContent(finalMsg);
                    nodeMessage.setRole(MessageRole.ASSISTANT.name().toLowerCase(Locale.ROOT));
                }
            }

            result.getMessageList().add(nodeMessage);
        }

        // 提问器节点
        if (INTERACTION_NODE.contains(messageEvent.getNodeType())) {
            instance.setStatus(WorkflowRunStatus.WAITING.getStatus().getDesc());
            workflowRunInfo.setStatus(WorkflowRunStatus.WAITING.getStatus());
        }
    }

    /**
     * 处理exception原始事件
     *
     * @param eventStr 原始事件信息
     */
    private String processOriginEvent(String eventStr) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            JsonNode rootNode = mapper.readTree(eventStr);

            if (rootNode.has("data") && rootNode.get("data").has(JIUWEN_EXCEPTION_NODE_ID)) {
                ObjectNode objectNode = (ObjectNode) rootNode;
                JsonNode dataNode = objectNode.get("data");
                objectNode.remove("data");
                objectNode.set(DATA_EXCEPTION, dataNode);
                return mapper.writeValueAsString(rootNode);
            } else {
                return eventStr;
            }
        } catch (Exception e) {
            log.error("transfer exception event error!");
            throw new AgentStudioException(StudioError.WORKFLOW_EXCEPTION_CONFIG_INVALID);
        }
    }

    /**
     * 处理error事件
     *
     * @param executeParams 执行参数
     * @param eventData 九问事件数据
     * @param result 执行结果
     */
    private void processError(ExecuteParams executeParams, JiuwenEventData eventData, WorkflowRunResult result) {
        WorkflowInstanceEntity instance = result.getInstance();
        WorkflowRunInfo workflowRunInfo = result.getWorkflowRunInfo();
        ErrorEvent errorEvent = i18nService.createWorkflowErrorEvent(executeParams.getWorkflowId(), eventData);

        // 错误信息设置到实例表
        instance.setStatus(WorkflowRunStatus.FAILED.getStatus().getDesc());
        instance.setErrorInfo(eventData.getMessage());

        // 错误信息记录到WorkflowRunInfo
        workflowRunInfo.setStatus(WorkflowRunStatus.FAILED.getStatus());
        workflowRunInfo.setErrorCode(eventData.getCode().toString());
        workflowRunInfo.setErrorMessage(errorEvent.getMessage());
        if (executeParams.isStream()) {
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                WorkflowStreamEventFactory.error(errorEvent));
        }
        alarmLogUtil.logAlarm("WORKFLOW", "workflow receive error evnt", workflowRunInfo.getErrorMessage());
    }

    /**
     * 处理workflow_end事件
     *
     * @param executeParams 执行参数
     * @param workflowRunInfo 工作流运行信息
     */
    public void processWorkflowEnd(ExecuteParams executeParams, WorkflowRunInfo workflowRunInfo) {
        processWorkflowEnd(executeParams, workflowRunInfo, null);
    }

    /**
     * 处理workflow_end事件
     *
     * @param executeParams 执行参数
     * @param workflowRunInfo 工作流运行信息
     * @param result 工作流运行结果
     */
    public void processWorkflowEnd(ExecuteParams executeParams, WorkflowRunInfo workflowRunInfo,
        WorkflowRunResult result) {
        workflowRunInfo.setEndTime(System.currentTimeMillis());
        if (workflowRunInfo.getStatus() == null || !WorkflowRunStatus.FAILED.getStatus()
            .equals(workflowRunInfo.getStatus())) {
            workflowRunInfo.setStatus(WorkflowRunStatus.SUCCEEDED.getStatus());
        }

        if (result != null && result.getInstance() != null) {
            WorkflowInstanceEntity instance = result.getInstance();
            if (StringUtils.isEmpty(instance.getStatus()) || !WorkflowRunStatus.FAILED.getStatus()
                .getDesc()
                .equalsIgnoreCase(instance.getStatus())) {
                instance.setStatus(WorkflowRunStatus.SUCCEEDED.getStatus().getDesc());
            }
            instance.setEndTime(workflowRunInfo.getEndTime());
        }
        if (executeParams.isStream()) {
            workflowRunInfo.setExecutionId(executeParams.getExecutionId());
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                WorkflowStreamEventFactory.workflowFinished(workflowRunInfo));
        }

        // 上报调用链数据
        if (executeParams.isTrace()) {
            if (result == null || result.getInstance() == null) {
                log.warn("Result or instance is null, it can not upload trace to ops");
                return;
            }
            try {
                if (!executeParams.isDebug()) {
                    String userId = RequestContextUtils.getRequestUserId();
                    // 清理缓存的工作流节点事件
                    instanceService.deleteExecution(userId, result.getInstance(), executeParams.getReleasedVersion());
                }
            } catch (Exception e) {
                log.warn("Fail clear trace tmp data. {}", e.getMessage());
            }
            traceTaskExecutor.execute(() -> {
                agentOpsService.workflowTraceHandler(result.getInstance(), executeParams, true);
            });
        }
    }

    public void processWorkflowInteraction(ExecuteParams executeParams) {
        if (executeParams.isTrace()) {
            agentOpsService.uploadWorkflowInteraction(executeParams);
        }
    }

    /**
     * 处理done事件
     *
     * @param executeParams 执行参数
     */
    private void processDone(ExecuteParams executeParams, WorkflowRunResult result) {
        if (executeParams.isStream()) {
            sendDoneEvt(executeParams);
        }
    }

    /**
     * 处理exception事件
     *
     * @param dataMap 执行参数
     */
    private void processExceptionNode(ExecuteParams executeParams, Map<String, String> dataMap) {
        if (executeParams.isStream()) {
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                WorkflowStreamEventFactory.exception(dataMap));
        }
    }

    /**
     * 处理workflow node message事件
     *
     * @param executeParams 执行参数
     * @param eventData 九问节点运行数据
     */
    private void processWorkflowNodeMessage(ExecuteParams executeParams, JiuwenEventData eventData,
        WorkflowRunResult result) {
        // NodeRunInfo数据构造
        NodeRunInfo nodeRunInfo = convertNodeRunInfo(eventData);

        // 调试模式额外发送insight事件
        if (executeParams.isDebug()) {
            result.getNodeRunInfoList().add(nodeRunInfo);
        }
        if (executeParams.isStream() && executeParams.isDebug()) {
            if (Objects.equals(nodeRunInfo.getNodeStatus().toString(), NodeRunInfo.NodeStatusEnum.STARTED.toString())) {
                sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                    WorkflowStreamEventFactory.nodeStart(nodeRunInfo));
            } else if (Objects.equals(nodeRunInfo.getNodeStatus().toString(),
                NodeRunInfo.NodeStatusEnum.WAIT.toString())) {
                sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                    WorkflowStreamEventFactory.nodeWait(nodeRunInfo));
            } else if (Objects.equals(nodeRunInfo.getNodeStatus().toString(),
                NodeRunInfo.NodeStatusEnum.FINISHED.toString())) {
                sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(),
                    WorkflowStreamEventFactory.nodeFinish(nodeRunInfo));
            }
        }
        if (Constant.KnowledgeRetrievalNode.PLUGIN.equals(nodeRunInfo.getNodeType())) {
            knowledgeBaseFileInfoService.processRetrievalNode(executeParams, nodeRunInfo, result);
        }

    }

    private String extractAnswer(JiuwenEventData eventData) {
        String text;
        if (eventData.getAnswer() instanceof String) {
            text = eventData.getAnswer().toString();
        } else {
            // 使用 WriteMapNullValue 保留 null 值字段
            text = JSON.toJSONString(eventData.getAnswer(), JSONWriter.Feature.WriteMapNullValue);
        }
        return text;
    }

    String extractOriginAnswer(JiuwenEventData eventData) {
        String text;
        if (eventData.getOriginAnswer() instanceof String) {
            text = eventData.getOriginAnswer().toString();
        } else {
            // 使用 WriteMapNullValue 保留 null 值字段
            text = JSON.toJSONString(eventData.getOriginAnswer(), JSONWriter.Feature.WriteMapNullValue);
        }
        return text;
    }

    private String convertNodeType(String jiuwenType) {
        return NodeType.fromJiuwen(jiuwenType).getEiType();
    }

    private String getEventNodeName(WorkflowIrWrapper ir, JiuwenEventData eventData) {
        return StringUtils.isEmpty(eventData.getNodeName()) ? ir.getIdNameMap()
            .getOrDefault(eventData.getNodeId(), eventData.getNodeId()) : eventData.getNodeName();
    }

    public void saveInstance(WorkflowRunInfo workflowRunInfo, WorkflowInstanceEntity instance,
                              ExecuteParams executeParams, boolean taskEnd) {
        if (!taskEnd) {
            conversationManagementService.saveTaskId(executeParams.getWorkflowId(), executeParams.getConversationId(),
                    MDC.get(TASK_ID));
        }

        if (workflowRunInfo.getEndTime() == null) {
            workflowRunInfo.setEndTime(System.currentTimeMillis());
        }

        if (workflowRunInfo.getStatus() == null || workflowRunInfo.getStatus()
                .equals(WorkflowRunStatus.RUNNING.getStatus())) {
            workflowRunInfo.setStatus(WorkflowRunStatus.SUCCEEDED.getStatus());
            instance.setStatus(WorkflowRunStatus.SUCCEEDED.getStatus().getDesc());
        }
        instance.setEndTime(workflowRunInfo.getEndTime());
        if (StringUtils.isEmpty(instance.getId())) {
            instance.setId(MDC.get(TASK_ID));
        }
        instanceService.save(instance, executeParams);
    }
}

