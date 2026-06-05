/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models.memory;

import lombok.Data;

/**
 * 保存记忆库配置的请求体
 */
@Data
public class SaveOrUpdateMemoryRepositoryRequestBody {

    /**
     * 记忆库id
     */
    private String id;

    /**
     * 记忆库配置
     */
    private MemoryRepositoryConfig config;
}
