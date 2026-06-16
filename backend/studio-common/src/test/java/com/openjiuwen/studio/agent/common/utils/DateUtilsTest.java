/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class DateUtilsTest {

    @Test
    public void testFormatDate_Timestamp_Null() {
        assertEquals("", DateUtils.formatDate((Timestamp) null));
    }

    @Test
    public void testFormatDate_Timestamp_Valid() {
        Timestamp ts = Timestamp.valueOf("2024-01-15 10:30:45");
        String result = DateUtils.formatDate(ts);
        assertNotNull(result);
        assertTrue(result.contains("2024"));
    }

    @Test
    public void testFormatDate_TimestampWithFormat_Null() {
        assertEquals("", DateUtils.formatDate((Timestamp) null, "yyyy-MM-dd"));
    }

    @Test
    public void testFormatDate_TimestampWithFormat_Valid() {
        Timestamp ts = Timestamp.valueOf("2024-01-15 10:30:45");
        String result = DateUtils.formatDate(ts, "yyyy-MM-dd");
        assertEquals("2024-01-15", result);
    }

    @Test
    public void testFormatDate_OffsetDateTime_Null() {
        assertEquals("", DateUtils.formatDate((OffsetDateTime) null));
    }

    @Test
    public void testFormatDate_OffsetDateTime_Valid() {
        OffsetDateTime odt = OffsetDateTime.of(2024, 1, 15, 10, 30, 45, 0, ZoneOffset.UTC);
        String result = DateUtils.formatDate(odt);
        assertNotNull(result);
        assertTrue(result.contains("2024"));
    }

    @Test
    public void testGetNow_NotNull() {
        Timestamp now = DateUtils.getNow();
        assertNotNull(now);
    }

    @Test
    public void testGetNow_CloseToCurrentTime() {
        long before = System.currentTimeMillis();
        Timestamp now = DateUtils.getNow();
        long after = System.currentTimeMillis();
        assertTrue(now.getTime() >= before);
        assertTrue(now.getTime() <= after);
    }

    @Test
    public void testFromBillStrToTimestamp_Valid() {
        Timestamp result = DateUtils.fromBillStrToTimestamp("20240115103045");
        assertNotNull(result);
    }

    @Test
    public void testParse_NullInput() {
        assertNull(DateUtils.parse(null));
    }

    @Test
    public void testParse_EmptyString() {
        assertNull(DateUtils.parse(""));
    }

    @Test
    public void testParse_BlankString() {
        assertNull(DateUtils.parse("   "));
    }

    @Test
    public void testParse_Timestamp() {
        Date result = DateUtils.parse("1705312245000");
        assertNotNull(result);
    }

    @Test
    public void testParse_UtcFormat() {
        Date result = DateUtils.parse("2024-01-15T10:30:45.000Z");
        assertNotNull(result);
    }

    @Test
    public void testParse_DateTimeWithMillis() {
        Date result = DateUtils.parse("2024-01-15 10:30:45.000");
        assertNotNull(result);
    }

    @Test
    public void testParse_DateTimeSeconds() {
        Date result = DateUtils.parse("2024-01-15 10:30:45");
        assertNotNull(result);
    }

    @Test
    public void testParse_DateTimeMinutes() {
        Date result = DateUtils.parse("2024-01-15 10:30");
        assertNotNull(result);
    }

    @Test
    public void testParse_DateOnly() {
        Date result = DateUtils.parse("2024-01-15");
        assertNotNull(result);
    }

    @Test
    public void testParse_SlashFormat() {
        Date result = DateUtils.parse("2024/01/15 10:30:45");
        assertNotNull(result);
    }

    @Test
    public void testParse_NumericFormat() {
        Date result = DateUtils.parse("20240115");
        assertNotNull(result);
    }

    @Test
    public void testParse_InvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> DateUtils.parse("not-a-date"));
    }

    @Test
    public void testDateTimeFormat_Constant() {
        assertEquals("yyyy/MM/dd HH:mm:ss", DateUtils.DATE_TIME_FORMAT);
    }

    @Test
    public void testBillDateTimeFormat_Constant() {
        assertEquals("yyyyMMddHHmmss", DateUtils.BILL_DATE_TIME_FORMAT);
    }
}
