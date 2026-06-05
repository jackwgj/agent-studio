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
public class ProviderAuthMetadata {
    private String id;

    private String providerId;

    private String authType;

    private String authInfo;

    private String authUrl;

    private String createdByUser;

    private long createdDate;

    private long lastUpdatedDate;

    private String domainId;

    private String projectId;

    private String workspaceId;

    private String identityId;

    /**
     * 认证配置
     * 非 t_provider_auth_metadata 表的字段
     */

    private String authConfig;

    /**
     * 认证配置ID
     * 非 t_provider_auth_metadata 表的字段
     */
    private String authId;
}
