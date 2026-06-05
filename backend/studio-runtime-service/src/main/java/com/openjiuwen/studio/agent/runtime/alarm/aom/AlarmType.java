/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.alarm.aom;

import lombok.Getter;

/**
 * 告警类型
 *
 */
@Getter
public enum AlarmType {
    /**
     * ModelArtsStudio 模型服务
     */
    MAAS("1", "MaaS", "MaaS模型服务异常##MaaS Model server");

    private final String resourceId;

    private final String resourceType;

    private final String alarmName;

    AlarmType(String resourceId, String resourceType, String alarmName) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.alarmName = alarmName;
    }
}
