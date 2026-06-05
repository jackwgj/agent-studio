/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Slf4j
public class DateUtils {
    public static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    public static final String NORM_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formatDate(Timestamp time) {
        if (time == null) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat(DATE_TIME_FORMAT);
        return df.format(time);
    }

    public static String getNowDateStr() {
        SimpleDateFormat df = new SimpleDateFormat(NORM_DATETIME_FORMAT);
        return df.format(new Date());
    }

    public static String formatOffsetDateTime(OffsetDateTime time) {
        if (time == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        log.info("ZoneId: {}", ZoneId.systemDefault());
        return formatter.format(time.atZoneSameInstant(ZoneId.systemDefault()));
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

    public static Date asDate(LocalDate localDate) {
        if(localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        if(localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        if(date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        if(date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
