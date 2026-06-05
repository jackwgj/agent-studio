/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
@Builder
public class MessageEntity {
    private String role;

    private String content;
}
