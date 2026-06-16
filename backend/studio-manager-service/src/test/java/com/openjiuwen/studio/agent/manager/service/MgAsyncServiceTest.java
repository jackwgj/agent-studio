/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MgAsyncServiceTest {

    @InjectMocks
    private MgAsyncService mgAsyncService;

    @Test
    void testCallRunAgentStream_RunnableExecuted() {
        AtomicBoolean executed = new AtomicBoolean(false);
        mgAsyncService.callRunAgentStream(() -> executed.set(true));
        assertTrue(executed.get());
    }

    @Test
    void testCallRunAgentStream_RunnableWithCounter() {
        AtomicInteger counter = new AtomicInteger(0);
        mgAsyncService.callRunAgentStream(counter::incrementAndGet);
        assertEquals(1, counter.get());
    }

    @Test
    void testCallRunAgentStream_RunnableThrowsException() {
        assertThrows(RuntimeException.class, () ->
            mgAsyncService.callRunAgentStream(() -> { throw new RuntimeException("test"); }));
    }
}
