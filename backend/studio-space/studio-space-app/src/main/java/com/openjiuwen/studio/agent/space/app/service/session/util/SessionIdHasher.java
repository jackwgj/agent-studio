/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.service.session.util;

import com.google.common.hash.Hashing;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * sessionId需要通过hash存到数据库中
 *
 * @since 2023-07-24
 */
@Component
public class SessionIdHasher {
    /**
     * 生成sha256
     *
     * @param str 字符串
     * @return sha256字符串
     */
    public String hash(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
    }
}
