/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import lombok.Data;

/**
 * HIS_IAM鉴权
 *
 */
@Data
public class AuthSgovConfig {
    private String url;

    private String scope;

    private String appId;

    private String credential;
}
