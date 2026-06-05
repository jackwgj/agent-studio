/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static java.time.temporal.ChronoUnit.DAYS;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.dto.agent.ConversationInfo;
import com.openjiuwen.studio.agent.runtime.dto.ConversationInfoList;
import com.openjiuwen.studio.agent.common.dto.agent.ExecutionInfo;
import com.openjiuwen.studio.agent.runtime.dto.ExecutionInfoList;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;
import com.openjiuwen.studio.agent.runtime.dto.JiuwenEventData;
import com.openjiuwen.studio.agent.runtime.entity.WorkflowInstanceEntity;
import com.openjiuwen.studio.agent.runtime.enums.JiuwenEventType;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrReq;
import com.openjiuwen.studio.agent.runtime.utils.CommonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 工作流运行实例服务
 *
 */
@Slf4j
@Service
public class WorkflowInstanceService {
    @Autowired
    private RedisClient redisClient;

    @Value("${workflow.insight-exec-rel:}")
    private String insightExecRel;  // 一个conversation下的execution记录

    @Value("${workflow.insight-conv-rel:}")
    private String insightConvRel; // 一个workflow下的 conversation记录

    @Value("${workflow.insight-exec:}")
    private String insightExec;

    @Value("${workflow.instance-expire:}")
    private Long instExpire;

    @Value("${workflow.conversation-expire:}")
    private Long convExpire;

    @Value("${workflow.max-execution-size:}")
    private Integer maxExecutionSize;

    @Value("${workflow.max-conversation-size:}")
    private Integer maxConversationSize;

    @Autowired
    private JiuWenClient jiuWenClient;

    /**
     * 获取工作流运行实例
     *
     * @param workflowInstanceId 工作流运行实例id
     * @return 返回工作流运行实例
     */
    public WorkflowInstanceEntity get(String workflowInstanceId, String versionId) {
        String insightExecKey = getInsightExec(RequestContextUtils.getRequestUserId(), workflowInstanceId, versionId);

        String instStr = redisClient.get(insightExecKey);
        if (StringUtils.isEmpty(instStr)) {
            return null;
        } else {
            try {
                return JSONObject.parseObject(instStr, WorkflowInstanceEntity.class);
            } catch (Exception e) {
                log.error("parse workflow instance error", e);
            }
        }
        return null;
    }

    /**
     * 获取工作流运行实例缓存
     *
     * @param workflowInstanceId 工作流运行实例id
     * @param versionId 发布版本号
     * @return 返回工作流运行实例
     */
    public WorkflowInstanceEntity getCache(String workflowInstanceId, String versionId) {
        return get(workflowInstanceId, versionId);
    }

    /**
     * 保存工作流运行实例
     *
     * @param workflowInstanceEntity 工作流运行实例
     */
    public void save(WorkflowInstanceEntity workflowInstanceEntity, ExecuteParams executeParams) {
        if (StringUtils.isEmpty(workflowInstanceEntity.getId())) {
            // id为空，不保存
            log.warn("workflow instance id is null!");
            return;
        }
        String versionId = executeParams.getReleasedVersion();

        if (executeParams.isDebug()) {
            saveInsightMessage(workflowInstanceEntity, versionId,
                executeParams.getAsyncTaskParamHolder() != null && executeParams.getAsyncTaskParamHolder().isAsync());
        } else if (executeParams.isTrace()) {
            // 非debug模式只保留需上报ops的事件
            List<JiuwenEvent> eventList = workflowInstanceEntity.getEventList()
                .stream()
                .filter(this::getFinishWorkflowNode)
                .collect(Collectors.toList());
            workflowInstanceEntity.setEventList(eventList);
            saveExecution(workflowInstanceEntity, versionId, Duration.ofMinutes(10));
        }
    }

    private boolean getFinishWorkflowNode(JiuwenEvent jiuwenEvent) {
        if (!Strings.CI.equals(jiuwenEvent.getEvent(),
            JiuwenEventType.WORKFLOW_NODE_MESSAGE.name().toLowerCase(Locale.ROOT))) {
            return false;
        }
        JiuwenEventData jiuwenEventData = jiuwenEvent.getData();
        return Strings.CI.equals("finish", jiuwenEventData.getStatus().toString());
    }

    /**
     * 开发存储insight事件接口，异步模式下实时存储
     *
     * @param workflowInstanceEntity 工作流运行实例
     * @param versionId 发布版本号
     */
    public void saveInsightMessage(WorkflowInstanceEntity workflowInstanceEntity, String versionId, boolean isAsync) {
        // error 日志信息屏蔽jiuwen
        if (StringUtils.isNotBlank(workflowInstanceEntity.getErrorInfo())) {
            workflowInstanceEntity.setErrorInfo(workflowInstanceEntity.getErrorInfo().replaceAll("(?i)(九问|jiuwen)", "Runtime"));
        }
        saveExecution(workflowInstanceEntity, versionId);
        saveExecutionInfos(workflowInstanceEntity, versionId);
        if (!isAsync) {
            saveConversationInfos(workflowInstanceEntity, versionId);
        }
    }

    private void saveExecution(@NotNull WorkflowInstanceEntity workflowInstanceEntity, String versionId) {
        saveExecution(workflowInstanceEntity, versionId, Duration.of(instExpire, DAYS));
    }

    private void saveExecution(@NotNull WorkflowInstanceEntity workflowInstanceEntity, String versionId,
        Duration ttlDuration) {
        String insightExecKey = getInsightExec(RequestContextUtils.getRequestUserId(), workflowInstanceEntity.getId(),
            versionId);
        redisClient.set(insightExecKey, JSONObject.toJSONString(workflowInstanceEntity), ttlDuration);
    }

    public void deleteExecution(String userId, WorkflowInstanceEntity workflowInstanceEntity, String versionId) {
        String insightExecKey = getInsightExec(userId, workflowInstanceEntity.getId(), versionId);

        redisClient.delete(insightExecKey);
    }

    /**
     * 将source对象数据clone到target
     *
     * @param source source对象
     * @param target target对象
     */
    public void copy(WorkflowInstanceEntity source, WorkflowInstanceEntity target) {
        target.setId(source.getId());
        target.setExternalId(source.getExternalId());
        target.setConversationId(source.getConversationId());
        target.setWorkflowId(source.getWorkflowId());
        target.setProjectId(source.getProjectId());
        target.setInputs(source.getInputs());
        target.setOutputs(source.getOutputs());
        target.setStatus(source.getStatus());
        target.setRunInfo(source.getRunInfo());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setErrorInfo(source.getErrorInfo());
        if (source.getEventList() == null) {
            target.setEventList(target.getEventList() == null ? new ArrayList<>() : target.getEventList());
            return;
        }
        if (target.getEventList() != null) {
            // 避免覆盖eventList
            target.getEventList().addAll(0, source.getEventList());
        } else {
            target.setEventList(source.getEventList());
        }
    }

    /**
     * 获取工作流对话记录
     *
     * @param workflowId workflowId
     * @param versionId 发布版本号
     * @return 执行记录
     */
    public ConversationInfoList getConversationInfos(String workflowId, String versionId) {
        try {
            String userId = RequestContextUtils.getRequestUserId();
            String conversationInfoKey = getInsightConvRel(workflowId, userId, versionId);

            ConversationInfoList conversationInfos = new ConversationInfoList();
            if (redisClient.exists(conversationInfoKey)) {
                String text = redisClient.get(conversationInfoKey);
                if (StringUtils.isNotEmpty(text)) {
                    conversationInfos = JSON.parseObject(text, ConversationInfoList.class);
                }
            }
            log.info("retrieve: workflowId={}, userId={}, conversationInfoSize={}", workflowId, userId,
                conversationInfos.size());
            return conversationInfos;
        } catch (Exception e) {
            log.error("redisson get bucket failed", e);
            throw new AgentStudioException(StudioError.REDISSON_GET_BUCKET_FAILED);
        }
    }

    /**
     * 获取工作流会话执行记录
     *
     * @param workflowId workflowId
     * @param versionId 发布版本号
     * @return 执行记录
     */
    public ExecutionInfoList getExecutionInfos(String workflowId, String conversationId, String versionId) {
        try {
            String userId = RequestContextUtils.getRequestUserId();

            String executionInfoKey = getInsightExecRel(workflowId, RequestContextUtils.getRequestUserId(),
                conversationId, versionId);

            ExecutionInfoList executionInfos = new ExecutionInfoList();
            if (redisClient.exists(executionInfoKey)) {
                String text = redisClient.get(executionInfoKey);
                if (StringUtils.isNotEmpty(text)) {
                    executionInfos = JSON.parseObject(text, ExecutionInfoList.class);
                }
            }
            log.info("retrieve: workflowId={}, userId={}, executionInfoSize={}", workflowId, userId,
                executionInfos.size());
            return executionInfos;
        } catch (Exception e) {
            log.error("redisson get bucket failed", e);
            throw new AgentStudioException(StudioError.REDISSON_GET_BUCKET_FAILED);
        }
    }

    /**
     * 从实例中提取工作流会话记录并保存
     *
     * @param workflowInstanceEntity workflowInstanceEntity
     * @param versionId 发布版本号
     */
    private void saveExecutionInfos(WorkflowInstanceEntity workflowInstanceEntity, String versionId) {
        String executionInfoKey = getInsightExecRel(workflowInstanceEntity.getWorkflowId(),
            RequestContextUtils.getRequestUserId(), workflowInstanceEntity.getConversationId(), versionId);
        ExecutionInfoList originExecutionInfos = new ExecutionInfoList();
        if (redisClient.exists(executionInfoKey)) {
            String text = redisClient.get(executionInfoKey);
            if (StringUtils.isNotEmpty(text)) {
                originExecutionInfos = JSON.parseObject(text, ExecutionInfoList.class);
            }
        }
        // 读取历史信息，过滤当前executionInfos
        List<ExecutionInfo> executionInfos = new ExecutionInfoList();
        for (ExecutionInfo executionInfo : originExecutionInfos) {
            if (!Objects.equals(executionInfo.getExecutionId(), workflowInstanceEntity.getId())) {
                executionInfos.add(executionInfo);
            }
        }
        // 更新最新executionInfos，根据size截断
        executionInfos.add(convertExecutionInfo(workflowInstanceEntity));
        int offset = executionInfos.size() - maxExecutionSize;
        if (offset > 0) {
            clearExecutionRecordsAsync(RequestContextUtils.getRequestUserId(), versionId, executionInfos.subList(0, offset));
        }
        executionInfos =
            CommonUtils.subList(offset, maxExecutionSize, executionInfos);
        // 设置info最后更新时间
        redisClient.set(executionInfoKey, JSON.toJSONString(executionInfos), Duration.of(convExpire, DAYS));
        log.info("update: projectId={}, conversationId={}, executionInfoSize={}", workflowInstanceEntity.getProjectId(),
            workflowInstanceEntity.getConversationId(), executionInfos.size());
    }

    private String getInsightExecRel(String workflowId, String userId, String conversationId, String versionId) {
        String executionInfoKey = String.format(insightExecRel, workflowId, userId, conversationId);
        if (!StringUtils.isEmpty(versionId)) {
            return executionInfoKey + "_" + versionId;
        }
        return executionInfoKey;
    }

    private String getInsightConvRel(String workflowId, String userId, String versionId) {
        String conversationInfoKey = String.format(insightConvRel, workflowId, userId);
        if (!StringUtils.isEmpty(versionId)) {
            log.debug("save conInfo versionId is {}", versionId);
            return conversationInfoKey + "_" + versionId;
        }
        return conversationInfoKey;
    }

    private String getInsightExec(String userId, String executionId, String versionId) {
        String insightExecKey = String.format(insightExec, userId, executionId);
        if (!StringUtils.isEmpty(versionId)) {
            return insightExecKey + "_" + versionId;
        }
        return insightExecKey;
    }

    public CompletableFuture<Void> clearWorkflowConversationRecords(String workflowId, String userId, String versionId,
        List<ConversationInfo> needDeleteInfos, String token) {
        return CompletableFuture.runAsync(() -> {
            log.info("start to clear workflow conversation records. {}", workflowId);
            for (ConversationInfo info : needDeleteInfos) {
                try {
                    log.info("Start to clear workflow record. workflow:{} userId:{} version:{} conversation:{}",
                        workflowId, userId, versionId, info.getConversationId());

                    String executionInfoKey = getInsightExecRel(workflowId, userId, info.getConversationId(),
                        versionId);
                    String text = redisClient.get(executionInfoKey);
                    if (StringUtils.isNotEmpty(text)) {
                        List<ExecutionInfo> executionInfos = JsonUtils.decode(text,
                            new TypeReference<List<ExecutionInfo>>() { });
                        clearExecutionRecords(userId, versionId, executionInfos);
                        redisClient.delete(executionInfoKey);
                    }

                    clearJiuwenConversaion(token, info.getConversationId());
                } catch (Exception e) {
                    log.warn(
                        "Fail clear workflow execution record. workflow: {} userId:{}, version:{} conversation:{} {}",
                        workflowId, userId, versionId, info.getConversationId(), e.getMessage());
                }
            }
            log.info("Success clear workflow conversation records. {}", workflowId);
        });
    }

    private void clearExecutionRecords(String userId, String versionId, List<ExecutionInfo> needDeleteInfos) {
        if (needDeleteInfos == null || needDeleteInfos.isEmpty()) {
            return;
        }
        for (ExecutionInfo info : needDeleteInfos) {
            log.info("Start to clear workflow execution record. userId:{} version:{} executionId:{}", userId, versionId,
                info.getExecutionId());
            String executionInfoKey = getInsightExec(userId, info.getExecutionId(), versionId);
            redisClient.delete(executionInfoKey);
        }
    }

    CompletableFuture<Void> clearExecutionRecordsAsync(String userId, String versionId, List<ExecutionInfo> needDeleteInfos) {
        return CompletableFuture.runAsync(() -> clearExecutionRecords(userId, versionId, needDeleteInfos));
    }

    private void clearJiuwenConversaion(String token, String conversationId) {
        try {
            JiuWenDeleteIrReq jiuWenDeleteIrReq = JiuWenDeleteIrReq.builder().conversationId(conversationId).build();

            jiuWenClient.deleteIr(token, jiuWenDeleteIrReq);
        } catch (Exception e) {
            log.warn("Fail clear jiuwen conversation record. conversation:{}", conversationId, e);
        }
    }

    /**
     * 从实例中提取工作流会话记录并保存
     *
     * @param workflowInstanceEntity workflowInstanceEntity
     * @param versionId 发布版本号
     */
    private void saveConversationInfos(WorkflowInstanceEntity workflowInstanceEntity, String versionId) {
        RedisLock lock = null;
        String conversationInfoKey = getInsightConvRel(workflowInstanceEntity.getWorkflowId(),
            RequestContextUtils.getRequestUserId(), versionId);

        try {
            lock = redisClient.getLock(conversationInfoKey + "_lock");
            // 获取分布式锁，如果锁不可用，则等待3秒，如果还是无法获取，则抛出异常
            if (lock.tryLock(Duration.ofSeconds(3))) {
                ConversationInfoList originConversationInfos = new ConversationInfoList();
                if (redisClient.exists(conversationInfoKey)) {
                    String text = redisClient.get(conversationInfoKey);
                    if (StringUtils.isNotEmpty(text)) {
                        originConversationInfos = JSON.parseObject(text, ConversationInfoList.class);
                    }
                }
                // 读取历史信息，过滤当前conversationInfo
                List<ConversationInfo> conversationInfos = new ConversationInfoList();
                ConversationInfo currentConversationInfo = null;
                for (ConversationInfo conversationInfo : originConversationInfos) {
                    if (!Objects.equals(conversationInfo.getConversationId(),
                        workflowInstanceEntity.getConversationId())) {
                        conversationInfos.add(conversationInfo);
                    } else {
                        currentConversationInfo = conversationInfo;
                    }
                }
                // 如conversation重新执行，更新conversationInfo
                if (currentConversationInfo != null) {
                    currentConversationInfo.setEndTime(workflowInstanceEntity.getEndTime());
                    currentConversationInfo.setOutputs(workflowInstanceEntity.getOutputs());
                    currentConversationInfo.setStatus(workflowInstanceEntity.getStatus());
                } else {
                    currentConversationInfo = convertConversationInfo(workflowInstanceEntity);
                }
                // 更新最新conversationInfos，根据size截断
                conversationInfos.add(currentConversationInfo);

                int offset = conversationInfos.size() - maxConversationSize;
                if (offset > 0) {
                    List<ConversationInfo> needDeleteInfos = conversationInfos.subList(0, offset);
                    clearWorkflowConversationRecords(workflowInstanceEntity.getWorkflowId(),
                        RequestContextUtils.getRequestUserId(), versionId, needDeleteInfos,
                        RequestContextUtils.getRequestAuthToken());
                }

                conversationInfos = CommonUtils.subList(offset, maxConversationSize, conversationInfos);
                // 设置info最后更新时间
                redisClient.set(conversationInfoKey, JSON.toJSONString(conversationInfos),
                    Duration.of(convExpire, DAYS));
                log.info("update: projectId={}, conversationId={}, workflowId={}, userId={}, conversationInfoSize={}",
                    workflowInstanceEntity.getProjectId(), workflowInstanceEntity.getConversationId(),
                    workflowInstanceEntity.getWorkflowId(), RequestContextUtils.getRequestUserId(),
                    conversationInfos.size());
            } else {
                log.error("failed to get lock, other user is processing same workflow!");
                throw new AgentStudioException(StudioError.REDISSON_GET_BUCKET_FAILED);
            }
        } catch (Exception e) {
            log.error("redisson get bucket failed", e);
            throw new AgentStudioException(StudioError.REDISSON_GET_BUCKET_FAILED);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    /**
     * 转换工作流实例为一条执行记录（不包含具体执行信息，仅供列表接口调用）
     *
     * @param workflowInstance workflowInstance
     * @return 执行记录
     */
    public ExecutionInfo convertExecutionInfo(WorkflowInstanceEntity workflowInstance) {
        ExecutionInfo execution = new ExecutionInfo();
        execution.setProjectId(workflowInstance.getProjectId());
        execution.setWorkflowId(workflowInstance.getWorkflowId());
        execution.setConversationId(workflowInstance.getConversationId());
        execution.setExecutionId(workflowInstance.getId());
        execution.setInputs(workflowInstance.getInputs());
        execution.setOutputs(workflowInstance.getOutputs());
        execution.setStatus(workflowInstance.getStatus());
        execution.setErrorInfo(workflowInstance.getErrorInfo());
        execution.setRunInfo(workflowInstance.getRunInfo());
        execution.setStartTime(workflowInstance.getStartTime());
        execution.setEndTime(workflowInstance.getEndTime());
        return execution;
    }

    /**
     * 转换工作流实例为一条conversation记录
     *
     * @param workflowInstance workflowInstance
     * @return 执行记录
     */
    public ConversationInfo convertConversationInfo(WorkflowInstanceEntity workflowInstance) {
        ConversationInfo conversationInfo = new ConversationInfo();
        conversationInfo.setProjectId(workflowInstance.getProjectId());
        conversationInfo.setWorkflowId(workflowInstance.getWorkflowId());
        conversationInfo.setConversationId(workflowInstance.getConversationId());
        conversationInfo.setInputs(workflowInstance.getInputs());
        conversationInfo.setOutputs(workflowInstance.getOutputs());
        conversationInfo.setStatus(workflowInstance.getStatus());
        conversationInfo.setStartTime(workflowInstance.getStartTime());
        conversationInfo.setEndTime(workflowInstance.getEndTime());
        return conversationInfo;
    }
}
