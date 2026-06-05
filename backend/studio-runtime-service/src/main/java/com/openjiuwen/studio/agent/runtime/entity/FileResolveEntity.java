/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述
 */
@Data
@NoArgsConstructor
public class FileResolveEntity {

    /**
     * 状态码 1:成功 0:失败
     */
    private Integer code;

    /**
     * 文件内容
     */
    private String content;
}
