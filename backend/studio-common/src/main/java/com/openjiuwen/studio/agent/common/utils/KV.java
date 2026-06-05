/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 键值对
 *
 */
@Data
@AllArgsConstructor
public class KV {
    private String key;

    private Object value;

    /**
     * 初始化KV对象
     *
     * @param key 键
     * @param value 值
     * @return KV对象
     */
    public static KV of(final String key, final Object value) {
        return new KV(key, value);
    }
}
