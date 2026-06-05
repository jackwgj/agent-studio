/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import lombok.Getter;

/**
 * KooSearch对接鉴权方式枚举
 */
public enum KooSearchAuthMode {
    /**
     * 无需鉴权
     */
    NONE("none"),

    APP_CODE("app_code"),

    /**
     * 承载租户的IAM Token
     */
    TOKEN("token");

    @Getter
    private final String authMode;

    KooSearchAuthMode(String authMode) {
        this.authMode = authMode;
    }
}
