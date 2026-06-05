/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import lombok.Data;

/**
 * @description:
 **/
@Data
public class TraceBaseInfo {
    private String domainId;

    private String projectId;

    private String workspaceId;

    private String rootSpanId;
}
