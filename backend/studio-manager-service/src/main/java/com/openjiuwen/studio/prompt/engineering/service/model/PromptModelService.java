/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.service.model;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.prompt.engineering.dto.InvokeResp;
import com.openjiuwen.studio.prompt.engineering.dto.ModelConfig;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBody;
import com.openjiuwen.studio.prompt.engineering.dto.VariableDto;
import com.openjiuwen.studio.prompt.engineering.mapper.PeTaskMapper;
import com.openjiuwen.studio.prompt.engineering.utils.PromptUtil;
import com.openjiuwen.studio.prompt.engineering.vo.PromptResultVo;

import jakarta.servlet.http.HttpServletRequest;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 对接模型调优接口
 *
 */
@Component
public abstract class PromptModelService<T> {
    private static final String CONTENT_TYPE = "Content-Type";

    private static final String APPLICATION_JSON = "application/json";

    private static final String AGENT_BUILDER_MODEL_CHAT_API = "/v1/%s/deployments/%s/chat/completions";

    private static final String MODEL_CHAT_API = "/v1/agent-builder/chat/completions";

    private static final String THIRD_MODEL_CHAT_API = "/v1/%s/alg-infer/3rdnlp/service/%s/v1/chat/completions";

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptModelService.class);

    protected abstract ResponseEntity<T> call(String xAuthToken, String url, PromptBody promptBody);

    protected abstract InvokeResp getRespFromResultBody(T body, String modelUrl);

    @Value("${inner.agent-runtime.endpoint}")
    private String agentRuntimeEndpoint;

    @Autowired
    private OkHttpClientUtils okHttpClientUtils;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PeTaskMapper peTaskMapper;

    public InvokeResp queryModelCenter(String workSpaceId, PromptBody promptBody, String xAuthToken, String projectId) {
        // 构建模型中心请求体

        String modelAuthId = peTaskMapper.selectModelAuthId(projectId, workSpaceId,
            promptBody.getModelConfig().getId());

        long startTime = System.currentTimeMillis();
        String modelUrl = agentRuntimeEndpoint + MODEL_CHAT_API + "?workspace_id=" + workSpaceId;

        String jsonRequestBody;
        try {
            Map<String, Object> request = getStringObjectMap(promptBody);
            jsonRequestBody = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            LOGGER.error("parse request to jsonRequestBody error, message: {}", e.getMessage());
            throw new AgentStudioException(StudioError.JSON_ENCODE_ERROR);
        }

        RequestBody body = RequestBody.create(jsonRequestBody, okhttp3.MediaType.parse(APPLICATION_JSON));
        Request.Builder requestBuilder = new Request.Builder().url(modelUrl).post(body);
        requestBuilder.addHeader("X-Auth-Id", modelAuthId == null ? "" : modelAuthId);
        requestBuilder.addHeader("X-Auth-Token", xAuthToken == null ? "" : xAuthToken);
        requestBuilder.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        Request requestObj = requestBuilder.build();
        PromptResultVo promptResultVo;
        try (Response okhttpResponse = okHttpClientUtils.getHttpClient().newCall(requestObj).execute()) {
            if (!okhttpResponse.isSuccessful()) {
                LOGGER.error("Call LLM execution error, message: {}, body:{}",
                    JSONObject.toJSONString(okhttpResponse),okhttpResponse.body()==null ? "null" :
                        okhttpResponse.body().string());
                throw new AgentStudioException(StudioError.CALL_LLM_EXECUTION_ERROR);
            }

            String responseBody = okhttpResponse.body().string();
            LOGGER.info("ModelService.queryModel get request from model {} response = {}", promptBody.getModelType(),
                JSONObject.toJSONString(responseBody));
            promptResultVo = mapper.readValue(responseBody, PromptResultVo.class);
            Objects.requireNonNull(promptResultVo)
                .setTime(PromptUtil.formatDouble((System.currentTimeMillis() - startTime) / 1000.0));
        } catch (IOException e) {
            LOGGER.error("call model {} failed: {}", modelUrl, e);
            throw new AgentStudioException(StudioError.CANT_OBTAIN_TEXT_FROM_LLM);
        }

        final InvokeResp invokeResp = getRespFromResultBody((T) promptResultVo, modelUrl);
        invokeResp.setModelType(promptBody.getModelType());
        invokeResp.setModel(promptBody.getModel());
        return invokeResp;
    }

    @NotNull
    private static Map<String, Object> getStringObjectMap(PromptBody promptBody) {
        Map<String, Object> request = new HashMap<>();

        // 基础参数
        ModelConfig modelConfig = promptBody.getModelConfig();
        request.put("model", modelConfig.getId());
        request.put("stream", false);
        request.put("max_tokens", modelConfig.getMaxTokens());
        request.put("temperature", modelConfig.getTemperature());
        request.put("top_p", modelConfig.getTopP());
        request.put("n", 1);
        request.put("presence_penalty", modelConfig.getPresencePenalty());
        request.put("frequency_penalty", 0);

        // 构建messages数组
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");

        if (!CollectionUtils.isEmpty(promptBody.getVariables())) {
            Map<String, String> map = promptBody.getVariables()
                .stream()
                .collect(Collectors.toMap(VariableDto::getKey, VariableDto::getValue, (k, v) -> k));
            message.put("content",
                PromptUtil.assemblePrompt(promptBody.getQuestion(), "\\{\\{(.*?)\\}\\}", "{{", "}}", map));
        } else {
            message.put("content", promptBody.getQuestion());
        }
        messages.add(message);
        request.put("messages", messages);
        return request;
    }

    private HttpHeaders extractHeaders() {
        Map<String, String> map = new LinkedHashMap<>();
        ServletRequestAttributes attributes
            = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Enumeration<String> names = request.getHeaderNames();
        HttpHeaders headers = new HttpHeaders();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            headers.add(name, request.getHeader(name));
        }
        return headers;
    }

    private ResponseEntity<T> askModel(String xAuthToken, PromptBody promptBody, String modelUrl) {
        long start = System.currentTimeMillis();
        try {
            return call(xAuthToken, modelUrl, promptBody);
        } finally {
            LOGGER.info("Call {}({}) cost {} ms", promptBody.getModelType(), promptBody.getModel(),
                System.currentTimeMillis() - start);
        }
    }
}
