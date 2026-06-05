/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 模型的自定义配置
 *
 * @since 2025-04-14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModelExtendConfig {
    private VectorConfig vectorConfig;

    private Map<String, String> customHeader;

    private String deploymentId;

    private IamInfo iamInfo;

    /**
     * 模型是否开启连通性测试
     */
    private Boolean timeDetectEnabled;

    /**
     * 向量模型的相关配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class VectorConfig {
        private String embeddingUrl;

        private String rerankUrl;
    }

    /**
     * iam相关配置
     */
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class IamInfo {
        private boolean iamEnabled;

        private boolean agencyEnabled;

        private String projectId;

        private String domainName;

        private String agencyName;
    }
}
