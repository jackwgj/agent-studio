/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.runtime.enums.ModelRespContentType;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Objects;

/**
 * 添加类的描述
 *
 */
@Slf4j
@Component("minimaxRequestAdapter")
public class MinimaxRequestAdapter extends AbstractRequestAdapter {
    @Override
    public String getName() {
        return "minimax";
    }

    @Override
    protected ModelRespContentType getContentType(JSONObject chunk) {
        if (chunk == null) {
            return ModelRespContentType.END_SIGNAL;
        }
        JSONObject baseResp = chunk.getJSONObject("base_resp");
        if (baseResp == null) {
            JSONArray choices = chunk.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return ModelRespContentType.ERR_SIGNAL;
            }
            if (((JSONObject) choices.get(0)).get("finish_reason") == null) {
                return ModelRespContentType.CONTENT_SIGNAL;
            }
            return ModelRespContentType.FINISH_SIGNAL;
        }

        if (!Objects.equals(baseResp.getString("status_code"), "0")) {
            log.error("Minimax Response event message is error. {}", chunk.toJSONString());
            return ModelRespContentType.ERR_SIGNAL;
        }

        return ModelRespContentType.END_SIGNAL;
    }

    @Override
    protected void endHandler(SseEmitter sseEmitter, ModelApiLog apiLog, String content, JSONObject chunk)
        throws IOException {
        // 返回结束标记
        sseEmitter.send(" " + getFinishContentWithRequestId(content, chunk, apiLog));
        sseEmitter.send(" [DONE]");
    }

    protected String getFinishContentWithRequestId(String content, JSONObject chunk, ModelApiLog apiLog) {
        if (chunk == null) {
            return content;
        }

        JSONObject endObj = new JSONObject();
        endObj.put("id", chunk.getString("id"));
        endObj.put("created", chunk.get("created"));
        endObj.put("model", chunk.get("model"));
        endObj.put("object", chunk.get("object"));

        JSONObject choice = new JSONObject();
        choice.put("index", 0);
        choice.put("finish_reason", "stop");
        choice.put("delta", new JSONObject());
        choice.getJSONObject("delta").put("content", "");

        endObj.put("choices", new JSONArray());
        endObj.getJSONArray("choices").add(choice);
        endObj.put("request_id", apiLog.getRequestId());

        JSONObject usage = new JSONObject();

        JSONObject baseUsage = chunk.getJSONObject("usage");
        usage.put("prompt_tokens", baseUsage.get("prompt_tokens"));
        usage.put("total_tokens", baseUsage.get("total_tokens"));
        usage.put("completion_tokens", baseUsage.get("completion_tokens"));

        endObj.put("usage", usage);

        apiLog.setPromptTokens(String.valueOf(usage.get("prompt_tokens")));
        apiLog.setCompletionTokens(String.valueOf(usage.get("completion_tokens")));
        apiLog.setTotalTokens(String.valueOf(usage.get("total_tokens")));

        return endObj.toString();
    }
}
