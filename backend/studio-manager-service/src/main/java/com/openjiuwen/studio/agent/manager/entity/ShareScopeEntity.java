/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 保存元素跨空间共享的授权范围
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
public class ShareScopeEntity {
    /**
     * 授权唯一标识，主键。
     */
    @JsonProperty("id")
    private String id;

    /**
     * 被共享的源元素ID。
     */
    @JsonProperty("resource_id")
    private String resourceId;

    /**
     * 被共享的资源类型，如agent、workflow、tool、mcp等。
     */
    @JsonProperty("resource_type")
    private String resourceType;

    /**
     * 被授权的空间ID。
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 当前企业项目ID。
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 租户ID。
     */
    @JsonProperty("tenant_id")
    private String tenantId;
}
