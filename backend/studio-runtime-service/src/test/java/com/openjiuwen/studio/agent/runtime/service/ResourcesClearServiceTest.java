/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.openjiuwen.studio.agent.common.utils.PublishUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
class ResourcesClearServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentIrCacheService agentIrCacheService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowIrCacheService workflowIrCacheService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PublishUtils publishUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObsService obsService;

    @InjectMocks
    private ResourcesClearService resourcesClearService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(resourcesClearService, "agentIrCacheService", agentIrCacheService);
        ReflectionTestUtils.setField(resourcesClearService, "workflowIrCacheService", workflowIrCacheService);
        ReflectionTestUtils.setField(resourcesClearService, "publishUtils", publishUtils);
        ReflectionTestUtils.setField(resourcesClearService, "obsService", obsService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_clearResource() throws Exception {
        // Then
        assertNotNull(resourcesClearService.clearResource("not_empty", "not_empty", "agent"));
        assertNotNull(resourcesClearService.clearResource("not_empty", "not_empty", "controller"));
        assertNotNull(resourcesClearService.clearResource("not_empty", "not_empty", "workflow"));
    }
}
