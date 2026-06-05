/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.dto.PeEvalResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 单个评估结果
 *
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeOptimizationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 结果id
     */
    private String id;

    /**
     * 任务id
     */
    private String optimizationTaskId;

    /**
     * 测试用例行号
     */
    private Integer testNum;

    /**
     * 候选模板id
     */
    private String promptId;

    @JsonProperty("progress_rate")
    private float progressRate;

    @JsonProperty("best_prompt")
    private String bestPrompt;

    @JsonProperty("error_msg")
    private String errorMsg;

    private String status;

    /**
     * 结果
     */
    private String generateResult;

    /**
     * 结果
     */
    private PeEvalResult evalResult;

}
