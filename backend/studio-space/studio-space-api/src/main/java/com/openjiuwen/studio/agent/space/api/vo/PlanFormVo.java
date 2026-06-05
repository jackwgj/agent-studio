/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 运行时生成计划解析
 */
@Data
public class PlanFormVo {
    @JsonProperty(value = "tool_type")
    private String toolType;

    @JsonProperty(value = "tool_name")
    private String tooName;

    @JsonProperty(value = "plan_list")
    private PlanDataVo planList;
}
