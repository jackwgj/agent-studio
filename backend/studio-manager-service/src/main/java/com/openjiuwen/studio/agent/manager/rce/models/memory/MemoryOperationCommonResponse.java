/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 记忆操作通用的响应消息体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryOperationCommonResponse {

    private boolean success;
}
