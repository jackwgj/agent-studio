/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 功能描述
 *
 */
public class HealthServiceTest extends BaseTest {
    @Autowired
    private HealthService healthService;

    @Test
    public void test_health_check_should_equal_result_when_test_data_combination() throws Exception {

        // run the test
        String result = healthService.healthCheck();

        // verify the results
        assertEquals("ok", result);

        // run the test
        String managerResult = healthService.managerHealthCheck();
        assertEquals("ok", managerResult);
    }
}
