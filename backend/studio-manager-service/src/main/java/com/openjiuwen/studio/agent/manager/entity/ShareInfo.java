/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 添加类的描述
 *
 */
@Data
public class ShareInfo {

    /**
     * 被共享的源元素ID，主键。
     */
    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("resource_name")
    private String resourceName;

    /**
     * 被共享的资源类型，如agent、workflow、tool、mcp等。
     */
    @JsonProperty("resource_type")
    private String resourceType;

    /**
     * 源元素归属的空间ID。
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("workspace_name")
    private String workspaceName;

    /**
     * 被共享的源元素的溯源ID。
     */
    @JsonProperty("trace_id")
    private String traceId;

    /**
     * 被共享的版本号，按最近顺序排序，多个版本号之间逗号隔开。
     */
    @JsonProperty("version_list")
    private String versionList;

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

    /**
     * 创建用户ID。
     */
    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("creator")
    private String creator;

    /**
     * 创建时间，默认为当前时间戳。
     */
    @JsonProperty("create_time")
    private Date createTime;

    /**
     * 更新用户ID。
     */
    @JsonProperty("updater_id")
    private String updaterId;

    @JsonProperty("updater")
    private String updater;

    /**
     * 更新时间，默认为当前时间戳。
     */
    @JsonProperty("update_time")
    private Date updateTime;

    /**
     * 共享范围，workspace列表，以逗号分隔
     */
    private List<ShareScopeEntity> shareScopeEntityList;
}
