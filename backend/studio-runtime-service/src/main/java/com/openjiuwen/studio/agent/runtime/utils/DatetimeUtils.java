/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间日期工具
 *
 */
public class DatetimeUtils {
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final long SECONDS_PER_MINUTE = 60L;

    public static final long MILLIS_PER_SECOND = 1000L;

    public static final long MINUTES_PER_HOUR = 60L;

    public static final long HOURS_PER_DAY = 24L;

    public static final long MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY; // 1440

    /**
     * 根据分钟数计算向上取整的天数
     *
     * @param minutes 总分钟数
     * @return 向上取整的天数，如果输入为负数则返回0
     */
    public static int calculateCeilingDaysFromMinutes(long minutes) {
        // 处理无效输入
        if (minutes <= 0) {
            return 0;
        }

        // 使用天花板除法：(minutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY
        return (int) ((minutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY);
    }

    /**
     * 日期格式化
     *
     * @param date 日期
     * @param format 日期格式
     * @return 格式化后的日期
     */
    public static String dateFormat(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * 日期格式化
     *
     * @param date 日期
     * @return
     */
    public static String dateFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
        return formatter.format(date);
    }

    /**
     * toTimestamp
     *
     * @param timeStr timeStr
     * @return long
     */
    public static long toTimestamp(String timeStr) {
        return LocalDateTime.parse(timeStr).atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 时间截取到日期
     *
     * @param date 时间
     * @return
     */
    public static Date dateFormatDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // 将小时、分钟、秒和毫秒部分设置为0
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // 获取日期部分
        return calendar.getTime();
    }

}
