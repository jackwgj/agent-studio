/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

/**
 * Health service
 */

public interface IHealthService {

    /**
     * healthCheck
     */
    String healthCheck();

    /**
     * managerHealthCheck
     */
    String managerHealthCheck();
}