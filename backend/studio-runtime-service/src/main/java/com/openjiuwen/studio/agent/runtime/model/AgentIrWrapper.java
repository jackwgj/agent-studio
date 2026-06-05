/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model;

import com.openjiuwen.studio.agent.common.sensitive.SensitiveEntity;
import com.openjiuwen.studio.agent.runtime.entity.ModelConfig;
import com.openjiuwen.studio.agent.runtime.enums.AgentIrType;

import lombok.Data;

/**
 * IR部分信息
 *
 */
@Data
public class AgentIrWrapper {
    /**
     * Agent IR md5值
     */
    private String md5;

    /**
     * Agent IR中的metadata
     */
    private AgentIrMetadata metadata;

    /**
     * Agent IR modelConfig
     */
    private ModelConfig modelConfig;

    /**
     * 内容审核
     */
    private SensitiveEntity sensitiveEntity;

    /**
     * IR上下文
     */
    private AgentIrContext context;

    private boolean safetyBarrier;

    /**
     * IR类型
     */
    private AgentIrType irType;

    public boolean isContentReviewEnabled() {
        return sensitiveEntity != null && sensitiveEntity.isEnabled();
    }
}
