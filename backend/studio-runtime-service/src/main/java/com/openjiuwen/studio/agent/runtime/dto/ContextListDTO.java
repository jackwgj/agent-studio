/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContextListDTO {

    @JsonProperty("context")
    String context;

    @JsonProperty("workflow_list")
    List<WorkFlowDTO> workflowList;

    @JsonProperty("event_list")
    List<com.openjiuwen.studio.agent.common.dto.agent.NodeRunInfo> eventList;


    @JsonProperty("name")
    private String name;

    @JsonProperty("value_before")
    private String valueBefor;

    @JsonProperty("value_after")
    private String valueAfter;
}
