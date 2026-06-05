/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 具体的plan详情
 */
@Data
public class PlanDataVo {
    @JsonProperty(value = "suggestion")
    private String suggestion;

    @JsonProperty(value = "confirm_data")
    private List<Object> confirmData;
}
