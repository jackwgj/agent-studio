/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.openjiuwen.studio.agent.common.dto.md.ChatCompletionRequest;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("maaSV1ApiRequestAdapter")
public class MaaSV1ApiRequestAdapter extends AbstractRequestAdapter {
    @Override
    public String getName() {
        return "maasv1";
    }

    @Override
    public Object requestBodyConvert(Map<String, String> headers, Object body, boolean stream) {
        if(body instanceof ChatCompletionRequest chat){
            if(chat.getThinking()==null){
                return body;
            }
            Map<String, Object> kwargs = new HashMap<>();
            if("disabled".equals(chat.getThinking().getType())){
                kwargs.put("thinking", false);
                kwargs.put("enable_thinking", false);
            }else{
                kwargs.put("thinking", true);
                kwargs.put("enable_thinking", true);
            }
            chat.setThinking(null);
            chat.setChatTemplateKwargs(kwargs);
        }
        return body;
    }
}
