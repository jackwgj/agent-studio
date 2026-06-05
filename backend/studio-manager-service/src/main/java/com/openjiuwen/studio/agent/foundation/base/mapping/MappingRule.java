/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MappingRule {
    @JsonProperty("koo_search_error_code")
    private String kooSearchErrorCode;

    @JsonProperty("agent_base_error_code")
    private String agentBaseErrorCode;

    @JsonProperty("description")
    private String description;
}


