/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceParam {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("description")
    private String description;

    @JsonProperty("required")
    private boolean required;

    @JsonProperty("visibility")
    private boolean visibility;

    @JsonProperty("default")
    private Object defaultValue;

    @JsonProperty("schema")
    private Object schema;

    @JsonProperty("value")
    private ValueDTO value;

    @Builder
    @Data
    public static class ValueDTO {
        @JsonProperty("type")
        private String type;

        @JsonProperty("content")
        private Object content;
    }
}
