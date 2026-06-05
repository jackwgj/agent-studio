/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.entity;

import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
public class AuthEntity {
    private IdentityEntity identity;

    private ScopeEntity scope;
}
