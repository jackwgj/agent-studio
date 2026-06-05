/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Date;

/**
 * 意图包分支
 *
 */
@Data
public class ComplexIntentBranchEntity {
    @JsonProperty("branch_id")
    private String branchId;

    @JsonProperty("intent_id")
    private String intentId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("branch_index")
    private Integer branchIndex;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("content")
    private String content;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("created_on")
    private Date createdOn;

    @JsonProperty("updated_on")
    private Date updatedOn;

    @JsonProperty("faq_ids")
    private String faqIds;
}
