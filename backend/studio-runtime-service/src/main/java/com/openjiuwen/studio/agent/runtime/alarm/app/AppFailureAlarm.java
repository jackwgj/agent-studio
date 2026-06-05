/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.alarm.app;

import com.openjiuwen.studio.agent.runtime.alarm.AlarmIDs;
import com.openjiuwen.studio.agent.runtime.alarm.BaseAlarm;

/**
 * 功能描述
 *
 */
public class AppFailureAlarm extends BaseAlarm {
    AppFailureAlarm() {
        super();
        setCause("Application Run Failed");
        setAlarmId(AlarmIDs.API_CALL_FAILURE);
    }
}
