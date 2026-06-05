/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class PrePluginAuthServiceConfig {

    private Integer limit;

    private Integer usage;

    private String domainId;

    private String projectId;

    private String baseUrl;

    private String workspaceId;

    private String scope;

    private Map<String, String> headers;

    private Map<String, String> query;

    @JsonProperty("crypt_method")
    private String cryptMethod = "ei";
}
