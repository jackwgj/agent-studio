/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 分页查询
 */
@Getter
@Setter
@ToString
public class PageInfo {
    private static final int DEFAULT_INDEX = 1;

    private static final int DEFAULT_SIZE = 10;

    @Min(value = 1, message = "page size must be more than 1")
    private int num;

    @Max(value = 1000, message = "page size must be less than 1000")
    private int size;

    public PageInfo() {
        this.num = DEFAULT_INDEX;
        this.size = DEFAULT_SIZE;
    }

    public PageInfo(Integer index, Integer size) {
        if (index == null || size == null) {
            this.num = DEFAULT_INDEX;
            this.size = DEFAULT_SIZE;
        } else {
            this.num = index;
            this.size = size;
        }
    }

    public int computeOffset() {
        int offset = 0;
        if (this.size > 0) {
            offset = (this.num - 1) * this.size;
        }
        return offset;
    }
}