/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.model.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagValue {

    /**
     * 主题
     */
    private String topic;

    /**
     * 名称
     */
    private String name;

    /**
     * 值
     */
    private String value;
}
