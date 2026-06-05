/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import lombok.Data;

import java.util.List;

/**
 * agent 列表信息
 */
@Data
public class AgentListResp {
    private List<AgentSummaryInfo> agents;

    private long total;
}
