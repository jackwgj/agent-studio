/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity.md;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JSONType(naming = PropertyNamingStrategy.SnakeCase)
public class ModelServiceBase {
    private String id;

    @JsonProperty("provider_id")
    private String providerId;

    private String serviceName;

    private String serviceKey;

    private String modelName;

    private String modelVersion;

    private String modelType;

    private String modelTags;

    private String modelDescription;

    private String modelDeployType;

    /**
     * DOCUMENT_URL
     */
    private String documentUrl;

    private Float modelSize;

    private Integer contextLength;

    private int modelPriority;

    private String domainId;

    private String projectId;

    private String workspaceId;

    private String createdByUser;

    private String lastUpdatedByUser;

    private long createdDate;

    private long lastUpdatedDate;

    private String apiUrl;

    private Boolean isReasoning;

    private Boolean isSupportFunction;

    private String authMetadataId;

    private String identityId;

    private String publishStatus;

    private String interfaceProtocol;

    private Boolean isSupportStream;

    private Integer throttlingPolicy;

    private boolean isPublic;

    private String authId;

    private String authType;
}
