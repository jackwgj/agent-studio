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
public class JiuWenProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("best_placeholder")
    private Map<String, String> bestPlaceholder;

    @JsonProperty("best_prompt")
    private String bestPrompt;

    @JsonProperty("error_msg")
    private String errorMsg;

    @JsonProperty("evaluation_method")
    private String evaluationMethod;

    @JsonProperty("examples")
    private List<String> examples;

    @JsonProperty("filled_prompt")
    private String filledPrompt;

    @JsonProperty("job_info")
    private JiuWenJobInfo jobInfo;

    @JsonProperty("original_placeholder")
    private Map<String, String> originalPlaceholder;

    @JsonProperty("original_prompt")
    private String originalPrompt;

    @JsonProperty("progress_rate")
    private double progressRate;

    @JsonProperty("status")
    private String status;

    @JsonProperty("success_rate")
    private double successRate;

    @JsonProperty("time_cost")
    private long timeCost;
}
