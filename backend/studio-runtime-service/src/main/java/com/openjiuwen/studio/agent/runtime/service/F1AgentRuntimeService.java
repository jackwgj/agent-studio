/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson2.JSONArray;
import com.openjiuwen.studio.agent.common.dto.agent.ListWorkflowsQo;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CommonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.runtime.dto.AgentEvent;
import com.openjiuwen.studio.agent.runtime.dto.ErrorEvent;
import com.openjiuwen.studio.agent.runtime.dto.F1AgentEvent;
import com.openjiuwen.studio.agent.runtime.dto.ImportRsp;
import com.openjiuwen.studio.agent.runtime.dto.Latency;
import com.openjiuwen.studio.agent.common.dto.run.WorkflowListItem;
import com.openjiuwen.studio.agent.common.dto.run.WorkflowListRsp;
import com.openjiuwen.studio.agent.runtime.enums.EventType;
import com.openjiuwen.studio.agent.runtime.enums.F1AgentNoMatchContent;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.F1AgentTemplate;
import com.openjiuwen.studio.agent.runtime.rce.client.AgentManagerClient;
import com.openjiuwen.studio.agent.runtime.utils.SseEmitterUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Agent运行Service
 *
 */
@Slf4j
@Service
public class F1AgentRuntimeService {
    private static final int THREAD_POOL_SIZE = 100;

    private static final String AGENT_EXECUTE_FAILED = "Agent运行失败";

    private final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @Autowired
    private AgentManagerClient agentManagerClient;

    @Value("${agent.sse-timeout-milliseconds}")
    private long agentSseTimeoutMilliSec;

    @Value("${op.svc.project-id}")
    private String opSvcProjectId;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RuntimeI18nService runtimeI18nService;

    @Autowired
    private JiuwenEventProcessor eventProcessor;

    private static final String CUSTOM = "custom";

    private static final String INNER = "inner";

    public F1AgentTemplate executeQuery(String query, List<F1AgentTemplate> f1AgentTemplates) {
        List<String> types = f1AgentTemplates.stream().map(x -> x.getType()).collect(Collectors.toList());
        for (String templateType : types) {
            if (query.contains(templateType)) {
                List<String> matchNames = f1AgentTemplates.stream().filter(x -> x.getType().equals(templateType))
                        .map(x -> x.getMatchName()).collect(Collectors.toList());
                for (String name : matchNames) {
                    if (query.contains(name)) {
                        F1AgentTemplate template = f1AgentTemplates.stream().filter(x -> x.getType().equals(templateType))
                                .filter(x -> x.getMatchName().equals(name)).findFirst().get();
                        return template;
                    }
                }
                return F1AgentTemplate.builder().type(templateType).build();
            }
        }
        return null;
    }

    public SseEmitter runF1AgentStream(AgentExecuteParams executeParams) {
        SseEmitter sseEmitter = buildSseEmitter(executeParams);
        String language = RequestHeaderHolderUtils.getRequestLanguage();
        try {
            // 1、初始化 应用|助手|插件|工作流 对应的模板
            String templateString = CommonUtils.getResourceContent("template/template.json");
            List<F1AgentTemplate> f1AgentTemplates = JSONArray.parseArray(templateString, F1AgentTemplate.class);

            // 2、匹配应用/助手、插件、工作流
            String query = executeParams.getQuery();
            F1AgentTemplate template = executeQuery(query, f1AgentTemplates);
            if (ObjectUtils.isEmpty(template)) {
                sendDone(sseEmitter);
                return sseEmitter;
            }
            String type = template.getType();
            String matchName = template.getMatchName();
            if (StringUtils.isEmpty(matchName)) {
                long endTime = System.currentTimeMillis();
                sendSseData(sseEmitter,
                        new F1AgentEvent().setEvent(EventType.NO_MATCH.toString()).setCreatedTime(System.currentTimeMillis())
                                .setLatency(endTime - executeParams.getStartTime()).setConversationId(executeParams.getConversationId())
                                .setContent(F1AgentNoMatchContent.enumOf(type)));
                sendDone(sseEmitter);
                return sseEmitter;
            }

            // 3、匹配上创建createting事件并创建agent应用
            String token = RequestContextUtils.getRequestAuthToken();
            long endTime = System.currentTimeMillis();
            sendSseData(sseEmitter,
                    new F1AgentEvent().setEvent(EventType.CREATING.toString()).setCreatedTime(System.currentTimeMillis())
                            .setLatency(endTime - executeParams.getStartTime()).setConversationId(executeParams.getConversationId()));

            fixedThreadPool.submit(() -> {
                try {
                    RequestHeaderHolderUtils.setRequestLanguage(language);
                    importAgent(template, sseEmitter, executeParams, token);
                } finally {
                    RequestHeaderHolderUtils.clear();
                }
            });

        } catch (Exception exception) {
            log.error("Fail to run agent stream because [{}]", exception.getMessage());
            sendErrorEvent(sseEmitter, exception, executeParams);
        }
        return sseEmitter;
    }

    public void importAgent(F1AgentTemplate template, SseEmitter sseEmitter, AgentExecuteParams executeParams,
                             String token) {
        String projectId = executeParams.getProjectId();
        String importWorkflowsId = template.getImportWorkflowsId() == null ? "" : template.getImportWorkflowsId();
        String importToolsId = template.getImportToolsId();
        String templateNames = template.getTemplateName();
        String appId = template.getAppId();
        String templateContent = CommonUtils.getResourceContent("template/" + templateNames);
        // 不同用户导入时使用不同的uuid,同一用户导入时uuid相同,用户指userId
        String userId = executeParams.getUserId();
        String uuid = UUID.nameUUIDFromBytes((projectId + userId + appId).getBytes(StandardCharsets.UTF_8)).toString();
        templateContent = templateContent.replace(template.getAppId(), uuid);
        template.setAppId(uuid);
        try {
            Resource resources = getResource(templateContent, templateNames);
            ResponseEntity<ImportRsp> responseEntity = null;
            switch (template.getAppType()) {
                case "agents":
                    responseEntity = agentManagerClient.importAgents(token, projectId, resources, uuid, importWorkflowsId, importToolsId);
                    break;
                case "workflows":
                    // 工作流删除时是软删除，创建后删除再创建时工作流id会变化，创建后再查询，确保获取的APPID是正确的
                    // 先按名称查询, 更新ID
                    Resource newResource = updateResourceAndTemplate(template, templateContent, resources, token, projectId);
                    // 导入workflow
                    responseEntity = agentManagerClient.importWorkflows(token, projectId, newResource, template.getAppId(), importToolsId);
                    // 导入后按名称查询并更新ID
                    updateResourceAndTemplate(template, templateContent, resources, token, projectId);
                    break;
                case "tools":
                    if (Objects.equals(projectId, opSvcProjectId)) {
                        templateContent = templateContent.replace(CUSTOM, INNER);
                        resources = getResource(templateContent, templateNames);
                    }
                    responseEntity = agentManagerClient.importTools(token, projectId, resources, uuid);
                    break;
                default:
                    sendErrorEvent(sseEmitter, new AgentStudioException("appType not agents or workflows or tools"),
                        executeParams);
                    return;
            }
            if (ObjectUtils.isEmpty(responseEntity)) {
                sendErrorEvent(sseEmitter, new AgentStudioException("import agents|workflows|tools response is null"),
                    executeParams);
                return;
            }
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                log.info("{} import successful", templateNames);
                long endTime = System.currentTimeMillis();
                sendSseData(sseEmitter,
                        new F1AgentEvent().setEvent(EventType.CREATED.toString()).setCreatedTime(System.currentTimeMillis())
                                .setLatency(endTime - executeParams.getStartTime()).setConversationId(executeParams.getConversationId())
                                .setAppType(template.getAppType()).setAppId(template.getAppId()).setAppName(template.getAppName())
                                .setAppIcon(template.getAppIcon()).setAppDesc(template.getAppDesc()));
                sendDone(sseEmitter);
                return;
            }
            if (ObjectUtils.isEmpty(responseEntity.getBody())) {
                sendErrorEvent(sseEmitter, new AgentStudioException(
                    "import agents|workflows|tools failed, response body is null"), executeParams);
                return;
            }
            sendErrorEvent(sseEmitter, new AgentStudioException(responseEntity.getBody().toString()), executeParams);
        } catch (Exception e) {
            log.error("import Agent error", e);
            sendErrorEvent(sseEmitter, e, executeParams);
        }
    }

    public Resource updateResourceAndTemplate(F1AgentTemplate template, String templateContent, Resource resource, String token, String projectId) {
        String appName = template.getAppName();
        String templateName = template.getTemplateName();
        ListWorkflowsQo listWorkflowsQo = new ListWorkflowsQo();
        listWorkflowsQo.setName(appName);
        listWorkflowsQo.setDescription(template.getAppDesc());
        String regex = appName + "_\\d+";
        ResponseEntity<WorkflowListRsp> workflowListRspResponseEntity =
            agentManagerClient.listWorkflows(token, projectId, listWorkflowsQo);
        String finalUuid = template.getAppId();
        if (ObjectUtils.isNotEmpty(workflowListRspResponseEntity.getBody())) {
            List<WorkflowListItem> workflowList = workflowListRspResponseEntity.getBody().getWorkflowList().stream()
                .filter(
                    workflowListItem ->
                    workflowListItem.getWorkflowId().equals(finalUuid)).collect(Collectors.toList());
                    List<WorkflowListItem> workflowList1 = workflowListRspResponseEntity.getBody().getWorkflowList()
                        .stream().filter(
                            workflowListItem ->
                    workflowListItem.getName().equals(appName) || workflowListItem.getName().matches(regex)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(workflowList) && CollectionUtils.isNotEmpty(workflowList1)) {
                String uuid = workflowList1.get(0).getWorkflowId();
                String content = templateContent.replace(template.getAppId(), uuid);
                Resource newResource = getResource(content, templateName);
                template.setAppId(uuid);
                return newResource;
            }
        }
        return resource;
    }

    public Resource getResource(String content, String name) {
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            MockMultipartFile file = new MockMultipartFile(name, name, "text/plain", inputStream);

            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            return resource;
        } catch (IOException e) {
            log.error("import agents|workflows|tools error when get resource: ", e);
            throw new AgentStudioException(StudioError.IMPORT_AGENT_WORKFLOW_TOOL_ERROR);
        }
    }

    private SseEmitter buildSseEmitter(AgentExecuteParams executeParams) {
        SseEmitter sseEmitter = new SseEmitter(agentSseTimeoutMilliSec);

        // SSE响应超时的处理
        sseEmitter.onTimeout(
                () -> log.error("SSE connection timed out, conversationId [{}]", executeParams.getConversationId()));

        // SSE结束后的处理
        sseEmitter.onCompletion(() -> {
        });
        return sseEmitter;
    }

    private void sendSseData(SseEmitter sseEmitter, Object sseData) {
        try {
            sseEmitter.send(sseData, MediaType.APPLICATION_JSON);
        } catch (IllegalStateException | IOException exception) {
            log.error("Fail to send sse, data [{}], reason [{}]", sseData, exception.getMessage());
            throw new AgentStudioException(StudioError.AGENT_INFO_SEND_FAILED, exception.getMessage());
        }
    }

    // 发送error消息块
    public void sendErrorEvent(SseEmitter emitter, Throwable throwable, AgentExecuteParams executeParams) {
        // 获取请求语言信息
        ErrorEvent errorEvent = runtimeI18nService.createErrorEvent(throwable, null);
        String errMsg = errorEvent.getErrorMsg();
        try {
            eventProcessor.sendEvent(executeParams.getAgentId(), emitter,
                    WorkflowStreamEventFactory.error(errorEvent));
            // 如果没有发送过statistic_data消息，需要再主动发送一次，用于统计耗时
            if (!executeParams.isDone()) {
                AgentEvent agentEvent = new AgentEvent().setEvent(EventType.STATIC_DATA.toString())
                        .setCreatedTime(System.currentTimeMillis())
                        .setLatency(new Latency().setOverall(getOverAllLatency(executeParams)));
                sendSseData(emitter, agentEvent);
            }

            SseEmitterUtils.setFailureOnSseEmitter(emitter, new Throwable(errMsg));

            // 发送结束消息块
            sendDone(emitter);
        } catch (AgentStudioException exception) {
            log.error("Fail to send error message [{}], because [{}]", errMsg, exception.getMessage());
        }
    }

    private double getOverAllLatency(AgentExecuteParams executeParams) {
        double overallLatency = (double) (System.currentTimeMillis() - executeParams.getStartTime()) / 1000;
        return Double.parseDouble(String.format(Locale.ROOT, "%.2f", overallLatency));
    }

    // 运行结束，返回done消息块
    private void sendDone(SseEmitter sseEmitter) {
        sendSseData(sseEmitter,
            new AgentEvent().setEvent(EventType.DONE.toString()).setCreatedTime(System.currentTimeMillis()));
        sseEmitter.complete();
    }
}
