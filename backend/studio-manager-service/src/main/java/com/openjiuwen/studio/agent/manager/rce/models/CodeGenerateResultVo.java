/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeGenerateResultVo {
    private String id;
    private Long created;
    private String model;
    private List<Choice> choices;

    @JsonProperty("request_id")
    private String requestId;

    private String object;

    private Double firstTokenReturnTime;

    private String serviceTier;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        private Delta delta;
        private int index;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Delta {
        private String role;
        private String content;
    }
}
