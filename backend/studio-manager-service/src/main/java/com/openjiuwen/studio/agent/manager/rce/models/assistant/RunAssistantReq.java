/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 运行assistant请求体
 *
 */
@Data
@Builder
public class RunAssistantReq {
    @Size(max = 1000)
    private List<Message> messages;

    private AssistantRunningParam assistantRunningParam;
}
