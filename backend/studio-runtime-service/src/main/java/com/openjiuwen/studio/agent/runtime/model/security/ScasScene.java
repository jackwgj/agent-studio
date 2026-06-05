/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;

/**
 * ScasScene
 *
 */
@Data
public class ScasScene {
    private String type;

    private String identityType;

    private String sceneId;

    private String authenticationPath;

    private String topic;
}
