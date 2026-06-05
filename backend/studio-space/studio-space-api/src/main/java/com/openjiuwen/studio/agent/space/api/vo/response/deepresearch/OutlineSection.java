package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OutlineSection {
    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否为核心章节
     */
    @JsonProperty("is_core_section")
    private Boolean isCoreSection;
}
