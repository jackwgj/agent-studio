/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.REQUEST_ID;
import static com.openjiuwen.studio.agent.runtime.constant.Constant.TASK_ID;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.LongTermMemoryRuntime;
import com.openjiuwen.studio.agent.runtime.dto.ReleaseInfo;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunReq;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunRsp;
import com.openjiuwen.studio.agent.runtime.dto.skuinst.LicenseRecord;
import com.openjiuwen.studio.agent.runtime.model.Conversation;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.service.AppReleaseService;
import com.openjiuwen.studio.agent.runtime.service.ConversationManagementService;
import com.openjiuwen.studio.agent.runtime.service.LicenseCtrlService;
import com.openjiuwen.studio.agent.runtime.service.WorkflowRuntimeService;
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * WorkflowRuntime controller，考虑支持流式单独实现
 */
@Validated
@RestController
public class WorkflowRuntimeController {
    private static final String APIG_SOURCE = "APIG";

    @Autowired
    private LicenseCtrlService licenseCtrlService;

    @Autowired
    private WorkflowRuntimeService workflowRuntimeService;

    @Autowired
    private AppReleaseService appReleaseService;

    @Autowired
    private ConversationManagementService conversationManagementService;

    @Value("${agent-ops.enable:true}")
    private boolean opsEnable;

    @Value("${agent-ops.default-mode:}")
    private String defaultMode;

    @Value("${env.type}")
    private String envType;

    @ApiOperation(value = "run workflow applications", nickname = "RunWorkflow",
        notes = "运行场景化应用接口", response = WorkflowRunRsp.class, tags = {"WorkflowRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = WorkflowRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/{project_id}/workflows/{workflow_id}/conversations/{conversation_id}",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    Object runWorkflowWithConversation(@Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "环境id", schema = @Schema())
        @RequestParam(value = "environment_id", required = false) String environmentId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "workflow id", required = true, schema = @Schema())
        @PathVariable("workflow_id") String workflowId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "conversation id", schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @RequestParam(value = "version", required = false) String version,
        @RequestHeader(value = "X-Invoke-Mode", required = false) String invokeMode,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @RequestHeader(value = "X-Invoke-Source", required = false) String invokeSource,
        @RequestHeader(value = "X-Execution-Id", required = false) String executionId,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody WorkflowRunReq body,
        @RequestHeader HttpHeaders httpHeaders) {
        return runWithConversation(projectId, workspaceId, environmentId, workflowId, conversationId, version,
            invokeMode, stream, invokeSource, body, httpHeaders, projectId, workspaceId, false,
            RequestContextUtils.getRequestUserDomainId(), executionId);
    }

    @ApiOperation(value = "run workflow node execution", nickname = "runWorkflowNodeExecute",
        notes = "运行工作流单节点执行", response = WorkflowRunRsp.class, tags = {"WorkflowRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = WorkflowRunRsp.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(
        value = "/v1/{project_id}/workflows/{workflow_id}/conversations/{conversation_id}/node_execute/{node_id}",
        produces = {"application/json"}, consumes = {"application/json"}, method = RequestMethod.POST)
    SseEmitter runWorkflowNodeExecute(@Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "租户项目id", required = true, schema = @Schema())
        @PathVariable("project_id") String projectId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "环境id", schema = @Schema())
        @RequestParam(value = "environment_id", required = false) String environmentId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "workflow id", required = true, schema = @Schema())
        @PathVariable("workflow_id") String workflowId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "conversation id", schema = @Schema())
        @PathVariable("conversation_id") String conversationId, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "node id", schema = @Schema()) @PathVariable("node_id")
        String nodeId, @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody WorkflowRunReq body,
        @RequestHeader HttpHeaders httpHeaders) {
        ExecuteParams executeParams = ExecuteParams.builder()
            .projectId(projectId)
            .workflowId(workflowId)
            .debug(true)
            .inputs(body.getInputs())
            .stream(true)
            .nodeId(nodeId)
            .isNodeExecute(true)
            .startTime(System.currentTimeMillis())
            .conversationId(conversationId)
            .version(body.getVersion())
            .pluginConfigs(body.getPluginConfigs())
            .httpHeaders(httpHeaders)
            .userId(RequestContextUtils.getRequestUserId())
            .inputsUserId(body.getUserId())
            .ownerWorkspaceId(workspaceId)
            .ownerProjectId(projectId)
            .environmentId(environmentId)
            .memoryRepoId(
                Optional.ofNullable(body.getLongTermMemory()).map(LongTermMemoryRuntime::getMemoryRepoId).orElse(null))
            .build();
        // 默认流式
        return workflowRuntimeService.runWorkflowStream(executeParams);
    }

    /**
     * 网页工作流执行接口
     *
     * @param shortCode shortCode
     * @param stream 是否流式返回
     * @param body WorkflowRunReq
     * @return Object，非流式返回WorkflowRunReq，流式返回Event
     */
    @ApiOperation(value = "run web Workflow", nickname = "runWebAgent", notes = "运行网页Workflow",
        response = WorkflowRunReq.class, tags = {"WorkflowRuntime"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = WorkflowRunReq.class),
        @ApiResponse(code = 400, message = "Error response", response = ErrorRsp.class)
    })
    @RequestMapping(value = "/v1/workflows/chat/{short_code}/conversations/{conversation_id}", produces = {
        "application/json"
    }, consumes = {"application/json"}, method = RequestMethod.POST)
    public Object runWebAgent(@Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "short_code", required = true, schema = @Schema())
        @PathVariable("short_code") String shortCode, @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.QUERY, description = "空间id", schema = @Schema())
        @RequestParam(value = "workspace_id", required = false) String workspaceId,
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64)
        @Parameter(in = ParameterIn.PATH, description = "conversation id", schema = @Schema())
        @PathVariable("conversation_id") String conversationId,
        @RequestHeader(value = "stream", required = false) Boolean stream,
        @NotNull @ApiParam(value = "输入参数", required = true) @Valid @RequestBody WorkflowRunReq body) {
        ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(shortCode);
        String ownerProject = StringUtils.isEmpty(releaseInfo.getProjectId())
            ? RequestContextUtils.getRequestProjectId()
            : releaseInfo.getProjectId();
        String ownerDomainId = StringUtils.isEmpty(releaseInfo.getDomainId())
            ? RequestContextUtils.getRequestUserDomainId()
            : releaseInfo.getDomainId();
        return runWithConversation(RequestContextUtils.getRequestProjectId(), null, "", releaseInfo.getAppId(),
            conversationId, releaseInfo.getVersionId(), null, stream, "", body, new HttpHeaders(), ownerProject,
            releaseInfo.getWorkspaceId(), true, ownerDomainId, null);
    }

    private void taskIdManager(String workflowId, String conversationId, String executionId) {
        if (StringUtils.isNotBlank(executionId)) {
            MDC.put(TASK_ID, executionId);
            conversationManagementService.deleteTaskId(workflowId, conversationId);
            return;
        }
        String taskId = conversationManagementService.queryTaskId(workflowId, conversationId);
        if (StringUtils.isEmpty(taskId)) {
            taskId = MDC.get(REQUEST_ID);
        }
        MDC.put(TASK_ID, taskId);
        conversationManagementService.deleteTaskId(workflowId, conversationId);
    }

    public Object runWithConversation(String projectId, String workspaceId, String environmentId, String workflowId,
        String conversationId, String version, String invokeMode, Boolean stream, String invokeSource,
        WorkflowRunReq body, HttpHeaders httpHeaders, String ownerProjectId, String ownerWorkspaceId, Boolean isWebPage,
        String ownerDomainId, String executionId) {
        taskIdManager(workflowId, conversationId, executionId);

        LicenseRecord record = null;
        if (APIG_SOURCE.equals(invokeSource)) {
            record = licenseCtrlService.agentInvocationCheck(ownerDomainId);
        }
        String traceMode = CommonUtils.toTraceModel(defaultMode, invokeMode);
        // 处理记忆变量
        processMemoryVariable(body);
        String workflowExecuteUserId = getWorkflowExecuteUserId();
        ExecuteParams executeParams = ExecuteParams.builder()
            .domainId(RequestContextUtils.getRequestUserDomainId())
            .projectId(projectId)
            .workspaceId(StringUtils.isNotEmpty(workspaceId) ? workspaceId : StringUtils.EMPTY)
            .workflowId(workflowId)
            .debug(Constant.Common.INVOKE_MOD_DEBUG.equalsIgnoreCase(invokeMode))
            .trace(CommonUtils.traceEnable(opsEnable, traceMode))
            .traceMode(traceMode)
            .isTreasure(invokeMode != null && invokeMode.equalsIgnoreCase(Constant.Common.INVOKE_MOD_PUBLISHED))
            .inputs(body.getInputs())
            .stream(stream != null ? stream : true)
            .enableHistory(body.isEnableHistory())
            .isNodeExecute(false)
            .startTime(System.currentTimeMillis())
            .conversationId(conversationId)
            .version(body.getVersion())
            .releasedVersion(version)
            .pluginConfigs(body.getPluginConfigs())
            .httpHeaders(httpHeaders)
            .userId(workflowExecuteUserId)
            .longTermMemoryRuntime(body.getLongTermMemory())
            .inputsUserId(body.getUserId())
            .environmentId(environmentId)
            .ownerProjectId(ownerProjectId)
            .ownerWorkspaceId(ownerWorkspaceId)
            .ownerDomainId(ownerDomainId)
            .isWebPage(isWebPage)
            .memoryRepoId(
                Optional.ofNullable(body.getLongTermMemory()).map(LongTermMemoryRuntime::getMemoryRepoId).orElse(null))
            .build();
        if (body.getConversation() == null) {
            executeParams.setConversation(null);
        } else {
            Conversation conversation = new Conversation();
            conversation.setLastUpdateTime(body.getConversation().getLastUpdateTime());
            conversation.setMessageList(body.getConversation().getMessageList());
            executeParams.setConversation(conversation);
        }

        if (Objects.equals(Constants.LATEST_PUBLISH_VERSION, version)) {
            executeParams.setLatestPublish(true);
            executeParams.setReleasedVersion(null);
        } else {
            executeParams.setLatestPublish(false);
            executeParams.setReleasedVersion(version);
        }

        try {
            // 默认流式
            if (executeParams.isStream()) {
                return workflowRuntimeService.runWorkflowStream(executeParams);
            } else {
                return ResponseModel.success(workflowRuntimeService.runWorkflow(executeParams));
            }
        } finally {
            if (APIG_SOURCE.equals(invokeSource)) {
                licenseCtrlService.agentInvocationReport(record, ownerDomainId);
            }
        }
    }

    private void processMemoryVariable(WorkflowRunReq body) {
        Map<String, Object> memoryInputs = body.getMemoryInputs();
        if (memoryInputs == null || memoryInputs.isEmpty()) {
            return;
        }

        body.setInputs(body.getInputs() == null ? new HashMap<>() : body.getInputs());
        memoryInputs.forEach((key, value) -> body.getInputs().put(key, value));
    }

    /**
     * 处理工作流执行时，传给九问的executeParam中的用户ID
     *
     * @return
     */
    private String getWorkflowExecuteUserId() {
        if (Constant.ENV_SIMPLE.equals(envType)) {
            return RequestContextUtils.getRequestUserIdCompatibleCustom();
        } else {
            return RequestContextUtils.getRequestUserId();
        }
    }
}

