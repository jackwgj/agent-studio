/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话使用到的记忆配置
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentIrConversationMemory {

    /**
     * 是否将会话生成长期记忆
     */
    private boolean generationMemory;

    /**
     * 长期记忆的触发
     */
    private AgentIrLongTermMemoryTrigger trigger;

}
