/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean;

/**
 * 租户数据清理编排服务
 * 参考InstanceInitializeService的设计模式，按顺序调用各个清理服务
 *
 * @since 2025-12-17
 */
public interface TenantDataCleanOrchestratorService {

    /**
     * 执行租户数据清理
     * 按照固定顺序清理各类数据
     *
     * @param domainId 租户ID
     */
    void executeClean(String domainId);
}