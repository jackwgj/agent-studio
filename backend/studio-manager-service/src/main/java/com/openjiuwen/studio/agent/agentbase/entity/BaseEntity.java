/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity extends UpdateBaseInfo {

    @JsonProperty("project_id")
    protected String projectId;

    @JsonProperty("domain_id")
    protected String domainId;

    @JsonProperty("domain_name")
    protected String domainName;

    @JsonProperty("created_user_id")
    protected String createdUserId;

    @JsonProperty("created_user_name")
    protected String createdUserName;

    @JsonProperty("create_time")
    protected long createTime;

    @JsonProperty("workspace_id")
    protected String workspaceId;

}
