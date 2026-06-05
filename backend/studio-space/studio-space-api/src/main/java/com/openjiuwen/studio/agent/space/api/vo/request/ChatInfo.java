/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.request;

import com.openjiuwen.studio.agent.space.api.vo.AgentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatInfo {
    private String conversationId;

    private String query;

    private AgentType agentType;

    private String agentId;

    private String agentWorkSpaceId;

    private List<String> fileId;
}
