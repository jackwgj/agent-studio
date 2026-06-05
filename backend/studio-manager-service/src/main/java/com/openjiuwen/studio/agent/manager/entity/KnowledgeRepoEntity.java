/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.ExtraParamsInfo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 功能描述
 *
 */
@Data
public class KnowledgeRepoEntity {
    @JsonProperty("knowledge_repo_id")
    private String knowledgeRepoId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("display_name")
    private String displayName;

    private String desc;

    private String type;

    private String source;
    
    private String filterString;
    
    private List<String> tag;

    private List<ExtraParamsInfo> extraParams;

    private String icon;

    @JsonProperty("icon_name")
    private String iconName;

    private Long size;

    private String creator;

    @JsonProperty("creator_id")
    private String creatorId;

    private String status;

    private String metadata;

    @JsonProperty("created_on")
    private Date createdOn;

    @JsonProperty("updated_on")
    private Date updatedOn;
}
