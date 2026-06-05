/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.commonservice;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResultNotifyRes {
    @JsonProperty("res_code")
    private String resCode;

    @JsonProperty("res_msg")
    private String resMsg;

    private String result;

    @JsonProperty("module_name")
    private String moduleName;
}
