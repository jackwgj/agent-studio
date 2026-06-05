/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.bo;

import lombok.Builder;
import lombok.Data;

/**
 * agent 流式事件数据结构
 *
 */
@Data
@Builder
public class AgentStreamEvent {
    private String object;

    private String content;

    private Long created;
}
