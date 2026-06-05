/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models.memory;

import lombok.Data;

import java.util.List;

@Data
public class BatchDeleteMemoryRepositoryRequestBody {
    private List<String> ids;
}
