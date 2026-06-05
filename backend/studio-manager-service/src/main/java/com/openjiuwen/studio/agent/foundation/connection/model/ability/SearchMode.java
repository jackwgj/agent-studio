/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.foundation.connection.model.ability;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SearchMode {
    @JsonProperty("search_mode")
    private String searchMode;

    @JsonProperty("search_mode_name")
    private String searchModeName;

    @JsonProperty("search_mode_name_en")
    private String searchModeNameEn;

    private String summary;

    @JsonProperty("summary_en")
    private String summaryEn;

    private String description;

    @JsonProperty("description_en")
    private String descriptionEn;
}