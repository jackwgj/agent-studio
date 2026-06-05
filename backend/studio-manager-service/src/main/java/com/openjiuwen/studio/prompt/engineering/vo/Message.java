/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述 模型chat接口message
 *
 */
@Data
@NoArgsConstructor
public class Message {
    private String role;

    private String content;
}
