/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import org.springframework.validation.annotation.Validated;

@Validated
@Data
public class Text2AudioReq {
    @Size(min = 1, max = 500)
    @JsonProperty("text")
    @JSONField(name = "text")
    private String text;

    @Valid
    private TtsConfig config;
}
