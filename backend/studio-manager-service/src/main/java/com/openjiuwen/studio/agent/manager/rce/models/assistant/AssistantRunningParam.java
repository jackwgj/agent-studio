/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Assistant的运行参数
 *
 */
@Data
public class AssistantRunningParam {
    private String instructions;

    @Size(max = 1000)
    private List<String> residentTools;

    private String additionalInstructions;
}
