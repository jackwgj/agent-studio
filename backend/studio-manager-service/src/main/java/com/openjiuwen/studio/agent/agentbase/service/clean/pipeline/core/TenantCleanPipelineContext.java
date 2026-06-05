/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户清理管道上下文：在任务间传递数据和控制状态
 * <p>
 * 专门用于租户清理相关的数据传递
 *
 * @since 2025-12-20
 */
@Getter
@Setter
public class TenantCleanPipelineContext {

    /**
     * 租户ID
     */
    private String domainId;

    /**
     * 是否中断流程
     */
    private boolean shouldStop = false;

    /**
     * 中断流程的原因
     */
    private String stopReason;

    /**
     * 终止流程
     *
     * @param reason 终止原因
     */
    public void stop(String reason) {
        this.shouldStop = true;
        this.stopReason = reason;
    }

    /**
     * 检查流程是否已终止
     *
     * @return 是否已终止
     */
    public boolean isStopped() {
        return shouldStop;
    }
}