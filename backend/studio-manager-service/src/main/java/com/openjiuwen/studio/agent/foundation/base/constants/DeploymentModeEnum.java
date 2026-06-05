/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.constants;

import lombok.Getter;

/**
 * 部署模式
 *
 * @since 2025-09-23
 */
public enum DeploymentModeEnum {

    HC("hc"),
    HCS("hcs"),
    // 软件包环境配置simple
    SIMPLE("simple");

    @Getter
    private final String value;

    DeploymentModeEnum(String value) {
        this.value = value;
    }

    public static boolean isHcs(String deployMode) {
        return HCS.getValue().equalsIgnoreCase(deployMode);
    }

    public static boolean isNotHc(String deployMode) {
        return !HC.getValue().equalsIgnoreCase(deployMode);
    }

    public static boolean isHc(String deployMode) {
        return HC.getValue().equalsIgnoreCase(deployMode);
    }
}
