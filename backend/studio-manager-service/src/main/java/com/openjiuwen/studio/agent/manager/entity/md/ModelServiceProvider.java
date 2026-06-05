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
public class ModelServiceProvider {
    private String id;

    private String providerName;

    private String providerNameEn;

    private String description;

    private String descriptionEn;

    private String tags;

    private String providerUrl;

    private Long createdDate;

    private Long lastUpdatedDate;

    private String logo;

    private String status;

    private String domainId;

    private String projectId;
    
    private String workspaceId;

    private String createdByUser;

    private String lastUpdatedByUser;

    private String identityId;

    private int priority;
}
