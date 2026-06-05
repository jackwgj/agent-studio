/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.ModelInfo;
import com.openjiuwen.studio.agent.runtime.dto.PromptBuilderReq;
import com.openjiuwen.studio.agent.common.dto.prompt.PromptBuilderRsp;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceListRsp;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceRsp;
import com.openjiuwen.studio.agent.runtime.rce.client.AgentManagerClient;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
@Slf4j
@Service
public class PromptBuilderService {
    @Autowired
    private AsyncService asyncService;

    @Value("${model.prompt-build.id}")
    private String promptBuildModelId;

    @Value("${model.prompt-build.type:openai}")
    private String promptBuildModelType;

    @Autowired
    private AgentManagerClient agentManagerClient;

    @Autowired
    private JiuWenClient jiuWenClient;

    public SseEmitter promptBuilder(PromptBuilderReq body, String projectId, String workspaceId) {
        SseEmitter sseEmitter = new SseEmitter(Constant.PROMPT_BUILDER_STREAM_TIMEOUT);
        String token = RequestContextUtils.getRequestAuthToken();

        if (body.getModelInfo() != null) {
            ResponseEntity<ModelServiceListRsp> models = agentManagerClient.queryAvailableModels(token, projectId,
                workspaceId, body.getModelInfo().getModel());
            List<ModelServiceRsp> datas = models.getBody() == null || models.getBody().getData() == null
                ? new ArrayList<>(0)
                : models.getBody().getData();
            boolean exist = false;
            for (ModelServiceRsp model : datas) {
                if (model.getId().equals(body.getModelInfo().getModel())) {
                    log.info("Success found model.");
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                log.error("Prompt build used model is not available. model:{} project:{}",
                    body.getModelInfo().getModel(), projectId);
                throw new AgentStudioException(StudioError.MD_MODEL_SERVICE_NOT_AVAILABLE);
            }
        }

        try {
            asyncService.callPromptBuilder(() -> {
                try {
                    // 如果prompt build请求中没有模型信息，则取配置的默认模型
                    if (body.getModelInfo() == null) {
                        ModelInfo modelInfo = new ModelInfo();
                        modelInfo.setModel(promptBuildModelId);
                        modelInfo.setModelSource(promptBuildModelType);
                        body.setModelInfo(modelInfo);
                    }
                    if (body.getModelInfo().getModelSource() == null) {
                        body.getModelInfo().setModelSource(promptBuildModelType);
                    }
                    if (body.getModelInfo().getModel() == null) {
                        body.getModelInfo().setModel(promptBuildModelId);
                    }

                    Flux<Object> flux = jiuWenClient.promptBuildStream(token, workspaceId, body);
                    Disposable disposable = flux.subscribe(data -> {
                        try {
                            sseEmitter.send(SseEmitter.event().data(data).build());
                        } catch (IOException e) {
                            log.error("error to send sse event:", e);
                            sendModelException(sseEmitter);
                        }
                    }, error -> {
                        log.error("error to receive sse event:", error);
                        sendModelException(sseEmitter);
                    }, () -> {
                        sendDone(sseEmitter);
                        sseEmitter.complete();
                    });
                    sseEmitter.onCompletion(disposable::dispose);
                    sseEmitter.onError(ex -> {
                        log.warn("SSE emitter error, disposing flux", ex);
                        disposable.dispose();
                    });
                } catch (Exception e) {
                    log.error("When call prompt-builder api, model service exception.", e);
                    sendModelException(sseEmitter);
                } finally {
                    sendDone(sseEmitter);
                    sseEmitter.complete();
                }
            });
        } catch (Exception e) {
            log.error("Can not submit callPromptBuilder async task", e);
            sendAsyncException(sseEmitter);
        }
        return sseEmitter;
    }

    private void sendDone(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event().name("done").data("[Done]"));
        } catch (IOException e) {
            log.error("Send done event failed!");
        }
    }

    private void sendModelException(SseEmitter sseEmitter) {
        try {
            PromptBuilderRsp promptBuilderRsp = new PromptBuilderRsp();
            promptBuilderRsp.setCode(1);
            promptBuilderRsp.setMessage("Model exception. Please try again.");
            promptBuilderRsp.setData("");
            sseEmitter.send(SseEmitter.event().name("exception").data(promptBuilderRsp, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            log.error("Send prompt-builder run exception event failed!");
        }
    }

    private void sendAsyncException(SseEmitter sseEmitter) {
        try {
            PromptBuilderRsp promptBuilderRsp = new PromptBuilderRsp();
            promptBuilderRsp.setCode(2);
            promptBuilderRsp.setMessage("Async exception.");
            promptBuilderRsp.setData("");
            sseEmitter.send(SseEmitter.event().name("exception").data(promptBuilderRsp, MediaType.APPLICATION_JSON));
        } catch (IOException e) {
            log.error("Send Async exception event failed!");
        }
    }
}
