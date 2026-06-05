/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * 添加类的描述
 *
 */
@Slf4j
@Component("baiChuanRequestAdapterRun")
public class BaiChuanRequestAdapter extends AbstractRequestAdapter {
    @Override
    public String getName() {
        return "baichuan";
    }

    @Override
    public Object requestBodyConvert(Map<String, String> headers, Object body, boolean stream) {
        if (body instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bodyMap = (Map<String, Object>) body;
            bodyMap.remove("thinking");
            // 进一步检查键和值的类型
            if (bodyMap.containsKey("frequency_penalty")) {
                Object frequencyPenaltyObj = bodyMap.get("frequency_penalty");
                if (frequencyPenaltyObj instanceof Number number) {
                    double stand = number.doubleValue();
                    double newValue = stand < 0 ? 1 : 1 + (stand / 2);
                    bodyMap.put("frequency_penalty", newValue);
                }
            }
            return bodyMap;
        } else if (body instanceof ChatCompletionRequest chatReq) {
            chatReq.setThinking(null);
            Float frequencyPenalty = chatReq.getFrequencyPenalty();
            if (frequencyPenalty != null) {
                chatReq.setFrequencyPenalty(frequencyPenalty < 0 ? 1 : 1 + (frequencyPenalty / 2));
            }
            return chatReq;
        } else {
            return body;
        }
    }

    @Override
    protected void finishHandler(SseEmitter sseEmitter, ModelApiLog apiLog, String content, JSONObject chunk)
        throws IOException {
        // 结束处理
        String msg = getContentWithRequestId(content, chunk, apiLog);
        sseEmitter.send(msg.startsWith(" ") ? msg : " " + msg);

        chunk.put("choices", new JSONArray(0));
        sseEmitter.send(" " + chunk.toJSONString());
    }
}
