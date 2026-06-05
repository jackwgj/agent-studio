/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class McpServiceExceptionUtilsTest extends BaseTest {
    @BeforeAll
    public static void init() {
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterAll
    static void end() {
    }

    @Test
    void testThrowUserNoPermissionError() {
        Assertions.assertThrows(Exception.class, McpServiceExceptionUtils::throwUserNoPermissionError);
    }
}
