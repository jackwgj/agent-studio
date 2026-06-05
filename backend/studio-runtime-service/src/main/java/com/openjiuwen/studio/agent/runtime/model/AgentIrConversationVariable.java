/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * jiuwen ir文件中的会话变量
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentIrConversationVariable {

    private String name;

    private String description;

    private String defaultValue;
}
