/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.entity;

import com.openjiuwen.studio.agent.foundation.base.constants.DeploymentModeEnum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AgentBaseDeploymentModeSet {

    /**
     * 所有部署模式
     */
    public static final Set<DeploymentModeEnum> ALL_DEPLOYMENT_MODE = Arrays.stream(DeploymentModeEnum.values())
        .collect(Collectors.toSet());

    public static final Set<DeploymentModeEnum> HC_DEPLOYMENT_MODE = Set.of(DeploymentModeEnum.HC);

    public static final Set<DeploymentModeEnum> HCS_DEPLOYMENT_MODE = Set.of(DeploymentModeEnum.HCS);

    public static final Set<DeploymentModeEnum> LITE_DEPLOYMENT_MODE = Set.of(DeploymentModeEnum.SIMPLE);
}
