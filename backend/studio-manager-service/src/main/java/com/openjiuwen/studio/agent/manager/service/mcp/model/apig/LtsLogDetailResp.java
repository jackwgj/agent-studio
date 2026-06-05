/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.apig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LtsLogDetailResp {

    @JsonProperty("group_name")
    private String groupName;

    @JsonProperty("group_id")
    private String groupId;

    @JsonProperty("stream_id")
    private String streamId;

    @JsonProperty("stream_name")
    private String streamName;
}
