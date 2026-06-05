/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * agent简要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSummaryInfo {
    private String id;

    private String name;

    private String description;

    private String icon;

    @Builder.Default
    private String type = null;

    @JsonProperty("workspace_id")
    @Builder.Default
    private String workspaceId = null;

    @JsonProperty("updated_at")
    @Builder.Default
    private String updatedAt = null;

    @JsonProperty("workspace_name")
    @Builder.Default
    private String workspaceName = null;

    private String status;

    private String prologue;

    private String creator;

    /**
     * 是否已发布到AgentSpace
     */
    @JsonProperty("publish_status")
    private Boolean publishStatus;
}
