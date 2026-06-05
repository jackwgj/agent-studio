/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.Message;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 功能描述
 *
 */
@Data
@Builder
public class AskModelReq {
    @ApiModelProperty(value = "模型名称", example = "180")
    @JsonProperty("model")
    private String modelName;

    @ApiModelProperty(value = "是否流式响应", example = "true")
    @JsonProperty("stream")
    private Boolean isStream;

    @ApiModelProperty(value = "消息列表", required = true)
    @JsonProperty("messages")
    @Size(max = 1000)
    private List<Message> messages;

    @ApiModelProperty(value = "最大生成token数", example = "2048")
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @ApiModelProperty(value = "温度参数", example = "0.5")
    @JsonProperty("temperature")
    private Double temperature;

    @ApiModelProperty(value = "核采样top-p", example = "0.5")
    @JsonProperty("top_p")
    private Double topP;

    @ApiModelProperty(value = "存在惩罚", example = "0")
    @JsonProperty("presence_penalty")
    private Integer presencePenalty;

    @ApiModelProperty(value = "频率惩罚", example = "0")
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @ApiModelProperty(value = "内容安全验证", required = true)
    @JsonProperty("content_security_verify")
    private ContentSecurityVerify contentSecurityVerify;


    @Data
    @Builder
    public static class ContentSecurityVerify {

        @ApiModelProperty(value = "响应验证", example = "false")
        @JsonProperty("is_response_verify")
        private Boolean isResponseVerify;

        @ApiModelProperty(value = "请求验证", example = "false")
        @JsonProperty("is_request_verify")
        private Boolean isRequestVerify;
        public ContentSecurityVerify(Boolean isResponseVerify, Boolean isRequestVerify) {
            this.isResponseVerify = isResponseVerify;
            this.isRequestVerify = isRequestVerify;
        }
    }
}

