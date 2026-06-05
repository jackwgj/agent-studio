/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import lombok.Data;

/**
 * agent对话返回的具体内容
 */
@Data
public class AgentChatContent {
    private String role;

    private String content;
}
