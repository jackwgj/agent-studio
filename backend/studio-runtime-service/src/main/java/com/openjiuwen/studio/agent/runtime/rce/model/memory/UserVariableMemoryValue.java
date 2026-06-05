/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.model.memory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 用户变量记忆信息
 *
 */
@Data
public class UserVariableMemoryValue {

    @JsonProperty("variable_key")
    private String variableKey;

    @JsonProperty("variable_value")
    private String variableValue;

    @JsonProperty("update_time")
    private Long updateTime;

}
