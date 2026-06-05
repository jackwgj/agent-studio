/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.plugin;

import com.openjiuwen.studio.agent.runtime.dto.md.CustomIAMAuthInfo;
import com.openjiuwen.studio.agent.runtime.dto.md.HisIAMAuthInfo;
import com.openjiuwen.studio.agent.runtime.dto.md.SgovAuthInfo;

import io.swagger.v3.oas.models.security.SecurityScheme;

public interface PluginTransform {
    HisIAMAuthInfo transformAuthInfo2HisIAMAuthInfo(SecurityScheme securityScheme);

    SgovAuthInfo transformAuthInfo2SgovAuthInfo(SecurityScheme securityScheme);

    CustomIAMAuthInfo transformAuthInfo2CustomIAMAuthInfo(SecurityScheme securityScheme);
}
