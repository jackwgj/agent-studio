/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * aiPpt Token
 */
@Data
public class AiPptTokenOrCodeVo {
    /**
     * ak
     */
    @JsonProperty(value = "api_key")
    private String apiKey;

    /**
     * 用户标识
     */
    @JsonProperty(value = "uid")
    private String uid;

    /**
     * 认证token
     */
    @JsonProperty(value = "token")
    private String token;

    /**
     * 认证code
     */
    @JsonProperty(value = "code")
    private String code;

    /**
     * 过期时间
     */
    @JsonProperty(value = "time_expire")
    private String timeExpire;
}
