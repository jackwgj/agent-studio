/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import java.security.SecureRandom;

public class SecureRandomUtils {

    private static final SecureRandom INSTANCE = new SecureRandom();

    /**
     * Get singleton SecureRandom instance.
     *
     * @return SecureRandom instance
     */
    public static SecureRandom getInstance() {
        return INSTANCE;
    }
}