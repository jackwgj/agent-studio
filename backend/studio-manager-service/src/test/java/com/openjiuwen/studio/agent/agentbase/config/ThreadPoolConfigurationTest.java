/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@ExtendWith(MockitoExtension.class)
public class ThreadPoolConfigurationTest {

    @Test
    void testKnowledgeRepoCopyExecutor() {
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        Executor executor = config.instanceInitialExecutor();
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
    }

    @Test
    void testLicenseReportTaskExecutor() {
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        Executor executor = config.licenseReportTaskExecutor();
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
    }

    @Test
    void testObsTaskExecutor() {
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        Executor executor = config.obsTaskExecutor();
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
    }

    @Test
    void testTemplateRepoMgmtExecutor() {
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        Executor executor = config.templateRepoMgmtExecutor();
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
    }
}
