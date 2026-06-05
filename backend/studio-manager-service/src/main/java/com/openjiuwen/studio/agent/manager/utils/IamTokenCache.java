/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

/**
 * 添加类的描述
 *
 */
public class IamTokenCache {

    private final String value;
    private final long expireAt;

    public IamTokenCache(String value, long expireAt) {
        this.value = value;
        this.expireAt = expireAt;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireAt;
    }

    public String getValue() {
        return value;
    }
}
