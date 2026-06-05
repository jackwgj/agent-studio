/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import lombok.Data;

/**
 * 内置web搜索组件参数，用于json解析
 *
 */
@Data
public class InnerWebSearchParam {
    private String query;

    private Integer limit;
}
