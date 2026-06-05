/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.ragflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RagFlowBaseEntity {
    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("create_date")
    private String createDate;

    @JsonProperty("update_time")
    private Long updateTime;

    @JsonProperty("update_date")
    private String updateDate;
}
