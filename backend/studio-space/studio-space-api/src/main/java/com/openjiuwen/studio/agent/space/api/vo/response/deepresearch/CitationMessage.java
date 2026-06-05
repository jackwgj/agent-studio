package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CitationMessage {
    /**
     * 名称
     */
    private String name;

    /**
     * URL
     */
    private String url;

    /**
     * 标题
     */
    private String title;

    /**
     * 块内容
     */
    private String chunk;

    /**
     * 来源
     */
    private String source;

    /**
     * 发布时间
     */
    @JsonProperty("publish_time")
    private String publishTime;

    /**
     * 分数
     */
    private Double score;

    /**
     * 来源信息
     */
    private String from;

    /**
     * ID
     */
    private Integer id;
}
