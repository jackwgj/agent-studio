/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.dto.md.ModelServiceCheckReq;
import com.openjiuwen.studio.agent.common.dto.md.ModelServiceCheckRsp;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.common.dto.run.ChatMessage;
import com.openjiuwen.studio.agent.runtime.dto.md.EmbeddingRequest;
import com.openjiuwen.studio.agent.common.dto.md.ModelInvokeBase;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceDetail;
import com.openjiuwen.studio.agent.runtime.dto.md.RankDocumentsRequest;
import com.openjiuwen.studio.agent.runtime.exception.OpenAiHttpException;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelServiceManager;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@Slf4j
@RestController
@Validated
@RequestMapping("/v1")
public class ModelServiceController {
    @Autowired
    private RuntimeModelServiceManager serviceManager;

    @PostMapping("/{project_id}/model-service/status/check")
    public ModelServiceCheckRsp modelServiceStatusCheck(@PathVariable("project_id") String projectId,
        @RequestHeader Map<String, String> headers, @Valid @RequestBody ModelServiceCheckReq request) {
        try {
            ModelApiLogThreadLocal.clear();
            ModelApiLog apiLog = new ModelApiLog().setModelName(request.getModelName())
                .setApi(request.getApiUrl())
                .setModelId(request.getModelName())
                .setDomainId(RequestContextUtils.getRequestUserDomainId())
                .setProjectId(projectId)
                .setStartTime(System.currentTimeMillis());
            ModelApiLogThreadLocal.setApiLog(apiLog);

            ModelInvokeBase invokeBody = createModelInvokeBase(request);
            if (invokeBody == null) {
                log.warn("Model type is not support to check.");
                return new ModelServiceCheckRsp().setSuccess(true).setReason("model type is not support to check.");
            }

            ModelServiceDetail detail = serviceManager.createModelServiceDetail(request);
            try {
                serviceManager.commonInvoke(headers, invokeBody, detail);
                return new ModelServiceCheckRsp().setSuccess(true).setStatusCode(200);
            } catch (OpenAiHttpException e) {
                log.warn("Model service check failed. status code: {}, error response: {}", e.getStatusCode(),
                    e.getErrorResponse());
                return new ModelServiceCheckRsp().setSuccess(e.getStatusCode().value() == 400)
                    .setStatusCode(e.getStatusCode().value())
                    .setReason(e.getErrorResponse());
            } catch (AgentStudioException e) {
                String msg = e.getDetails() == null ? e.getErrorCode().toString() : e.getDetails().toString();
                return new ModelServiceCheckRsp().setSuccess(Integer.parseInt(e.getErrorCode().getCode()) < 300)
                    .setStatusCode(e.getErrorCode().getHttpStatus().value())
                    .setReason(msg);
            } catch (Exception e) {
                log.warn("Model api url is invalid.", e);
                return new ModelServiceCheckRsp().setSuccess(false).setStatusCode(500);
            }
        } finally {
            ModelApiLogThreadLocal.clear();
        }
    }

    private ModelInvokeBase createModelInvokeBase(ModelServiceCheckReq request) {
        switch (request.getModelType().toUpperCase(Locale.ROOT)) {
            case "IMAGE-TO-TEXT":
            case "LLM": {
                ChatCompletionRequest chatInvoke = new ChatCompletionRequest();
                chatInvoke.setModel(request.getModelName());
                chatInvoke.setStream(false);
                ChatMessage chatMessage = new ChatMessage().setRole("user").setContent("你好");
                chatInvoke.setMessages(Collections.singletonList(chatMessage));
                return chatInvoke;
            }
            case "TEXT-EMBEDDING": {
                EmbeddingRequest embeddingInvoke = new EmbeddingRequest();
                embeddingInvoke.setModel(request.getModelName());
                embeddingInvoke.setInput("你好");
                return embeddingInvoke;
            }
            case "RERANK": {
                RankDocumentsRequest randInvoke = new RankDocumentsRequest();
                randInvoke.setDocs(Arrays.asList("a", "b"));
                randInvoke.setQuery("a");
                return randInvoke;
            }
            default: {
                return null;
            }
        }
    }
}
