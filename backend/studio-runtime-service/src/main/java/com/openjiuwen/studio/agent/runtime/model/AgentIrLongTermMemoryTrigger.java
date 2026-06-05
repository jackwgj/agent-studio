/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import com.openjiuwen.studio.agent.common.enums.LongTermMemoryTriggerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * jiuwen IR文件中长期记忆的触发配置
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentIrLongTermMemoryTrigger {

    /**
     * 配置trigger表示在对话中检索使用
     */
    @Builder.Default
    private LongTermMemoryTriggerType type = LongTermMemoryTriggerType.SEARCH;

}
