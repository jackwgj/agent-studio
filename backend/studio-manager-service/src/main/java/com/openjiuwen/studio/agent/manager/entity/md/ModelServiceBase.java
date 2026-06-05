/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity.md;

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
public class ModelServiceBase {
    private String id;

    private String providerId;

    private String serviceName;

    private String serviceKey;

    private String modelName;

    private String modelVersion;

    private String modelType;

    private String modelTags;

    private String modelDescription;

    private String modelDescriptionEn;

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

    private Boolean isSupportCloseReasoning;

    private Boolean isNetwork;

    private Boolean isSupportFunction;

    private String authMetadataId;

    private String identityId;

    private String publishStatus;

    private String interfaceProtocol;

    private Boolean isSupportStream;

    private String systemPrompt;

    private Integer throttlingPolicy;

    private String logo;

    private String status;

    private boolean isPublic;

    private String syncStatus;

    private String disclaimer;

    private String disclaimerEn;
}

