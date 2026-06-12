/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 对象管理实体类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomObjectEntity {
    /**
     * 自定义对象id
     */
    @JsonProperty("id")
    private String id;

    /**
     * 项目id
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 工作空间id
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 自定义对象名字
     */
    @JsonProperty("name")
    private String name;

    /**
     * 自定义对象描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 自定义对象格式
     */
    @JsonProperty("object_schema")
    private String objectSchema;

    /**
     * 创建人id
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * 更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;
}
