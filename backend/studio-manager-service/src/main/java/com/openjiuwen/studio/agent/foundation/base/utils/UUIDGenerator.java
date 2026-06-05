/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.utils;

import org.apache.commons.lang3.Strings;

import java.util.UUID;

public class UUIDGenerator {

    /**
     * Gets uuid *
     *
     * @return the uuid
     */
    public static String getUUID() {
        return Strings.CS.remove(UUID.randomUUID().toString(), "-");
    }

}