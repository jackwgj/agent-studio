/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@MockitoSettings(strictness = Strictness.LENIENT)
class DatetimeUtilsTest {

    @Test
    void testDateFormat_WithDateAndFormatAndTimeZone() {
        Date date = new Date(1700000000000L);
        TimeZone utc = TimeZone.getTimeZone("UTC");
        String result = DatetimeUtils.dateFormat(date, DatetimeUtils.DATETIME_FORMAT, utc);
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testDateFormat_WithTimestampAndFormatAndTimeZone() {
        long timestamp = 1700000000000L;
        TimeZone utc = TimeZone.getTimeZone("UTC");
        String result = DatetimeUtils.dateFormat(timestamp, DatetimeUtils.DATETIME_FORMAT, utc);
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testGetTimeZone() {
        TimeZone tz = DatetimeUtils.getTimeZone("Asia/Shanghai");
        assertNotNull(tz);
        assertEquals("Asia/Shanghai", tz.getID());
    }

    @Test
    void testGetTimeZone_WithUtc() {
        TimeZone tz = DatetimeUtils.getTimeZone("UTC");
        assertNotNull(tz);
        assertEquals("UTC", tz.getID());
    }

    @Test
    void testGetDateTimeMills_WithValidIso8601() {
        String isoTime = "2024-01-01T00:00:00.000Z";
        long millis = DatetimeUtils.getDateTimeMills(isoTime);
        assertTrue(millis > 0);
    }

    @Test
    void testGetDateTimeMills_WithBlankString() {
        long millis = DatetimeUtils.getDateTimeMills("");
        assertTrue(millis > 0);
    }

    @Test
    void testGetDateTimeMills_WithNull() {
        long millis = DatetimeUtils.getDateTimeMills(null);
        assertTrue(millis > 0);
    }

    @Test
    void testGetDateTimeMills_WithInvalidFormat() {
        long millis = DatetimeUtils.getDateTimeMills("not-a-date");
        assertTrue(millis > 0);
    }

    @Test
    void testDateParse_WithValidTime() {
        Date result = DatetimeUtils.dateParse("2024-01-15 10:30:00");
        assertNotNull(result);
    }

    @Test
    void testDateParse_WithEmptyString() {
        Date result = DatetimeUtils.dateParse("");
        assertNull(result);
    }

    @Test
    void testDateParse_WithNull() {
        Date result = DatetimeUtils.dateParse(null);
        assertNull(result);
    }

    @Test
    void testDateParse_WithInvalidFormat() {
        Date result = DatetimeUtils.dateParse("invalid-date");
        assertNull(result);
    }

    @Test
    void testDateFormat_WithDateAndFormat() {
        Date date = new Date();
        String result = DatetimeUtils.dateFormat(date, DatetimeUtils.DATE_FORMAT);
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testDateFormat_WithDateAndNullFormat() {
        Date date = new Date();
        String result = DatetimeUtils.dateFormat(date, null);
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testDateFormat_WithDateAndEmptyFormat() {
        Date date = new Date();
        String result = DatetimeUtils.dateFormat(date, "");
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testDateFormat_WithCustomFormat() {
        Date date = new Date(1700000000000L);
        String result = DatetimeUtils.dateFormat(date, DatetimeUtils.DATE_FORMAT_YYYYMMDDHHMMSS);
        assertNotNull(result);
        assertTrue(result.matches("\\d{14}"));
    }

    @Test
    void testConstants() {
        assertEquals("yyyy-MM-dd HH:mm:ss", DatetimeUtils.DATETIME_FORMAT);
        assertEquals("yyyy-MM-dd", DatetimeUtils.DATE_FORMAT);
        assertEquals("yyyyMMddHHmmss", DatetimeUtils.DATE_FORMAT_YYYYMMDDHHMMSS);
        assertEquals(60L, DatetimeUtils.SECONDS_PER_MINUTE);
        assertEquals(1000L, DatetimeUtils.MILLIS_PER_SECOND);
    }
}
