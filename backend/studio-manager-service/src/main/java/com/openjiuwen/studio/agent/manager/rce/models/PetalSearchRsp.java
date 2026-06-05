/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * petal search搜索响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PetalSearchRsp {
    private Integer total;

    @JsonProperty("web_pages")
    private List<WebPage> webPages;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WebPage {
        private String title;

        private String url;

        private String content;

        private String desc;

        @JsonProperty("authority_score")
        private float authorityScore;

        @JsonProperty("relevance_level")
        private int relevanceLevel;

        @JsonProperty("timeliness_score")
        private float timelinessScore;

        @JsonProperty("site_category")
        private List<String> siteCategory;

        @JsonProperty("publish_time")
        private String publishTime;
    }
}
