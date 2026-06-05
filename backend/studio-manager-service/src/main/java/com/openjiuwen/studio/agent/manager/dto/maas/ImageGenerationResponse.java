/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.maas;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 图像生成响应类
 *
 */
@Data
public class ImageGenerationResponse {
    private String model;

    private long created;

    private List<ImageData> data;

    private Usage usage;

    private String error;

    @Data
    @Builder
    public static class ImageData {
        private String url;

        @JsonProperty("b64_json")
        private String b64Json;
    }

    @Data
    public static class Usage {
        @JsonProperty("model_latency")
        private int modelLatency;

        @JsonProperty("prompt_tokens")
        private int promptTokens;

        @JsonProperty("completion_tokens")
        private int completionTokens;

        @JsonProperty("total_tokens")
        private int totalTokens;
    }
}


