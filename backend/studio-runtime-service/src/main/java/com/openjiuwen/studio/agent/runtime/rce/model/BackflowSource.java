/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述 回流数据来源信息
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackflowSource {
    private String id;

    private String type;

    private String name;
}
