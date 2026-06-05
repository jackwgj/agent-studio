/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 功能描述
 *
 */
@Data
@Accessors(chain = true)
public class ScasTextIdentifyRequest {
    private String taskID;

    private TextInfo message;

    private String businessID;

    private String sceneID;

    private String uid = "-1";

    private String reqTime;
}
