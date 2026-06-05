/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import lombok.Data;

/**
 * agent对话返回的chunk
 */
@Data
public class AgentChatResp {
    private String event;

    private Object content;

    private long createdTime;

    private String role;

    private Object latency;

    private Object plugin;
}
