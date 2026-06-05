/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.memory;

import lombok.Data;

import java.util.List;

/**
 * 用户记忆缓存区 会话记忆存储模型
 *
 */
@Data
public class UserCachedConversation {
    private String conversationId;

    private List<UserCachedChatTurn> chatTurns;
}
