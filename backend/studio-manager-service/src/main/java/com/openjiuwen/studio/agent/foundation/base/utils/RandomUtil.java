/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class RandomUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成随机数
     *
     * @param num 生产小于num随机数
     * @return int
     */
    public static int getRandom(int num) {
        return SECURE_RANDOM.nextInt(num);
    }

}
