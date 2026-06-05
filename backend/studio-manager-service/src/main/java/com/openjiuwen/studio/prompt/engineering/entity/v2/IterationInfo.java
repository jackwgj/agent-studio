/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.entity.v2;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IterationInfo  implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 迭代轮次
     */
    @JsonProperty("iteration_round")
    private int iterationRound;

    /**
     * 该轮优化后的提示词
     */
    @JsonProperty("optimized_prompt")
    private String optimizedPrompt;

    /**
     * 该轮准确率
     */
    @JsonProperty("success_rate")
    private double successRate;
}
