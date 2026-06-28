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
import com.openjiuwen.studio.agent.runtime.dto.RagResultReference;
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

        convertNodeOutputToStandardFormat(nodeRunInfo);

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

    @SuppressWarnings("unchecked")
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

    /**
     * 将 FlowKnowledgeRetrieval 输出格式 {results: [...], context: "..."}
     * 转换为标准 RetrievalKnowledgeBasesResponseBody 格式 {outputList: [...RagResultReference...]}
     */
    @SuppressWarnings("unchecked")
    private void convertNodeOutputToStandardFormat(NodeRunInfo nodeRunInfo) {
        Map<String, Object> outputs = nodeRunInfo.getOutputs();
        if (outputs == null || outputs.isEmpty()) {
            return;
        }
        Object resultsObj = outputs.get("results");
        if (!(resultsObj instanceof List)) {
            return;
        }
        List<Map<String, Object>> results = (List<Map<String, Object>>) resultsObj;
        // 已经转换过了，避免重复
        if (outputs.containsKey("outputList")) {
            return;
        }
        List<RagResultReference> ragResultList = results.stream()
            .map(item -> {
                RagResultReference ref = new RagResultReference();
                ref.setText(item.get("text") != null ? item.get("text").toString() : null);
                ref.setContent(item.get("text") != null ? item.get("text").toString() : null);
                if (item.get("score") instanceof Number) {
                    ref.setScore(((Number) item.get("score")).floatValue());
                }
                ref.setSource(item.get("source") != null ? item.get("source").toString() : null);
                ref.setKnowledgeBaseId(item.get("knowledgeBaseId") != null ? item.get("knowledgeBaseId").toString() : null);
                ref.setKnowledgeBaseType(item.get("knowledgeBaseType") != null ? item.get("knowledgeBaseType").toString() : null);
                ref.setFileId(item.get("fileId") != null ? item.get("fileId").toString() : null);
                ref.setDocumentName(item.get("documentName") != null ? item.get("documentName").toString() : null);
                ref.setSubtitle(item.get("subtitle") != null ? item.get("subtitle").toString() : null);
                if (item.get("serialNumber") instanceof Number) {
                    ref.setSerialNumber(((Number) item.get("serialNumber")).longValue());
                }
                ref.setRetrievalId(item.get("retrievalId") != null ? item.get("retrievalId").toString() : null);
                if (item.get("type") != null) {
                    ref.setType(RagResultReference.TypeEnum.fromValue(item.get("type").toString()));
                }
                if (item.get("metadata") instanceof Map) {
                    ref.setMetadata((Map<String, Object>) item.get("metadata"));
                }
                return ref;
            })
            .collect(Collectors.toList());
        outputs.remove("results");
        outputs.remove("context");
        outputs.put("outputList", ragResultList);
        nodeRunInfo.setOutputs(outputs);
    }
}
