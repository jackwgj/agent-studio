/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 沙箱服务的响应体
 *
 */
@Data
public class PythonInterpreterRsp {

    private String code;

    @JsonProperty("cot_string")
    private String cotString;

    private boolean success;
}
