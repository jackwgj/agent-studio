/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    /**
     * 全产品默认日期格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static final String BILL_DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    // 日期格式
    private static final String[] PATTERNS = {
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",        // UTC
        "yyyy-MM-dd HH:mm:ss.SSS",             // MILL
        "yyyy-MM-dd HH:mm:ss",                 // SEC
        "yyyy-MM-dd HH:mm",                    // MIN
        "yyyy-MM-dd",                          // DATE
        "yyyy/MM/dd HH:mm:ss.SSS",             // MILL_SLASH
        "yyyy/MM/dd HH:mm:ss",                 // SEC_SLASH
        "yyyyMMddHHmmss",                      // SEC2NUM
        "yyyyMMdd",                            // DATE2NUM
        "yyyy/MM/dd HH:mm",                    // MIN_SLASH
        "yyyy/MM/dd",                          // DATE_SLASH
        "yyyy年MM月dd日 HH时mm分ss秒",         // SEC_ZH
        "yyyy年MM月dd日 HH时mm分",             // MIN_ZH
        "yyyy年MM月dd日",                      // DATE_ZH
        "MMM dd, yyyy HH:mm:ss a",             // SEC_MON_ZH
        "MMM d, yyyy",                         // DATE_MON_ZH
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",          // ENGLISH
        "EEE MMMM dd HH:mm:ss Z yyyy"          // SEC_WEEK_ZH
    };


    public static String formatDate(Timestamp time) {
        if (time == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
        return df.format(time);
    }

    public static String formatDate(Timestamp time, String dateformat) {
        if (time == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        return df.format(time);
    }

    public static String formatDate(OffsetDateTime time) {
        if (time == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        return time.format(formatter);
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static Timestamp getNow() {
        return new Timestamp((new Date()).getTime());
    }

    /**
     * 将string按照UTC时区转化为Timestamp
     */
    public static Timestamp fromBillStrToTimestamp(String billTimeStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(BILL_DATE_TIME_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        ParsePosition parsePosition = new ParsePosition(0);
        Date date = simpleDateFormat.parse(billTimeStr, parsePosition);

        return new Timestamp(date.getTime());
    }

    /**
     * 自动解析日期字符串（替换 Dates.parse(str)）
     */
    public static Date parse(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }

        // 时间戳
        if (dateStr.matches("\\d+")) {
            return new Date(Long.parseLong(dateStr));
        }

        // 遍历格式匹配
        for (String pattern : PATTERNS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                sdf.setLenient(false);
                return sdf.parse(dateStr);
            } catch (ParseException ignored) {
            }
        }

        throw new IllegalArgumentException("无法解析日期：" + dateStr);
    }
}
