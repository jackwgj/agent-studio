/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 任务展示配置
 */
@Data
@Accessors(chain = true)
public class AgentBuilderTaskDisplayVo {
    @JsonProperty("is_pin")
    private boolean pin;
}