/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto.plugin;

import lombok.Data;

@Data
public class OauthInfo {

    private String endpointUrl;

    private String grantType;

    private String clientId;

    private String clientSecret;

    private String scope;
}
