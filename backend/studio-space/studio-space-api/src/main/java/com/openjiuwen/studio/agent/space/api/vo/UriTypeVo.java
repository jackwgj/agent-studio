/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UriTypeVo {
    @JsonProperty("agent_uri")
    private String agentUri;

    @JsonProperty("web_uri")
    private String webUri;
}
