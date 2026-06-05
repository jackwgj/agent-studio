/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.entity.AsrCustomShortResponse;
import com.openjiuwen.studio.agent.runtime.entity.Audio2TextReq;
import com.openjiuwen.studio.agent.runtime.entity.StsTextResp;
import com.openjiuwen.studio.agent.runtime.model.RequestResult;
import com.openjiuwen.studio.agent.runtime.properties.SisProperties;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class SpeechToTextCustomization {

    @Value("${agent.builder.sis.domain:}")
    private String sisClientUrl;

    @Value("${agent.builder.sis.iamEndpoint:}")
    private String iamUrl;

    private final SisProperties sisProperties;

    @Autowired
    private OkHttpUtils okHttpUtils;

    public SpeechToTextCustomization(SisProperties sisProperties) {
        this.sisProperties = sisProperties;
    }

    public void sendDone(SseEmitter sseEmitter) {
        try {
            SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event().name("done").data("[Done]");
            sseEmitter.send(sseEventBuilder);
        } catch (IOException e) {
            log.error("Send done event failed!");
        }
    }

    public StsTextResp convertSpeechToText(Audio2TextReq req) {
        log.info("start to call sis, convert speech to text");
        String text = null;
        String httpResponse = null;
        AsrCustomShortResponse response = null;
        Audio2TextReq audio2TextReq = new Audio2TextReq();
        audio2TextReq.setData(req.getData());
        audio2TextReq.setConfig(req.getConfig());
        try {
            String url = (sisClientUrl + "/v1/{sis_project_id}/asr/short-audio").
                    replace("{sis_project_id}", sisProperties.getProjectId());
            String opToken = "";
            final String jsonRequest = JSON.toJSONString(audio2TextReq);

            Map<String, String> headers = new HashMap<>();
            headers.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON);
            headers.put(Constant.Common.X_AUTH_TOKEN, opToken);
            RequestResult requestResult = okHttpUtils.call(url, OkHttpUtils.Method.POST, headers, jsonRequest);
            // 检查请求是否成功
            if (!requestResult.isSuccess()) {
                // 获取错误码
                log.error("sts failed, error code is {} {} {}", requestResult.getCode(), requestResult.getExceptionMessage(), requestResult.getResponse());
                throw new AgentStudioException(StudioError.SIS_VOICE_DURATION_EXCEEDED);
            }
            httpResponse = requestResult.getResponse();
            response = JSONObject.parseObject(httpResponse, AsrCustomShortResponse.class);
        } catch (AgentStudioException e) {
            log.error("sts response timeout!", e);
            throw new AgentStudioException(StudioError.SIS_VOICE_DURATION_EXCEEDED);
        } catch (Exception e) {
            log.error("sts failed!", e);
            throw new AgentStudioException(StudioError.SIS_VOICE_RECOGNITION_FAILED);
        }
        if (response.getResult() == null) {
            log.error("sts result is null {}", JSONObject.parseObject(httpResponse));
        }
        text = response.getResult().getText();
        log.info("converted text is {}", text);
        return new StsTextResp().setMessage(text);
    }
}
