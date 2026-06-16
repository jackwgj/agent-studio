/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilTest {

    @Test
    void testGetStartDateTime_BothValid() throws ParseException {
        Date result = DateUtil.getStartDateTime("2025-01-15 10:00:00", "2025-01-16 10:00:00");
        assertNotNull(result);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(2025, cal.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testGetStartDateTime_StartNull() {
        assertThrows(NullPointerException.class, () -> DateUtil.getStartDateTime(null, "2025-01-16 10:00:00"));
    }

    @Test
    void testGetStartDateTime_InvalidFormat() {
        assertThrows(ParseException.class, () -> DateUtil.getStartDateTime("invalid", "2025-01-16 10:00:00"));
    }

    @Test
    void testGetEndDateTime_BothValid() throws ParseException {
        Date result = DateUtil.getEndDateTime("2025-01-15 10:00:00", "2025-01-16 10:00:00");
        assertNotNull(result);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testGetEndDateTime_EndNull() {
        assertThrows(NullPointerException.class, () -> DateUtil.getEndDateTime("2025-01-15 10:00:00", null));
    }

    @Test
    void testGetEndDateTime_InvalidFormat() {
        assertThrows(ParseException.class, () -> DateUtil.getEndDateTime("2025-01-15 10:00:00", "bad"));
    }

    @Test
    void testDateFormatConstant() {
        assertEquals("yyyy-MM-dd HH:mm:ss", DateUtil.DATE_FORMAT);
    }
}
