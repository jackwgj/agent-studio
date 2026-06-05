/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Map;

/**
 * JiuWen auth service置类型
 *
 */
@Data
public class AuthServiceConfig {
    private String scope;

    private Map<String, String> headers;

    private Map<String, String> query;

    @JsonProperty("crypt_method")
    private String cryptMethod = "ei";
}
