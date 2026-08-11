/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 团队对话请求（manager 直传团队参数，runtime 转发引擎 /v1/conversation/team）。
 *
 * <p>子 Agent 纯无状态（方案 B）：引擎按 subAgentIds 加载各子 Agent 已有 IR 动态组装监督者；
 * Java 不传 systemPrompt（监督者提示词固定引擎侧，F4 决策）。conversationHistory 仅注入监督者上下文。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTeamReq {

    @NotNull
    private String query;

    @NotEmpty
    private List<String> subAgentIds;

    @NotNull
    private String modelDeploymentId;

    /** 多轮历史 list[{role, content}]，仅注入监督者，可为 null（第一轮） */
    private List<Map<String, String>> conversationHistory;
}
