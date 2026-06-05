/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * ocr模型请求体
 *
 */
@Data
@Builder
public class AskOcrReq {
    private String data;

    private String language;

    private boolean table;

    @JsonProperty("single_orientation_mode")
    private boolean singleOrientationMode;

    @JsonProperty("erase_seal")
    private boolean eraseSeal;
}
