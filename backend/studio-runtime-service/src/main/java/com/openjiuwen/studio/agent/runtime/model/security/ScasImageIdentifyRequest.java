/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.security;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
@Data
@Accessors(chain = true)
public class ScasImageIdentifyRequest {

    private String taskID;

    private List<ScasImageInfo> imageURLs = new ArrayList<>();

    private String businessID;

    private String sceneID;

    private String loginType;

    private String reqTime;

    private String uid = "-1";
}
