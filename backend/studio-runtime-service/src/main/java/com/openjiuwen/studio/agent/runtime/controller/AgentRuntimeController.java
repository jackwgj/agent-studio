/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.REQUEST_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.TASK_ID;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.entity.Text2AudioReq;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.AgentRunReq;
import com.openjiuwen.studio.agent.runtime.dto.AgentRunRsp;
import com.openjiuwen.studio.agent.runtime.dto.LongTermMemoryRuntime;
import com.openjiuwen.studio.agent.runtime.dto.ReleaseInfo;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseRecord;
import com.openjiuwen.studio.agent.runtime.entity.Audio2TextReq;
import com.openjiuwen.studio.agent.runtime.entity.StsTextResp;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.AgentIrWrapper;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.AgentIrCacheService;
import com.openjiuwen.studio.agent.runtime.service.AgentRuntimeService;
import com.openjiuwen.studio.agent.runtime.service.AppReleaseService;
import com.openjiuwen.studio.agent.runtime.service.ConversationManagementService;
import com.openjiuwen.studio.agent.runtime.service.LicenseCtrlService;
import com.openjiuwen.studio.agent.runtime.service.SpeechToTextCustomization;
import com.openjiuwen.studio.agent.runtime.service.TextToSpeechCustomization;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.runtime.utils.CommonUtils;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 知识型Agent运行接口
 *
 */
@Validated
@RestController
@Slf4j
public class AgentRuntimeController {
    private static final String APIG_SOURCE = "APIG";

    @Autowired
    private LicenseCtrlService licenseCtrlService;

    @Autowired
    private AgentRuntimeService agentRuntimeService;

    @Autowired
    private AgentIrCacheService agentIrCacheService;

    @Autowired
    private AppReleaseService appReleaseService;

    @Autowired
    private SpeechToTextCustomization speechToTextCustomization;

    @Autowired
    private TextToSpeechCustomization textToSpeechCustomization;

    @Autowired
    private ConversationManagementService conversationManagementService;

    @Value("${agent-ops.enable:true}")
    private boolean opsEnable;

    @Value("${agent-ops.default-mode:}")
    private String defaultMode;

    private void taskIdManager(String agentId, String conversationId, String executionId) {
        if (StringUtils.isNotBlank(executionId)) {
            MDC.put(TASK_ID, executionId);
            conversationManagementService.deleteTaskId(agentId, conversationId);
            return;
        }
        String taskId = conversationManagementService.queryTaskId(agentId, conversationId);
        if (StringUtils.isEmpty(taskId)) {
            taskId = MDC.get(REQUEST_ID);
        }
        MDC.put(TASK_ID, taskId);
        conversationManagementService.deleteTaskId(agentId, conversationId);
    }

    public Object runWithConversation(String projectId, String workspaceId, String agentId, String conversationId,
        String version, String invokeMode, String type, Boolean stream, String invokeSource, String ownerProjectId,
        String ownerWorkspaceId, AgentRunReq body, Boolean isWebPage, String ownerDomainId, String agentType,
        String executionId, String environmentId) {
        LicenseRecord record = null;
        taskIdManager(agentId, conversationId, executionId);
        if (APIG_SOURCE.equals(invokeSource)) {
            record = licenseCtrlService.agentInvocationCheck(ownerDomainId);
        }
        type = StringUtils.isEmpty(type) ? Constant.AppType.AGENT : type;
        agentType = agentType ==  null ? "" : agentType;
        if (Constant.AppType.AGENT.equals(type)) {
            switch (agentType) {
                case Constant.AppType.DEEP_RESEARCH: {
                    type = Constant.AppType.DEEP_RESEARCH;
                    break;
                }
                case Constant.AppType.PLAN_EXECUTE: {
                    type = Constant.AppType.PLAN_EXECUTE;
                    break;
                }
            }
        }

        String traceMode = CommonUtils.toTraceModel(defaultMode, invokeMode);
        AgentExecuteParams executeParams = AgentExecuteParams.builder()
            .domainId(RequestContextUtils.getRequestUserDomainId())
            .projectId(projectId)
            .workspaceId(StringUtils.isNotEmpty(workspaceId) ? workspaceId : StringUtils.EMPTY)
            .agentId(agentId)
            .debug(Constant.Common.INVOKE_MOD_DEBUG.equalsIgnoreCase(invokeMode))
            .uploadOpsSwitch(CommonUtils.traceEnable(opsEnable, traceMode))
            .requestId(MDC.get(REQUEST_ID))
            .taskId(MDC.get(TASK_ID))
            .traceMode(traceMode)
            .query(body.getQuery())
            .fileUrls(body.getFiles())
            .inputs(body.getInputs())
            .conversationId(conversationId)
            .type(invokeMode)
            .toolSwitchDict(body.getToolSwitchDict())
            .ownerDomainId(ownerDomainId)
            .executeType(type)
            .startTime(System.currentTimeMillis())
            .userId(RequestContextUtils.getRequestUserId())
            .modelDeploymentId(body.getModelDeploymentId())
            .isHistoryEnabled(body.isEnableHistory() == null || body.isEnableHistory())
            .histories(body.getHistories())
            .longTermMemoryRuntime(body.getLongTermMemory())
            .ownerProjectId(ownerProjectId)
            .ownerWorkspaceId(ownerWorkspaceId)
            .isWebPage(isWebPage)
            .memoryRepoId(
                Optional.ofNullable(body.getLongTermMemory()).map(LongTermMemoryRuntime::getMemoryRepoId).orElse(null))
            .environmentId(environmentId)
            .build();

        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        if (apiLog == null) {
            apiLog = new ModelApiLog();
        }
        apiLog.setStartTime(executeParams.getStartTime())
            .setUserId(executeParams.getUserId())
            .setDomainId(executeParams.getDomainId())
            .setModelId(executeParams.getAgentId())
            .setOwnerDomainId(ownerDomainId)
            .setRequestId(MDC.get(REQUEST_ID))
            .setModelName(executeParams.getExecuteType())
            .setInput(executeParams.getQuery());
        executeParams.setApiLog(apiLog);

        if (Objects.equals(Constants.LATEST_PUBLISH_VERSION, version)) {
            executeParams.setLatestPublish(true);
            executeParams.setVersionId(null);
        } else {
            executeParams.setLatestPublish(false);
            executeParams.setVersionId(version);
        }

        try {
            // 流式
            if (stream == null || stream) {
                return agentRuntimeService.runAgentStream(executeParams);
            }
            // 非流式
            return agentRuntimeService.runAgent(executeParams);
        } finally {
            if (APIG_SOURCE.equals(invokeSource)) {
                licenseCtrlService.agentInvocationReport(record, ownerDomainId);
            }
        }
    }

    /**
     * 执行知识型Agent，带会话id
     *
     * @param projectId 租户项目id
     * @param agentId agent id
     * @param conversationId 会话id
     * @param version 发布版本号
     * @param stream 是否流式返回
     * @param body AgentRunReq
     * @return Object，非流式返回AgentRunRsp，流式返回Event
     */
    @ApiOperation(value = "run agent", nickname = "RunAgent", notes = "运行知识型Agent",
        response = AgentRunRsp.class, tags = {"AgentRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AgentRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agents/{agent_id}/conversations/{conversation_id}",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    public Object runAgentWithConversation(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Parameter(in = ParameterIn.QUERY, description = "Agent类型", schema = @Schema())
        @RequestParam(value = "agent_type", required = false) String agentType,
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "agent id", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "conversation id", schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @RequestParam(value = "version", required = false) String version,
        @RequestHeader(value = "X-Invoke-Mode", required = false) String invokeMode,
        @RequestParam(value = "type", required = false) String type,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @RequestHeader(value = "X-Invoke-Source", required = false) String invokeSource,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "环境id", schema = @Schema())
        @RequestParam(value = "environment_id", required = false) String environmentId,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody AgentRunReq body) {
        return runWithConversation(projectId, workspaceId, agentId, conversationId, version, invokeMode, type, stream,
            invokeSource, projectId, workspaceId, body, false, RequestContextUtils.getRequestUserDomainId(),
            agentType, executionId, environmentId);
    }

    /**
     * runAgentWithConversationInner
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param agentType agentType
     * @param agentId agentId
     * @param conversationId conversationId
     * @param version version
     * @param invokeMode invokeMode
     * @param type type
     * @param stream stream
     * @param body body
     * @return Object
     */
    @ApiOperation(value = "run agent inner", nickname = "RunAgentInner",
        notes = "运行知识型Agent, 内部调用, 提供给AgentManagerService, 不注册API-G", response = AgentRunRsp.class,
        tags = {"AgentRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AgentRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/inner/{project_id}/agents/{agent_id}/conversations/{conversation_id}",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    public Object runAgentWithConversationInner(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Parameter(in = ParameterIn.QUERY, description = "Agent类型", schema = @Schema())
        @RequestParam(value = "agent_type", required = false) String agentType,
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "agent id", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId, @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "conversation id", schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @RequestParam(value = "version", required = false) String version,
        @RequestHeader(value = "X-Invoke-Mode", required = false) String invokeMode,
        @RequestParam(value = "type", required = false) String type,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "环境id", schema = @Schema())
        @RequestParam(value = "environment_id", required = false) String environmentId,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody AgentRunReq body) {
        return runWithConversation(projectId, workspaceId, agentId, conversationId, version, invokeMode, type, stream,
            null, projectId, workspaceId, body, false, RequestContextUtils.getRequestUserDomainId(), agentType, executionId, environmentId);
    }

    /**
     * 执行知识型Agent，不带会话id
     *
     * @param projectId 租户项目id
     * @param agentId agent id
     * @param version 发布版本号
     * @param stream 是否流式返回
     * @param body AgentRunReq
     * @param workspaceId workspaceId
     * @param invokeMode invokeMode
     * @return Object，非流式返回AgentRunRsp，流式返回Event
     */
    @ApiOperation(value = "run agent", nickname = "runAgent", notes = "运行知识型Agent", response = AgentRunRsp.class,
        tags = {"AgentRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AgentRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/agents/{agent_id}/conversations", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    public Object runAgent(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Parameter(in = ParameterIn.QUERY, description = "Agent类型", schema = @Schema())
        @RequestParam(value = "agent_type", required = false) String agentType,
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "agent id", required = true, schema = @Schema())
        @PathVariable("agent_id") String agentId, @RequestParam(value = "version", required = false) String version,
        @RequestHeader(value = "X-Invoke-Mode", required = false) String invokeMode,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "环境id", schema = @Schema())
        @RequestParam(value = "environment_id", required = false) String environmentId,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody AgentRunReq body) {
        return runWithConversation(projectId, workspaceId, agentId, UUID.randomUUID().toString(), version, invokeMode,
            Constant.AppType.AGENT, stream, null, projectId, workspaceId, body, false,
            RequestContextUtils.getRequestUserDomainId(), agentType, executionId, environmentId);
    }

    /**
     * 网页应用执行接口
     *
     * @param shortCode shortCode
     * @param stream 是否流式返回
     * @param body AgentRunReq
     * @return Object，非流式返回AgentRunRsp，流式返回Event
     */
    @ApiOperation(value = "run web agent", nickname = "runWebAgent", notes = "运行网页Agent",
        response = AgentRunRsp.class, tags = {"AgentRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AgentRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/agents/chat/{short_code}", produces = {"application/json"},
        consumes = {"application/json"}, method = RequestMethod.POST)
    public Object runWebAgent(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "short_code", required = true, schema = @Schema())
        @PathVariable("short_code") String shortCode, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = true) String workspaceId,
        @Parameter(in = ParameterIn.QUERY, description = "会话id", schema = @Schema())
        @RequestParam(value = "conversation_id", required = false) String conversationId,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody AgentRunReq body) {
        ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(shortCode);
        String projectId = StringUtils.isEmpty(releaseInfo.getProjectId())
            ? RequestContextUtils.getRequestProjectId()
            : releaseInfo.getProjectId();
        String domainId = StringUtils.isEmpty(releaseInfo.getDomainId())
            ? RequestContextUtils.getRequestUserDomainId()
            : releaseInfo.getDomainId();
        AgentIrWrapper irWrapper = StringUtils.isBlank(releaseInfo.getVersionId())
                ? agentIrCacheService.getLatestAgentIr(releaseInfo.getAppId())
                : agentIrCacheService.getPublishedAgentIr(releaseInfo.getAppId(), releaseInfo.getVersionId());

        conversationId = StringUtils.isEmpty(conversationId) ? UUID.randomUUID().toString() : conversationId;

        return runWithConversation(RequestContextUtils.getRequestProjectId(), workspaceId, releaseInfo.getAppId(),
            conversationId, releaseInfo.getVersionId(), null, Constant.AppType.AGENT, stream, null,
            projectId, releaseInfo.getWorkspaceId(), body, true, domainId, irWrapper.getIrType().getValue(), null, null);
    }

    /**
     * 语音转写接口
     */
    @ApiOperation(value = "audio transcription", nickname = "audioTranscription", notes = "语音转写",
        response = Object.class, tags = {"AudioTranscription"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Object.class)})
    @PostMapping(value = "/v1/{project_id}/agents/audio/transcriptions", consumes = {"application/json"})
    public ResponseEntity<StsTextResp> audioTranscriptions(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @RequestBody Audio2TextReq request) {
        return ResponseModel.success(speechToTextCustomization.convertSpeechToText(request));
    }

    /**
     * 文本转语音接口
     */
    @ApiOperation(value = "text To Speech", nickname = "textToSpeech", notes = "文本转语音", response = Object.class,
        tags = {"TextToSpeech"})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Object.class)})
    @PostMapping(value = "/v1/{project_id}/agents/audio/tts", consumes = {"application/json"})
    public JSONObject textToSpeech(@Pattern(regexp = "^[a-zA-Z0-9_-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId, Map<String, String> headers,
        @RequestBody Text2AudioReq request) {
        return textToSpeechCustomization.convertTextToSpeech(request);
    }
}

