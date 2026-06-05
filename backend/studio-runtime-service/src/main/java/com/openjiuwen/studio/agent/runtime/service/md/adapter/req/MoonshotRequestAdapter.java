/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 添加类的描述
 *
 */
@Slf4j
@Component("moonshotRequestAdapter")
public class MoonshotRequestAdapter extends AbstractRequestAdapter {
    @Override
    public String getName() {
        return "moonshot";
    }

    @Override
    protected void finishHandler(SseEmitter sseEmitter, ModelApiLog apiLog, String content, JSONObject chunk)
        throws IOException {
        // 结束处理
        String msg = getFinishRequestMsg(content, chunk, apiLog);
        sseEmitter.send(msg.startsWith(" ") ? msg : " " + msg);
    }

    private String getFinishRequestMsg(String content, JSONObject chunk, ModelApiLog apiLog) {
        if (chunk == null) {
            return content;
        }

        JSONArray choices = chunk.getJSONArray("choices");
        JSONObject usage;
        if (choices == null || choices.isEmpty()) {
            usage = chunk.getJSONObject(USAGE);
        } else {
            JSONObject choice = (JSONObject) choices.get(0);
            usage = choice.getJSONObject(USAGE);
            if (usage != null) {
                choice.remove(USAGE);
                chunk.put(USAGE, usage);
            }
        }
        chunk.put("request_id", apiLog.getRequestId());

        if (usage != null) {
            apiLog.setTotalTokens(String.valueOf(usage.get("total_tokens")));
            apiLog.setCompletionTokens(String.valueOf(usage.get("completion_tokens")));
            apiLog.setPromptTokens(String.valueOf(usage.get("prompt_tokens")));
        }

        return chunk.toString();
    }
}
