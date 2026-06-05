/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Created by Fang Zhen on 2024/3/18.
 */
@Data
public class ModelSetting {
    // 最大token数
    @Min(1)
    @Max(40000)
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    // 温度
    @Max(2)
    private double temperature;

    // 多样性
    @Min(0)
    @Max(1)
    @JsonProperty("top_p")
    private double topP;

    // 存在惩罚
    @Min(-2)
    @Max(2)
    @JsonProperty("presence_penalty")
    private double presencePenalty;

    // 频率惩罚
    @Min(-2)
    @Max(2)
    @JsonProperty("frequency_penalty")
    private double frequencyPenalty;
}
