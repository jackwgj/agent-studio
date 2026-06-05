/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * jiuwen ir里的上下文信息
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentIrContext {

    /**
     * 是否启用记忆功能
     */
    @Builder.Default
    private Boolean openMemory = false;

    /**
     * 长期记忆配置
     */
    private AgentIrLongTermMemory longTermMemory;

    /**
     * 会话变量列表
     */
    private List<AgentIrConversationVariable> conversationVariables;
}
