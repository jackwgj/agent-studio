/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import lombok.Data;

@Data
public class WorkflowIrMetadata {
    private String creatorId;

    private String projectId;

    private String domainId;

    private long updatedAt;

    private long publishedAt;

    private String visibility;

    private String status;

    private String workspaceId;

    private String type;
}
