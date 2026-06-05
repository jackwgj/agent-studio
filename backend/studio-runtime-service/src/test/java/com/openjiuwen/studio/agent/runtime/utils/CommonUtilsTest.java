/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommonUtilsTest {

    @Test
    public void toTraceModelTest() {
        String defaultMode = "df";
        Assertions.assertEquals(defaultMode, CommonUtils.toTraceModel(defaultMode, null));
        Assertions.assertEquals(defaultMode, CommonUtils.toTraceModel(defaultMode, ""));

        Assertions.assertEquals("DEBUG", CommonUtils.toTraceModel(defaultMode, "debug"));
        Assertions.assertEquals("DEBUG", CommonUtils.toTraceModel(defaultMode, "DEBUG"));

        Assertions.assertEquals("EXPERIMENT", CommonUtils.toTraceModel(defaultMode, "EXPERIMENT"));
        Assertions.assertEquals("EXPERIMENT", CommonUtils.toTraceModel(defaultMode, "EXPERIment"));

        Assertions.assertEquals("API", CommonUtils.toTraceModel(defaultMode, "PROD"));
        Assertions.assertEquals("API", CommonUtils.toTraceModel(defaultMode, "Api"));

        Assertions.assertEquals(defaultMode, CommonUtils.toTraceModel(defaultMode, "xx"));
        Assertions.assertEquals(defaultMode, CommonUtils.toTraceModel(defaultMode, "12"));
    }

    @Test
    public void traceEnableTest() {
        Assertions.assertTrue(CommonUtils.traceEnable(true, "DEBUG"));
        Assertions.assertTrue(CommonUtils.traceEnable(true, "API"));
        Assertions.assertTrue(CommonUtils.traceEnable(true, "EXPERIMENT"));

        Assertions.assertFalse(CommonUtils.traceEnable(true, null));
        Assertions.assertFalse(CommonUtils.traceEnable(true, ""));
        Assertions.assertFalse(CommonUtils.traceEnable(false, "DEBUG"));
        Assertions.assertFalse(CommonUtils.traceEnable(false, "API"));
        Assertions.assertFalse(CommonUtils.traceEnable(false, "EXPERIMENT"));
    }
}
