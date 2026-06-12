/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class EnvironmentManagerEntity {

    /**
     * 主键ID UUID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 环境名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 是否默认
     */
    @JsonProperty("is_default")
    private Boolean isDefault;

    /**
     * 资源对象
     */
    @JsonProperty("status")
    private String status;

    /**
     * resources
     */
    @JsonProperty("resources")
    private String resources;

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
     * 更新用户id
     */
    @JsonProperty("updater_id")
    private String updaterId;

    /**
     * 账号id
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * 项目id
     */
    @JsonProperty("project_id")
    private String projectId;
}

