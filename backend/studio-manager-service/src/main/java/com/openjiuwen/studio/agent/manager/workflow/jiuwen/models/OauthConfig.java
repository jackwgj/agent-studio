/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import lombok.Data;

@Data
public class OauthConfig {
    private String endpointUrl;

    private String grantType;

    private String clientId;

    private String clientSecret;

    private String oauthScope;

    private String scope;
}
