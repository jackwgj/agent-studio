package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class OutlineContent {
    /**
     * 语言
     */
    private String language;

    /**
     * 思考过程
     */
    private String thought;

    /**
     * 标题
     */
    private String title;

    /**
     * 章节列表
     */
    @JsonProperty("sections")
    private List<OutlineSection> sections;
}
