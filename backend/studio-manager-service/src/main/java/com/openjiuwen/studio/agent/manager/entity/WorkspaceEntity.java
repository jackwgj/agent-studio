/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 项目空间实体类
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
public class WorkspaceEntity {
    /**
     * 项目空间ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 项目空间名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 项目空间标识
     */
    @JsonProperty("flag")
    private String flag;

    /**
     * project_id
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 项目空间描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 项目空间图标
     */
    @JsonProperty("icon")
    private String icon;

    /**
     * 租户ID
     */
    @JsonProperty("tenant_id")
    private String tenantId;

    /**
     * 项目空间类型
     */
    @JsonProperty("type")
    private String type;

    /**
     * 项目空间状态
     */
    @JsonProperty("status")
    private String status;

    /**
     * 创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * 创建人
     */
    @JsonProperty("creator")
    private String creator;

    /**
     * 创建人ID
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;

    /**
     * 更新人
     */
    @JsonProperty("updater")
    private String updater;

    /**
     * 更新人ID
     */
    @JsonProperty("updater_id")
    private String updaterId;

    /**
     * 空间角色
     */
    @JsonProperty("role")
    private String role;

    /**
     * 是否已经预制过教学模版
     */
    @JsonProperty("is_preset_agent")
    private int isPresetAgent;
}
