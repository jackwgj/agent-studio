/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WorkflowChatDetail {
    @JsonProperty(value = "start_time")
    private long startTime;

    @JsonProperty(value = "end_time")
    private long endTime;

    private String text;

    private int index;

    @JsonProperty(value = "node_id")
    private String nodeId;

    @JsonProperty(value = "node_type")
    private String nodeType;

    @JsonProperty(value = "node_name")
    private String nodeName;

    private String createdTime;

    private String message;
}
