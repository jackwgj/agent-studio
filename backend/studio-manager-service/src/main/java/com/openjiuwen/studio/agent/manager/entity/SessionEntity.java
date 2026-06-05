/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.Date;

/**
 * workflow session实体
 *
 */
@Data
public class SessionEntity {
    @JsonProperty("message_id")
    private String sessionId;

    @JsonProperty("instance_id")
    private String instanceId;

    @JsonProperty("workflow_id")
    private String workflowId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("created_on")
    private Date createdOn;

    @JsonProperty("updated_on")
    private Date updatedOn;
}
