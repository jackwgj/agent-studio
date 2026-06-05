/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间日期工具
 *
 */
public class DatetimeUtils {
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DATETIME_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS000XXX";

    public static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone("UTC");

    public static final long SECONDS_PER_MINUTE = 60L;

    public static final long MILLIS_PER_SECOND = 1000L;

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * 日期格式化
     *
     * @param date 日期
     * @param format 日期格式
     * @return 格式化后的日期
     */
    public static String dateFormat(Date date, String format, TimeZone timeZone) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        formatter.setTimeZone(timeZone);
        return formatter.format(date);
    }

    public static TimeZone getTimeZone(String zoneId) {
        return TimeZone.getTimeZone(ZoneId.of(zoneId));
    }

    /**
     * 时间戳格式化
     *
     * @param timestamp 时间戳
     * @param format 格式化
     * @param timeZone 时区
     * @return 数据格式
     */
    public static String dateFormat(Long timestamp, String format, TimeZone timeZone) {
        Date date = new Date(timestamp);
        return dateFormat(date, format, timeZone);
    }

    /**
     * getDateTimeMills
     *
     * @param iso8601Time iso8601Time
     * @return long
     */
    public static long getDateTimeMills(String iso8601Time) {
        if (StringUtils.isBlank(iso8601Time)) {
            return System.currentTimeMillis();
        }

        try {
            return ZonedDateTime.parse(iso8601Time).toInstant().toEpochMilli();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    /**
     * 字符型时间转日期格式
     *
     * @param time 时间
     * @return
     */
    public static Date dateParse(String time) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
        try {
            return formatter.parse(time);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static String dateFormat(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(StringUtils.isEmpty(format) ? DATE_FORMAT : format);
        return formatter.format(date);
    }
}
