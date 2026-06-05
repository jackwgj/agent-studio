/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security.moderation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RunTextModerationTextRequestBody {

    @JsonProperty("event_type")
    private String eventType;


    @JsonProperty("glossary_names")
    private List<String> glossaryNames;


    private TextDetectionDataReq data;

    @JsonProperty("white_glossary_names")
    private List<String> whiteGlossaryNames;

    private List<String> categories;

    @JsonProperty("biz_type")
    private String bizType;

    private Extra extra;
}
