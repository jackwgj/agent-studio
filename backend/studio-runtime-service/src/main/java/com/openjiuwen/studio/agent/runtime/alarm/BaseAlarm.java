/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.alarm;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

/**
 * 告警信息
 *
 */
@Data
@EqualsAndHashCode
public class BaseAlarm {
    private String projectId;

    private String alarmId;

    private String cause;

    private Map<String, String> locationInfo;

    private Map<String, String> additionalInfo;

    public BaseAlarm() {
        locationInfo = new HashMap<>();
        additionalInfo = new HashMap<>();
        additionalInfo.put("microservice", "agent-builder-agent-runtime");
    }
}
