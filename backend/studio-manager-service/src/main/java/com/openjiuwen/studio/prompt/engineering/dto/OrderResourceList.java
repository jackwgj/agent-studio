/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 功能描述 订购资源列表
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResourceList {

    private List<OrderResource> list;

    private int page;

    private int size;

    private int total;
}
