/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @InjectMocks
    private HealthService healthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(healthService, "content", "ok");
    }

    @Test
    void testHealthCheck_Success() {
        String result = healthService.healthCheck();
        assertNotNull(result);
        assertEquals("ok", result);
    }

    @Test
    void testHealthCheck_ReturnsConfiguredContent() {
        ReflectionTestUtils.setField(healthService, "content", "healthy");
        String result = healthService.healthCheck();
        assertEquals("healthy", result);
    }

    @Test
    void testManagerHealthCheck_Success() {
        String result = healthService.managerHealthCheck();
        assertNotNull(result);
        assertEquals("ok", result);
    }

    @Test
    void testManagerHealthCheck_ReturnsConfiguredContent() {
        ReflectionTestUtils.setField(healthService, "content", "manager-healthy");
        String result = healthService.managerHealthCheck();
        assertEquals("manager-healthy", result);
    }
}
