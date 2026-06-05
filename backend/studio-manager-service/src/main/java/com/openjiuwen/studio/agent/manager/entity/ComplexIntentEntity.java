/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Date;

/**
 * 意图包实体类
 *
 */
@Data
public class ComplexIntentEntity {
    @JsonProperty("intent_id")
    private String intentId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("branches")
    private String branches;

    @JsonProperty("branches_cnt")
    private Integer branchesCnt;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("created_on")
    private Date createdOn;

    @JsonProperty("updated_on")
    private Date updatedOn;
}
