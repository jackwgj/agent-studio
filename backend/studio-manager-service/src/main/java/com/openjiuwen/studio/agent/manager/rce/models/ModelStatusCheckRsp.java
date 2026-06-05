/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * 模型状态检查返回值
 *
 */
@Data
@Builder
public class ModelStatusCheckRsp {
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;
}
