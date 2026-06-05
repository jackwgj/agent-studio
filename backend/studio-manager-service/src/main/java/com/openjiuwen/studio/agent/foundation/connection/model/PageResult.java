/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.model;

import lombok.Data;

import java.util.List;

/**
 * 通用分页响应消息体
 *
 * @since 2025-08-22
 */
@Data
public class PageResult<T> {

    /**
     * 总数
     */
    private Long totalCount;

    /**
     * 资源列表
     */
    private List<T> items;
}
