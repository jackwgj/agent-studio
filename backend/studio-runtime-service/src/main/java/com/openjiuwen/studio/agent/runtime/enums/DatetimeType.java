/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.enums;

import java.util.Locale;

/**
 * 时间日期类型
 *
 */
public enum DatetimeType {
    DATETIME,
    SECOND,
    MILLISECOND;

    public static DatetimeType enumOf(String type) {
        switch (type.toLowerCase(Locale.ROOT)) {
            case "datetime":
                return DATETIME;
            case "second":
                return SECOND;
            case "millisecond":
                return MILLISECOND;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
