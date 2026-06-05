/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import org.apache.commons.lang3.Strings;

public class StringComparisonUtils {
    public static boolean equalsIgnoreCase(CharSequence str1, CharSequence str2) {
        return Strings.CI.equals(str1, str2);
    }

    public static boolean equals(CharSequence str1, CharSequence str2) {
        return Strings.CS.equals(str1, str2);
    }
}
