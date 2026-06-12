/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class EnvironmentVariableEntity {

    /**
     * 主键ID UUID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 环境变量
     */
    @JsonProperty("env_variable")
    private String envVariable;

    /**
     * 环境id
     */
    @JsonProperty("env_id")
    private String envId;

    /**
     * 创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * 创建者
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;

    /**
     * 更新时间
     */
    @JsonProperty("updater_id")
    private String updaterId;

    /**
     * 项目id
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 工作空间
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    public EnvironmentVariableEntity() {
        this.id = UUID.randomUUID().toString();
    }
}

