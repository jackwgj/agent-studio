/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import lombok.Getter;

@Getter
public enum LicenseReportOperationType {
    ADD("add"),

    DELETE("delete");

    private final String value;

    LicenseReportOperationType(String value) {
        this.value = value;
    }

}
