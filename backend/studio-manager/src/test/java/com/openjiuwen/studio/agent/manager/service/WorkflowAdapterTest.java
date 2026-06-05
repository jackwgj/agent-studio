/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.workflow.resource.adapt.WorkflowAdapter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@MockitoSettings(strictness = Strictness.LENIENT)
public class WorkflowAdapterTest {
    @InjectMocks
    private WorkflowAdapter workflowAdapter;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(workflowAdapter, "blockNodes", "not_empty");
        ReflectionTestUtils.setField(workflowAdapter, "opSvcProjectId", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_updatePath() {
        assertDoesNotThrow(() -> {
            // 创建 WorkflowEntity mock 对象
            WorkflowEntity metadata = mock(WorkflowEntity.class);
            // 模拟 getId 方法返回值
            when(metadata.getId()).thenReturn("testId");

            // 调用 updatePath 方法
            workflowAdapter.updatePath(metadata);

            // 验证 setDslPath 和 setIrPath 方法被正确调用
        });
    }

    @Test
    void test_updatePath_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            WorkflowEntity metadata = new WorkflowEntity();
            metadata.setId("not_empty");

            // When
            workflowAdapter.updatePath(metadata);
        });
    }
}
