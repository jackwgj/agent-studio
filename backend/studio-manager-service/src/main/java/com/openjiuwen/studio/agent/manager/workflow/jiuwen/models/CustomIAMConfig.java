/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
public class CustomIAMConfig {
    private String scope;

    private String iamProject;

    private String iamPassword;

    private String iamDomain;

    private String iamUrl;

    private String iamUser;

    private String iamAK;

    private String iamSK;
}
