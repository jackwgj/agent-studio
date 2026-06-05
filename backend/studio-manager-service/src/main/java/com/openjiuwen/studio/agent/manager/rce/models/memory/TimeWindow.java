/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models.memory;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeWindow {
    /**
     * 抽取记忆轮数，即对话执行多少轮后触发提取记忆操作
     * minimum: 1
     */
    private Integer conversationRound;

    /**
     * 抽取记忆时间，即每间隔多长时间后触发提取记忆操作
     * minimum: 5
     */
    private Integer timeSpan;
}
