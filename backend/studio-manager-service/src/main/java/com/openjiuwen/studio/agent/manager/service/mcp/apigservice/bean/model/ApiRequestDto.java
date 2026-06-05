/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiRequestDto {
    private String fcName;

    private String fcInstanceId;

    private String reqUrl;

    private String apigGroupId;

    private String apigInstanceId;
}
