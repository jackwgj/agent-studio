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
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiuWenEvaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("label")
    private String label;

    @JsonProperty("order")
    private int order;

    @JsonProperty("predict")
    private String predict;

    @JsonProperty("question")
    private String question;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("score")
    private double score;

    @JsonProperty("variable")
    private Map<String, String> variable;
}
