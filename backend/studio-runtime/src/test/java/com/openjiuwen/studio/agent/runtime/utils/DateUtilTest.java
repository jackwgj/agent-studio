/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.openjiuwen.studio.agent.common.utils.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;

/**
 * 功能描述
 *
 */
public class DateUtilTest {
    @Test
    public void testGetStartDateTime_BothNotNull() throws ParseException {
        String startDateTime = "2024-01-01 00:00:00";
        String endDateTime = "2024-01-02 00:00:00";
        Date expected = DateUtils.parseDate(startDateTime, DateUtil.DATE_FORMAT);
        Date result = DateUtil.getStartDateTime(startDateTime, endDateTime);
        assertEquals(expected, result);
    }

    @Test
    public void testGetStartDateTime_InvalidDateFormat() {
        String startDateTime = "invalid-date";
        String endDateTime = "2024-01-01 00:00:00";
        assertThrows(ParseException.class, () -> DateUtil.getStartDateTime(startDateTime, endDateTime));
    }

    @Test
    public void testGetEndDateTime_BothNotNull() throws ParseException {
        String startDateTime = "2024-01-01 00:00:00";
        String endDateTime = "2024-01-02 00:00:00";
        Date expected = DateUtils.parseDate(endDateTime, DateUtil.DATE_FORMAT);
        Date result = DateUtil.getEndDateTime(startDateTime, endDateTime);
        assertEquals(expected, result);
    }

    @Test
    public void testGetEndDateTime_InvalidDateFormat() {
        String startDateTime = "2024-01-01 00:00:00";
        String endDateTime = "invalid-date";
        assertThrows(ParseException.class, () -> DateUtil.getEndDateTime(startDateTime, endDateTime));
    }
}
