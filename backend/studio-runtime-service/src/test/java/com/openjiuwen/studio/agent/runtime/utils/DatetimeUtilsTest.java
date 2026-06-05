/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DatetimeUtilsTest {
    @Test
    public void testCalculateCeilingDaysFromMinutes_ZeroMinutes() {
        int result = DatetimeUtils.calculateCeilingDaysFromMinutes(0);
        assertEquals(0, result);
    }

    @Test
    public void testCalculateCeilingDaysFromMinutes_NegativeMinutes() {
        int result = DatetimeUtils.calculateCeilingDaysFromMinutes(-100);
        assertEquals(0, result);
    }

    @Test
    public void testCalculateCeilingDaysFromMinutes_SmallMinutes() {
        // 5 minutes → 1 day
        int result = DatetimeUtils.calculateCeilingDaysFromMinutes(5);
        assertEquals(1, result);
    }

    @Test
    public void testCalculateCeilingDaysFromMinutes_ExactlyOneDay() {
        // 1440 minutes → 1 day
        int result = DatetimeUtils.calculateCeilingDaysFromMinutes(1440);
        assertEquals(1, result);
    }

    @Test
    public void testCalculateCeilingDaysFromMinutes_ExactlySevenDays() {
        // 10080 minutes → 7 days
        int result = DatetimeUtils.calculateCeilingDaysFromMinutes(10080);
        assertEquals(7, result);
    }

    @Test
    public void testCalculateCeilingDaysFromMinutes_JustOverSevenDays() {
        // 10081 minutes → 8 days
        int result = DatetimeUtils.calculateCeilingDaysFromMinutes(10081);
        assertEquals(8, result);
    }

    @Test
    public void testCalculateCeilingDaysFromMinutes_MidDay() {
        // 720 minutes (12 hours) → 1 day
        int result = DatetimeUtils.calculateCeilingDaysFromMinutes(720);
        assertEquals(1, result);
    }
}
