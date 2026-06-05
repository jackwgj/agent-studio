/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * AiPpt基础响应体
 */
@Data
public class AiPptBaseRsp<T> {
    @JsonProperty(value = "code")
    private String code;

    /**
     * data信息
     */
    @JsonProperty(value = "data")
    private T data;

    @JsonProperty(value = "msg")
    private String msg;
}
