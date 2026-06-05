/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 */
@Service
public class HealthService implements IHealthService {
    @Value("${management.health.service.content}")
    private String content;

    @Override
    public String healthCheck() {
        return content;
    }

    @Override
    public String managerHealthCheck() {
        return content;
    }
}
