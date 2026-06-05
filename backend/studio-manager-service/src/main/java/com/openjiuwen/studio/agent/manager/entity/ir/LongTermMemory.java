/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity.ir;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 长期记忆相关的配置
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LongTermMemory {

    /**
     * 会话记忆
     */
    private ConversationMemory conversationMemory;

}
