/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AlarmLogUtilTest {
    @Test
    public void alarmTest() {
        AlarmLogUtil util = new AlarmLogUtil();
        util.logAlarm("test", "mm", "dd");
        ReflectionTestUtils.setField(util, "enabled", true);
        util.logAlarm("test", "mm", "dd");
    }
}
