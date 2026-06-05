/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2028. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.validation.annotation.Validated;

@Validated
@Data
public class Audio2TextReq {
    @NotNull(message = "Parameter 'data' must be not null, please check and try again later!")
    private String data;

    @Valid
    private SttConfig config;
}
