/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ApplicationTest {

    @Test
    void testMain_StartsApplication() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext ctx = mock(ConfigurableApplicationContext.class);
            mocked.when(() -> SpringApplication.run(Application.class, new String[]{}))
                    .thenReturn(ctx);
            assertDoesNotThrow(() -> Application.main(new String[]{}));
            mocked.verify(() -> SpringApplication.run(Application.class, new String[]{}));
        }
    }
}
