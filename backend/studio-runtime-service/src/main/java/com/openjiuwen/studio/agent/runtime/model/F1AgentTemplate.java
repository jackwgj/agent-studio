/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class F1AgentTemplate {
    private String templateName;

    private String matchName;

    private String type;

    private String importAgentsId;

    private String importWorkflowsId;

    private String importToolsId;

    private String appType;

    private String appName;

    private String appDesc;

    private String appIcon;

    private String appId;

}
