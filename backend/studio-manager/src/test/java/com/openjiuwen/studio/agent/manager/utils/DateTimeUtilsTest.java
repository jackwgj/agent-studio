/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.TimeZone;

class DateTimeUtilsTest {
    @Test
    void testDateFormatWithValidInput() {
        // Given
        Long timestamp = 1609459200000L; // 2021-01-01 00:00:00 UTC
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        String expectedFormattedDate = "2021-01-01T00:00:00.000000Z";

        assertEquals(expectedFormattedDate,
            DatetimeUtils.dateFormat(timestamp, DatetimeUtils.DATETIME_FORMAT_ISO8601, timeZone));
    }

    @Test
    void should_get_current_mills_when_get_date_time_mills_with_null() {
        long parsedMills = DatetimeUtils.getDateTimeMills(null);
        assertTrue(System.currentTimeMillis() - parsedMills < 1000);
    }

    @Test
    void should_get_current_mills_when_get_date_time_mills_with_illegal_format_time() {
        long parsedMills = DatetimeUtils.getDateTimeMills("dsasdwe");
        assertTrue(System.currentTimeMillis() - parsedMills < 1000);
    }

    @Test
    void should_get_matched_mills_when_get_date_time_with_iso8601_time() {
        assertEquals(1764228071000L, DatetimeUtils.getDateTimeMills("2025-11-27T15:21:11+08:00"));
    }
}
