/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * uuid生成工具类
 *
 */
@Slf4j
public class UuidUtils {
    /**
     * 生成一个不带连字符的UUID字符串。
     *
     * @return 返回一个32位的UUID字符串，所有连字符已被移除。
     */
    public static String getUUID() {
        // 生成一个随机的UUID并转换为字符串，然后移除所有的连字符
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
