/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 工具引用对象
 *
 */
@Data
@Getter
@Setter
public class ToolReference {
    @JsonProperty("tool_id")
    private String toolId;

    @JsonProperty("is_resident")
    private Boolean isResident;
}
