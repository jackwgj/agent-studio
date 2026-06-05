/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model.memory;

import lombok.Data;

/**
 * 用户记忆抽取配置
 *
 */
@Data
public class UserMemoryExtractCfg {
    private String userId;

    private String applicationId;

    private long ttlSeconds;

    private int maxChatTurns;
}
