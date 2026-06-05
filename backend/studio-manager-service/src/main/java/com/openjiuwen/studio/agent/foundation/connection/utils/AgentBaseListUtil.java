/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 列表处理类
 *
 * @since 2025-08-23
 */
@Slf4j
public class AgentBaseListUtil {

    /**
     * 根据offset和limit截取子列表
     *
     * @param src
     * @param offset
     * @param limit
     * @param <T>
     * @return
     */
    public static <T> List<T> slice(List<T> src, int offset, int limit) {
        if (src == null || src.isEmpty()) {
            return Collections.emptyList();
        }
        if (offset < 0) {
            log.error("offset [{}] must >= 0", offset);
            throw new IllegalArgumentException("offset must >= 0");
        }
        if (limit <= 0) {
            log.error("limit [{}] must > 0", limit);
            throw new IllegalArgumentException("limit must > 0");
        }

        int size = src.size();
        if (offset >= size) {
            return Collections.emptyList();
        }

        int end = Math.min(offset + limit, size);
        return src.subList(offset, end);
    }

}
