
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBody;
import com.openjiuwen.studio.prompt.engineering.dto.VariableDto;
import com.openjiuwen.studio.prompt.engineering.entity.FormConvert;
import com.openjiuwen.studio.prompt.engineering.utils.PromptUtil;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * playground调优，大模型侧请求体
 *
 */
@Getter
@Setter
public class PromptVo {
    private List<Message> messages;

    private String model;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("n")
    private Integer n;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    public static PromptVo convertToPromptVo(PromptBody dto) {
        PromptBodyToPromptVoConverter toPeConversionVoConverter = new PromptBodyToPromptVoConverter();
        return toPeConversionVoConverter.convert(dto);
    }

    private static class PromptBodyToPromptVoConverter implements FormConvert<PromptBody, PromptVo> {
        @Override
        public PromptVo convert(PromptBody promptBody) {
            PromptVo promptVo = new PromptVo();
            Message message = new Message();
            message.setRole("user");
            if (!CollectionUtils.isEmpty(promptBody.getVariables())) {
                Map<String, String> map = promptBody.getVariables().stream()
                        .collect(Collectors.toMap(VariableDto::getKey, VariableDto::getValue, (k, v) -> k));
                message.setContent(
                        PromptUtil.assemblePrompt(promptBody.getQuestion(), "\\{\\{(.*?)\\}\\}", "{{", "}}", map));
            } else {
                message.setContent(promptBody.getQuestion());
            }
            List<Message> promptMessages = new ArrayList<>();
            promptMessages.add(message);
            promptVo.setMessages(promptMessages);
            promptVo.setModel(promptBody.getThirdModelType());
            BeanUtils.copyProperties(promptBody.getModelConfig(), promptVo);
            promptVo.setN(1);
            return promptVo;
        }
    }
}
