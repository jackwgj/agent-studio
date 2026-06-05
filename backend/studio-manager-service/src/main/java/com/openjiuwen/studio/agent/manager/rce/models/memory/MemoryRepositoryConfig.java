/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models.memory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.LongTermMemoryStrategy;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 记忆库配置
 */
@Data
@Builder
public class MemoryRepositoryConfig {

    /**
     * 提取记忆频率
     */
    @JsonProperty("time_window")
    private TimeWindow timeWindow;

    /**
     * 记忆提取策略
     */
    @JsonProperty("long_term_memory_strategies")
    private List<LongTermMemoryStrategy> longTermMemoryStrategies;
}
