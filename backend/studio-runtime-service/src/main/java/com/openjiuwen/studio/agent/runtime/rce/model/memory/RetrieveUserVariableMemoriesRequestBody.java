/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.model.memory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 用户变量记忆召回的请求体
 *
 */
@Data
public class RetrieveUserVariableMemoriesRequestBody {
    @JsonProperty("variable_keys")
    private List<String> variableKeys;
}
