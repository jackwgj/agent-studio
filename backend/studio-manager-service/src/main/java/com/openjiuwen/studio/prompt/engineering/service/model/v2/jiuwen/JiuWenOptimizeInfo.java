/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service.model.v2.jiuwen;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.prompt.engineering.dto.ModelCase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JiuWenOptimizeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 迭代次数
     */
    @JsonProperty("num_iter")
    private Integer numIter;

    /**
     * 测试用例列表
     */
    @JsonProperty("cases")
    private List<ModelCase> cases;

    /**
     * 示例数量
     */
    @JsonProperty("example_num")
    private Integer exampleNum;

    /**
     * 达到目标提前结束
     */
    @JsonProperty("early_stop_score")
    private double earlyStopScore;

    /**
     * LLM 并行数
     */
    @JsonProperty("llm_parallel")
    private Integer llmParallel;

    /**
     * CoT（思维链）示例数量
     */
    @JsonProperty("cot_example_num")
    private Integer cotExampleNum;

    /**
     * 优化方法（如 JOINT）
     */
    @JsonProperty("optimize_method")
    private String optimizeMethod;

    /**
     * 评估方法（如 LLM）
     */
    @JsonProperty("evaluation_method")
    private String evaluationMethod;

    /**
     * 任务类型（主观subject、客观object）
     */
    @JsonProperty("user_compare_options")
    private String userCompareOptions;

    /**
     * 评分标准
     */
    @JsonProperty("user_compare_rules")
    private String userCompareRules;

    /**
     * 背景知识
     */
    @JsonProperty("external_knowledge")
    private String externalKnowledge;


    /**
     * 占位符列表
     */
    @JsonProperty("placeholder")
    private List<PlaceholderItem> placeholder;

    /**
     * 优化算法
     */
    @JsonProperty("optimize_algorithm")
    private String optimizeAlgorithm;

}
