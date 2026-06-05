/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.entity;

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
public class PeEvaluationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评估结果id
     */
    private String id;

    /**
     * 评估任务id
     */
    private String evaluationTaskId;

    /**
     * 测试用例行号
     */
    private Integer testNum;

    /**
     * 候选模板id
     */
    private String promptId;

    /**
     * 结果
     */
    private String generateResult;

    /**
     * 评估结果
     */
    private PeEvalResult evalResult;

    /**
     * 评估失败原因
     */
    private String evalFailedReason;

    private String workspaceId;
}
