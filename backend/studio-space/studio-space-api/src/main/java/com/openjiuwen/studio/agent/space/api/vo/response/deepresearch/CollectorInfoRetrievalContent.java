package com.openjiuwen.studio.agent.space.api.vo.response.deepresearch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CollectorInfoRetrievalContent {
    /**
     * URL
     */
    private String url;

    /**
     * 标题
     */
    private String title;

    /**
     * 查询条件
     */
    @JsonProperty("query")
    private String query;
}
