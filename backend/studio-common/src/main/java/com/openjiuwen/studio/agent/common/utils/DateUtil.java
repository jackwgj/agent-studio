/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * Public date class
 */

public class DateUtil {

    /**
     * date format
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Get the start time in the query condition, when the start time is empty,
     * the default end time is one day before
     *
     * @param startDateTime startDateTime
     * @param endDateTime endDateTime
     * @return Date
     * @throws ParseException parse faild
     */
    public static Date getStartDateTime(String startDateTime, String endDateTime) throws ParseException {
        Date startTime = DateUtils.parseDate(startDateTime, DATE_FORMAT);
        Date endTime = DateUtils.parseDate(endDateTime, DATE_FORMAT);
        if (startTime == null && endTime != null) {
            startTime = DateUtils.addDays(endTime, -1);
        }
        return startTime;
    }

    /**
     * Get the start time in the query condition,
     * when the start time is empty, the default end time is one day before
     *
     * @param startDateTime startDateTime
     * @param endDateTime endDateTime
     * @return Date
     * @throws ParseException ParseException
     */
    public static Date getEndDateTime(String startDateTime, String endDateTime) throws ParseException {
        Date startTime = DateUtils.parseDate(startDateTime, DATE_FORMAT);
        Date endTime = DateUtils.parseDate(endDateTime, DATE_FORMAT);
        if (startTime != null && endTime == null) {
            endTime = DateUtils.addDays(startTime, 1);
        }
        return endTime;
    }
}