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
public class PromptEvalutionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 顺序
     */
    @JsonProperty("order")
    private int order;

    /**
     * 预期输出
     */
    @JsonProperty("label")
    private String label;

    /**
     * 评测输出
     */
    @JsonProperty("predict")
    private String predict;

    /**
     * 统一为rowTemple，目前没什么作用
     */
    @JsonProperty("question")
    private String question;

    /**
     * 评分原因
     */
    @JsonProperty("reason")
    private String reason;

    /**
     * 得分
     */
    @JsonProperty("score")
    private double score;

    /**
     * 输入的评估数据
     */
    @JsonProperty("variable")
    private String variable;
}
