/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.annotation.SecurityCheck;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.runtime.dto.md.EmbeddingRequest;
import com.openjiuwen.studio.agent.runtime.dto.md.ImageGenerationReq;
import com.openjiuwen.studio.agent.runtime.dto.md.RankDocumentsRequest;
import com.openjiuwen.studio.agent.runtime.dto.md.VideoGenerationReq;
import com.openjiuwen.studio.agent.runtime.exception.OpenAiHttpException;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.rce.client.AgentManagerClient;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelServiceManager;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import io.micrometer.common.util.StringUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Validated
@RequestMapping("/v1")
public class RuntimeModelServiceController {
    @Autowired
    private RuntimeModelServiceManager modelService;

    @Autowired
    private AgentManagerClient managerClient;

    private void updateModelStatus(boolean refresh, String modelId, boolean success, String projectId,
        String workspaceId) {
        if (!refresh) {
            return;
        }
        if (workspaceId == null || workspaceId.isEmpty()) {
            return;
        }
        Map<String, String> body = new HashMap<>(2);
        body.put("model_id", modelId.contains("|") ? modelId.split("\\|")[0] : modelId);
        body.put("status", success ? "success" : "fail");
        try {
            log.info("Start to update model status. {} {} {}", projectId, workspaceId, body);
            managerClient.updateModelStatus(RequestContextUtils.getRequestAuthToken(), projectId, workspaceId, body);
        } catch (Exception e) {
            log.error("Update model status fail. ", e);
        }
    }

    @ApiOperation(value = "模型调测", nickname = "chatCompletions", notes = "chatCompletions", response = Object.class,
        tags = {"RuntimeModelServiceController"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "chatCompletions", response = Object.class)
    })
    @PostMapping(value = "/agent-builder/chat/completions", consumes = {"application/json"})
    @SecurityCheck
    public Object chatCompletions(
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @RequestParam(value = "workspace_id", required = false)
        String workspaceId, @RequestHeader(value = "X-Workspace-Id", required = false) String hdWorkspaceId,
        @RequestHeader Map<String, String> headers, @Valid @RequestBody ChatCompletionRequest request,
        @RequestParam(value = "refresh", required = false) Boolean refresh,
        @RequestHeader(value = "X-Auth-Id", required = false) String authId,
        @RequestHeader(value = "X-Owner-Project-Id", required = false) String ownerProjectId) {
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        apiLog.setThreadId(Thread.currentThread().getName()).setOwnerProjectId(ownerProjectId);
        String projectId = RequestContextUtils.getRequestProjectId();
        apiLog.setProjectId(projectId);
        workspaceId = getAndValidWorkspaceId(workspaceId, hdWorkspaceId, headers);
        apiLog.setWorkspaceId(workspaceId);

        apiLog.setThreadId(Thread.currentThread().getName());

        apiLog.setServiceKey(request.getModel());

        boolean stream = Boolean.TRUE.equals(request.getStream());
        apiLog.setStream(stream);

        long start = System.currentTimeMillis();
        log.info("Model chat completion. workspace:{} ownerProject:{} Project:{} ModelService:{} authId:{} IsStream:{}", workspaceId, ownerProjectId, projectId, request.getModel(), authId, stream);
        boolean rspStatus = true;
        boolean rf = Boolean.TRUE.equals(refresh) || "true".equals(request.getRefresh());
        String modelId = request.getModel();
        try {
            request.setRefresh(null);
            ownerProjectId = StringUtils.isEmpty(ownerProjectId) ? projectId : ownerProjectId;
            Object obj = modelService.chatCompletions(workspaceId, request.getModel(), stream, headers, request, rf,
                projectId, authId, ownerProjectId);
            updateModelStatus(rf, modelId, true, projectId, workspaceId);
            return obj;
        } catch (Exception e) {
            updateModelStatus(rf, modelId, false, projectId, workspaceId);
            if (e instanceof OpenAiHttpException) {
                throw (OpenAiHttpException) e;
            }
            rspStatus = false;
            if (e instanceof AgentStudioException) {
                throw e;
            }
            log.error("Model chat completion exception. Project:{} ModelService:{} IsStream:{}", "", request.getModel(),
                stream, e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        } finally {
            log.info("Model chat completion finish. Staus:{} Project:{} ModelService:{} IsStream:{} Cost:{}ms",
                rspStatus, "", request.getModel(), stream, System.currentTimeMillis() - start);
        }
    }

    @ApiOperation(value = "文本向量化", nickname = "textEmbeddings", notes = "textEmbeddings", response = Object.class,
        tags = {"RuntimeModelServiceController"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "textEmbeddings", response = Object.class)
    })
    @PostMapping(value = "/agent-builder/embeddings", consumes = {"application/json"})
    public Object textEmbeddings(
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @RequestParam(value = "workspace_id", required = false)
        String workspaceId, @RequestHeader(value = "X-Workspace-Id", required = false) String hdWorkspaceId,
        @RequestHeader Map<String, String> headers, @RequestBody EmbeddingRequest request,
        @RequestParam(value = "refresh", required = false) Boolean refresh,
        @RequestHeader(value = "X-Auth-Id", required = false) String authId,
        @RequestHeader(value = "X-Owner-Project-Id", required = false) String ownerProjectId) {
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        apiLog.setThreadId(Thread.currentThread().getName());
        String projectId = RequestContextUtils.getRequestProjectId();
        apiLog.setProjectId(projectId);
        workspaceId = getAndValidWorkspaceId(workspaceId, hdWorkspaceId, headers);
        apiLog.setWorkspaceId(workspaceId);

        String model = request.getModel();
        apiLog.setServiceKey(model);
        apiLog.setStream(false);

        long start = System.currentTimeMillis();
        log.info("Model text embeddings. Project:{} ModelService:{} IsStream:{}", "", model, false);
        boolean rf = Boolean.TRUE.equals(refresh) || "true".equals(request.getRefresh());
        boolean rspStatus = true;
        String modelId = request.getModel();
        try {
            request.setRefresh(null);
            ownerProjectId = StringUtils.isEmpty(ownerProjectId) ? projectId : ownerProjectId;
            Object obj = modelService.textEmbeddings(workspaceId, model, headers, request, rf, projectId, authId,
                ownerProjectId);
            updateModelStatus(rf, modelId, true, projectId, workspaceId);
            return obj;
        } catch (Exception e) {
            updateModelStatus(rf, modelId, false, projectId, workspaceId);
            if (e instanceof OpenAiHttpException) {
                throw (OpenAiHttpException) e;
            }
            rspStatus = false;
            if (e instanceof AgentStudioException) {
                throw e;
            }
            log.error("Model text embeddings exception. Project:{} ModelService:{} ", "", model, e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        } finally {
            log.info("Model text embeddings finish. status:{} Project:{} ModelService:{} Cost:{}ms", rspStatus, "",
                model, System.currentTimeMillis() - start);
        }
    }

    @ApiOperation(value = "重排序接口", nickname = "rerank", notes = "rerank", response = Object.class,
        tags = {"RuntimeModelServiceController"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "rerank", response = Object.class)
    })
    @PostMapping(value = "/agent-builder/rerank", consumes = {"application/json"})
    public Object rerank(
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @RequestParam(value = "workspace_id", required = false)
        String workspaceId, @RequestHeader(value = "X-Workspace-Id", required = false) String hdWorkspaceId,
        @RequestHeader Map<String, String> headers, @Valid @RequestBody RankDocumentsRequest request,
        @RequestParam(value = "refresh", required = false) Boolean refresh,
        @RequestHeader(value = "X-Auth-Id", required = false) String authId,
        @RequestHeader(value = "X-Owner-Project-Id", required = false) String ownerProjectId) {
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        String projectId = RequestContextUtils.getRequestProjectId();
        apiLog.setProjectId(projectId);
        workspaceId = getAndValidWorkspaceId(workspaceId, hdWorkspaceId, headers);
        apiLog.setWorkspaceId(workspaceId);
        apiLog.setThreadId(Thread.currentThread().getName());

        String model = request.getModel();
        apiLog.setStream(false);
        apiLog.setServiceKey(model);

        long start = System.currentTimeMillis();
        log.info("Model text rerank. Project:{} ModelService:{} IsStream:{}", "", model, false);
        boolean rspStatus = true;
        boolean rf = Boolean.TRUE.equals(refresh) || "true".equals(request.getRefresh());
        String modelId = request.getModel();
        try {
            request.setRefresh(null);
            ownerProjectId = StringUtils.isEmpty(ownerProjectId) ? projectId : ownerProjectId;
            Object obj = modelService.rerank(workspaceId, model, headers, request, rf, projectId, authId,
                ownerProjectId);
            updateModelStatus(rf, modelId, true, projectId, workspaceId);
            return obj;
        } catch (Exception e) {
            updateModelStatus(rf, modelId, false, projectId, workspaceId);
            if (e instanceof OpenAiHttpException) {
                throw (OpenAiHttpException) e;
            }
            rspStatus = false;
            if (e instanceof AgentStudioException) {
                throw e;
            }
            log.error("Model text rerank exception. Project:{} ModelService:{} ", "", model, e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        } finally {
            log.info("Model text rerank finish. status:{} Project:{} ModelService:{} Cost:{}ms", rspStatus, "", model,
                System.currentTimeMillis() - start);
        }
    }

    @ApiOperation(value = "图像生成", nickname = "imageGenerations", notes = "imageGenerations",
        response = Object.class, tags = {"RuntimeModelServiceController"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "imageGenerations", response = Object.class)
    })
    @PostMapping(value = "/agent-builder/images/generations", consumes = {"application/json"})
    public Object imageGenerations(
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @RequestParam(value = "workspace_id", required = false)
        String workspaceId, @RequestHeader(value = "X-Workspace-Id", required = false) String hdWorkspaceId,
        @RequestHeader Map<String, String> headers, @Valid @RequestBody ImageGenerationReq request,
        @RequestParam(value = "refresh", required = false) Boolean refresh,
        @RequestHeader(value = "X-Auth-Id", required = false) String authId,
        @RequestHeader(value = "X-Owner-Project-Id", required = false) String ownerProjectId) {
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        String projectId = RequestContextUtils.getRequestProjectId();
        workspaceId = getAndValidWorkspaceId(workspaceId, hdWorkspaceId, headers);
        String model = request.getModel();

        apiLog.setProjectId(projectId);
        apiLog.setStream(false);
        apiLog.setServiceKey(model);
        apiLog.setWorkspaceId(workspaceId);
        apiLog.setThreadId(Thread.currentThread().getName());

        long start = System.currentTimeMillis();
        log.info("Model text to image. Project:{} ModelService:{} IsStream:{}", "", model, false);
        boolean rspStatus = true;
        boolean rf = Boolean.TRUE.equals(refresh) || "true".equals(request.getRefresh());
        try {
            request.setRefresh(null);
            ownerProjectId = StringUtils.isEmpty(ownerProjectId) ? projectId : ownerProjectId;
            Object obj = modelService.imageGenerations(workspaceId, model, headers, request, rf, projectId, authId,
                ownerProjectId);
            updateModelStatus(rf, model, true, projectId, workspaceId);
            return obj;
        } catch (Exception e) {
            updateModelStatus(rf, model, false, projectId, workspaceId);
            if (e instanceof OpenAiHttpException) {
                throw (OpenAiHttpException) e;
            }
            rspStatus = false;
            if (e instanceof AgentStudioException) {
                throw e;
            }
            log.error("Model text to image exception. Project:{} ModelService:{} ", "", model, e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        } finally {
            log.info("Model text to image finish. status:{} Project:{} ModelService:{} Cost:{}ms", rspStatus, "", model,
                System.currentTimeMillis() - start);
        }
    }

    @ApiOperation(value = "视频生成", nickname = "videoGenerations", notes = "videoGenerations",
        response = Object.class, tags = {"RuntimeModelServiceController"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "videoGenerations", response = Object.class)
    })
    @PostMapping(value = "/agent-builder/video/generations", consumes = {"application/json"})
    public Object videoGenerations(
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @RequestParam(value = "workspace_id", required = false)
        String workspaceId, @RequestHeader(value = "X-Workspace-Id", required = false) String hdWorkspaceId,
        @RequestHeader Map<String, String> headers, @Valid @RequestBody VideoGenerationReq request,
        @RequestParam(value = "refresh", required = false) Boolean refresh,
        @RequestHeader(value = "X-Auth-Id", required = false) String authId,
        @RequestHeader(value = "X-Owner-Project-Id", required = false) String ownerProjectId) {
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        String projectId = RequestContextUtils.getRequestProjectId();
        workspaceId = getAndValidWorkspaceId(workspaceId, hdWorkspaceId, headers);

        apiLog.setProjectId(projectId);
        apiLog.setStream(false);
        apiLog.setServiceKey(request.getModel());
        apiLog.setWorkspaceId(workspaceId);
        apiLog.setThreadId(Thread.currentThread().getName());

        long start = System.currentTimeMillis();
        log.info("Model text to video. Project:{} ModelService:{} IsStream:{}", "", request.getModel(), false);
        boolean rspStatus = true;
        boolean rf = Boolean.TRUE.equals(refresh) || "true".equals(request.getRefresh());

        String modelId = request.getModel();
        try {
            request.setRefresh(null);
            ownerProjectId = StringUtils.isEmpty(ownerProjectId) ? projectId : ownerProjectId;
            Object obj = modelService.videoGenerations(workspaceId, modelId, headers, request, rf, projectId, authId,
                ownerProjectId);
            updateModelStatus(rf, modelId, true, projectId, workspaceId);
            return obj;
        } catch (Exception e) {
            updateModelStatus(rf, modelId, false, projectId, workspaceId);
            if (e instanceof OpenAiHttpException) {
                throw (OpenAiHttpException) e;
            }
            rspStatus = false;
            if (e instanceof AgentStudioException) {
                throw e;
            }
            log.error("Model text to video exception. Project:{} ModelService:{} ", "", modelId, e);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        } finally {
            log.info("Model text to video finish. status:{} Project:{} ModelService:{} Cost:{}ms", rspStatus, "", modelId,
                System.currentTimeMillis() - start);
        }
    }

    @ApiOperation(value = "查询视频生成任务", nickname = "queryVideoGenerationTask", notes = "queryVideoGenerationTask",
        response = Object.class, tags = {"RuntimeModelServiceController"})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "queryVideoGenerationTask", response = Object.class)
    })
    @GetMapping(value = "/agent-builder/video/generations/{task_id}")
    public Object queryVideoGenerationTask(
        @Pattern(regexp = "^[a-zA-Z0-9_()-]+$") @Size(max = 64) @RequestParam(value = "workspace_id", required = false)
        String workspaceId, @RequestHeader(value = "X-Workspace-Id", required = false) String hdWorkspaceId,
        @RequestHeader(value = "X-Auth-Id", required = false) String authId, @PathVariable("task_id") String taskId) {
        ModelApiLog apiLog = ModelApiLogThreadLocal.getApiLog();
        String projectId = RequestContextUtils.getRequestProjectId();
        String workspace = StringUtils.isEmpty(workspaceId) ? hdWorkspaceId : workspaceId;
        apiLog.setProjectId(projectId);
        return modelService.queryVideoGenerationTask(projectId, workspace, authId, taskId);
    }

    private String getAndValidWorkspaceId(String workspaceId, String hdWorkspaceId, Map<String, String> headers) {
        if (StringUtils.isEmpty(workspaceId)) {
            workspaceId = hdWorkspaceId;
        }
        if (StringUtils.isEmpty(workspaceId)) {
            workspaceId = "";
        }
        return workspaceId;
    }
}
