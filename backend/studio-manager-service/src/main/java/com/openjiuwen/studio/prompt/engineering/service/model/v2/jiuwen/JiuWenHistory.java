/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiuWenHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("evaluations")
    private List<JiuWenEvaluation> evaluations;

    @JsonProperty("examples")
    private List<String> examples;

    @JsonProperty("filled_prompt")
    private String filledPrompt;

    @JsonProperty("iteration_round")
    private int iterationRound;

    @JsonProperty("optimized_placeholder")
    private Map<String, String> optimizedPlaceholder;

    @JsonProperty("optimized_prompt")
    private String optimizedPrompt;

    @JsonProperty("original_placeholder")
    private Map<String, String> originalPlaceholder;

    @JsonProperty("success_rate")
    private double successRate;
}
