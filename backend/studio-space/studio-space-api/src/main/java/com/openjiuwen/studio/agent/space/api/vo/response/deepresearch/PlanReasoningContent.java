package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class PlanReasoningContent {
    /**
     * 语言
     */
    private String language;

    /**
     * 标题
     */
    private String title;

    /**
     * 思考过程
     */
    private String thought;

    /**
     * 是否已完成研究
     */
    @JsonProperty("is_research_completed")
    private Boolean isResearchCompleted;

    /**
     * 步骤列表
     */
    @JsonProperty("steps")
    private List<PlanStep> steps;
}
