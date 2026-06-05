/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

public class SqlLikeEscapeHelper {

    public static String escapeLikeCharacters(String initialString) {
        if (initialString == null) {
            return null;
        }
        return initialString.replaceAll("/", "//")
            .replaceAll("%", "/%")
            .replaceAll("'", "/'")
            .replaceAll("_", "/_")
            .replaceAll("\\\\", "/\\\\")
            .replaceAll("\"", "/\"");
    }
}
