/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * PaginationUtils
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaginationUtils {
    /**
     * paginate
     *
     * @param list list
     * @param page page
     * @param size size
     * @return List<T>
     */
    public static <T> List<T> paginate(List<T> list, int page, int size) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        int total = list.size();
        int offset = (page - 1) * size;

        if (offset >= total) {
            return Collections.emptyList();
        }

        int toIndex = Math.min(offset + size, total);
        return list.subList(offset, toIndex);
    }
}
