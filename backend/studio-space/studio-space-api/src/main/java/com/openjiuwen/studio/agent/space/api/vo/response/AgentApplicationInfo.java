/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 智能体应用信息
 */
@Data
public class AgentApplicationInfo {
    private String id;

    private String name = null;

    private String icon = null;

    private String description = null;

    private String type = null;

    @JsonProperty("workspace_id")
    private String workspaceId = null;

    @JsonProperty("updated_at")
    private String updatedAt = null;

    @JsonProperty("workspace_name")
    private String workspaceName = null;

    private String prologue;

    private String creator;
}
