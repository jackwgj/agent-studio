/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.AgentEvent;
import com.openjiuwen.studio.agent.runtime.dto.ApiExecDataEventAnswer;
import com.openjiuwen.studio.agent.runtime.dto.KnowledgeBaseFileInfo;
import com.openjiuwen.studio.agent.runtime.dto.KnowledgeFileRef;
import com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfo;
import com.openjiuwen.studio.agent.runtime.dto.PluginContent;
import com.openjiuwen.studio.agent.runtime.dto.PluginResult;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunStreamRsp;
import com.openjiuwen.studio.agent.runtime.enums.EventType;
import com.openjiuwen.studio.agent.runtime.enums.WorkflowStreamEventEnum;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.WorkflowRunResult;
import com.openjiuwen.studio.agent.runtime.utils.SseEmitterUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeBaseFileInfoService {

    public void processRetrievalNode(ExecuteParams executeParams, NodeRunInfo nodeRunInfo, WorkflowRunResult result) {
        // 检索结果只在节点状态为finished时返回
        if (!Objects.equals(nodeRunInfo.getNodeStatus().toString(), NodeRunInfo.NodeStatusEnum.FINISHED.toString())) {
            return;
        }
        Set<KnowledgeBaseFileInfo> fileInfos = parseRetrievalNode(nodeRunInfo);
        if (CollectionUtils.isEmpty(fileInfos)) {
            return;
        }
        if (executeParams.isStream()) {
            WorkflowRunStreamRsp workflowRunStreamRsp = new WorkflowRunStreamRsp();
            workflowRunStreamRsp.setEvent(
                WorkflowStreamEventEnum.KNOWLEDGE_BASE_FILES_INFO.name().toLowerCase(Locale.ROOT));
            workflowRunStreamRsp.setData(fileInfos);
            sendEvent(executeParams.getWorkflowId(), executeParams.getSseEmitter(), workflowRunStreamRsp);
        } else {
            // 非流式模式下，将检索结果设置到result中
            List<KnowledgeBaseFileInfo> knowledgeBaseFileInfoList = result.getKnowledgeBaseFileInfoList();
            if (!CollectionUtils.isEmpty(knowledgeBaseFileInfoList)) {
                knowledgeBaseFileInfoList.addAll(fileInfos);
                result.setKnowledgeBaseFileInfoList(new ArrayList<>(knowledgeBaseFileInfoList));
                return;
            }
            result.setKnowledgeBaseFileInfoList(new ArrayList<>(fileInfos));
        }
    }

    public void processRetrievalNode(NodeRunInfo nodeRunInfo, WorkflowRunResult result, SseEmitter sseEmitter) {
        if (!Objects.equals(nodeRunInfo.getNodeStatus().toString(), NodeRunInfo.NodeStatusEnum.FINISHED.toString())) {
            return;
        }
        Set<KnowledgeBaseFileInfo> fileInfos = parseRetrievalNode(nodeRunInfo);
        if (CollectionUtils.isEmpty(fileInfos)) {
            return;
        }
        if (sseEmitter != null) {
            AgentEvent agentEvent = new AgentEvent().setEvent(EventType.KNOWLEDGE_BASE_FILE.toString())
                .setContent(fileInfos)
                .setCreatedTime(System.currentTimeMillis());
            sendSseData(sseEmitter, agentEvent);
        } else {
            List<KnowledgeBaseFileInfo> knowledgeBaseFileInfoList = result.getKnowledgeBaseFileInfoList();
            if (!CollectionUtils.isEmpty(knowledgeBaseFileInfoList)) {
                knowledgeBaseFileInfoList.addAll(fileInfos);
                result.setKnowledgeBaseFileInfoList(knowledgeBaseFileInfoList);
            }
            result.setKnowledgeBaseFileInfoList(new ArrayList<>(fileInfos));
        }
    }

    public void processRetrievalPlugin(ApiExecDataEventAnswer pluginRsp, SseEmitter sseEmitter) {
        if (!pluginRsp.getName().equals(Constant.KnowledgeRetrievalNode.RETRIEVAL)) {
            return;
        }
        if (sseEmitter != null) {
            sendSseData(sseEmitter, prepareAgentEventByPlugin(pluginRsp));
        }
    }

    private AgentEvent prepareAgentEventByPlugin(ApiExecDataEventAnswer pluginRsp) {
        List<KnowledgeBaseFileInfo> knowledgeBaseFileInfos = getKnowledgeBaseFileInfosFromPlugin(pluginRsp);
        return new AgentEvent().setEvent(EventType.KNOWLEDGE_BASE_FILE.toString())
            .setContent(knowledgeBaseFileInfos)
            .setCreatedTime(System.currentTimeMillis());
    }

    public List<KnowledgeBaseFileInfo> getKnowledgeBaseFileInfosFromPlugin(ApiExecDataEventAnswer pluginRsp) {
        if (pluginRsp == null || pluginRsp.getContent() == null) {
            return Collections.emptyList();
        }
        Object object = pluginRsp.getContent();
        com.alibaba.fastjson2.JSONObject jsonObject = (com.alibaba.fastjson2.JSONObject) object;
        PluginContent content = jsonObject.toJavaObject(PluginContent.class);
        if (content == null || content.getResult() == null) {
            return Collections.emptyList();
        }
        List<KnowledgeFileRef> fileRefs = content.getResult().getOutputList();
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(fileRefs)) {
            return Collections.emptyList();
        }
        Set<KnowledgeFileRef> knowledgeFileRefSet = new HashSet<>(fileRefs);
        return knowledgeFileRefSet.stream()
            .filter(item -> !StringUtils.isEmpty(item.getFileId()))
            .map(item -> KnowledgeBaseFileInfo.builder()
                .knowledgeBaseId(item.getKnowledgeBaseId())
                .fileId(item.getFileId())
                .fileName(item.getDocumentName())
                .knowledgeBaseType(item.getKnowledgeBaseType())
                .type(item.getType())
                .build())
            .toList();
    }

    public void sendEvent(String workflowId, SseEmitter sseEmitter, WorkflowRunStreamRsp eventObj) {
        try {
            SseEmitterUtils.sendEvent(sseEmitter, eventObj);
        } catch (IOException e) {
            String errorInfo = String.format("when workflow: %s run, stream call execute error.", workflowId);
            log.error(errorInfo, e);
            throw new AgentStudioException(StudioError.CLIENT_ABORT_REQUEST);
        }
    }

    private void sendSseData(SseEmitter sseEmitter, AgentEvent sseData) {
        try {
            if (sseData.getCreatedTime() == null) {
                sseData.setCreatedTime(System.currentTimeMillis());
            }
            sseEmitter.send(sseData, MediaType.APPLICATION_JSON);
        } catch (IllegalStateException | IOException exception) {
            log.error("Fail to send sse, data [{}], reason [{}]", sseData, exception.getMessage());
            throw new AgentStudioException(StudioError.AGENT_INFO_SEND_FAILED, exception.getMessage());
        }
    }

    private Set<KnowledgeBaseFileInfo> parseRetrievalNode(NodeRunInfo nodeRunInfo) {
        Map<String, Object> outputs = nodeRunInfo.getOutputs();
        if (!Constant.KnowledgeRetrievalNode.PLUGIN.equals(nodeRunInfo.getNodeType()) || outputs == null
            || outputs.isEmpty()) {
            return null;
        }
        PluginResult pluginResult;
        try {
            pluginResult = JSON.parseObject(JSON.toJSONString(outputs), PluginResult.class);
        } catch (RuntimeException ex) {
            return Collections.emptySet();
        }
        if (pluginResult == null || CollectionUtils.isEmpty(pluginResult.getOutputList())) {
            return Collections.emptySet();
        }
        return pluginResult.getOutputList()
            .stream()
            .filter(item -> !StringUtils.isEmpty(item.getFileId()))
            .map(item -> KnowledgeBaseFileInfo.builder()
                .knowledgeBaseId(item.getKnowledgeBaseId())
                .fileId(item.getFileId())
                .type(item.getType())
                .fileName(item.getDocumentName())
                .knowledgeBaseType(item.getKnowledgeBaseType())
                .nodeName(nodeRunInfo.getNodeName())
                .build())
            .collect(Collectors.toSet());
    }
}
