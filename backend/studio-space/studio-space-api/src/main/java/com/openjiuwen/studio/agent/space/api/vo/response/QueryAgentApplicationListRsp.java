/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 智能体应用列表响应体
 */
@Data
public class QueryAgentApplicationListRsp {
    private Long count;

    @JsonProperty("agent_list")
    private List<AgentApplicationInfo> agentList = null;
}
