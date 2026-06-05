/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Public date class
 */
public class DateUtil {
    /**
     * date format
     */
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private static final String DATE_FORMAT2 = "yyyy/MM/dd HH:mm:ss 'GMT'XXX";

    private static final FastDateFormat FORMAT2 = FastDateFormat.getInstance("yyyyMMddHHmmss", Locale.ROOT);

    public static String timeStampFormat(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT2);
        // 设置时区为GMT+8
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 格式化日期时间
        return formatter.format(date);
    }

    public static String format(Date d) {
        return DATE_FORMAT.format(d);
    }

    public static String format(LocalDateTime d) {
        return format(d, DATE_FORMAT);
    }

    public static String format(LocalDateTime d, FastDateFormat format) {
        if (null == d) {
            return null;
        }
        return format.format(Date.from(d.atZone(ZoneId.systemDefault()).toInstant()));
    }

    public static String format(Date d, FastDateFormat format) {
        if (null == d) {
            return null;
        }
        return format.format(d);
    }

    public static String formatCompact(Date d) {
        return format(d, FORMAT2);
    }

    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date day = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(day);
    }

    public static String GMTFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT2);
        // 设置时区为GMT+8
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 格式化日期时间
        return formatter.format(date);
    }

    public static long getDayDiff(Date startDate, Date endDate) {
        long day = 1000L * 24 * 60 * 60;
        long diff = endDate.getTime() - startDate.getTime();
        long dayDiff = diff / day;
        return dayDiff;
    }
}
