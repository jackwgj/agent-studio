/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/**
 * TenantCleanPipelineExecutor测试类
 *
 * @since 2025-12-20
 */
public class TenantCleanPipelineExecutorTest {

    @InjectMocks
    private com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineExecutor executor;

    @Mock
    private TenantCleanPipelineTask task1;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 用例描述：正常流程，所有任务执行成功
     * 预置条件：任务列表不为空，任务shouldExecute返回true，任务execute无异常
     * 输入参数：context.setDomainId("testDomain")
     * 预期结果：context.isStopped()为false，stopReason为null
     */
    @Test
    public void test_execute_when_allTasksSuccess_then_pipelineSuccess() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomain");

        // Arrange
        when(task1.shouldExecute(context)).thenReturn(true);
        when(task1.getName()).thenReturn("Task1");

        // ✅ 改为直接设置数据 (使用 Setter 或直接构造)
        List<TenantCleanPipelineTask> tasks = List.of(task1);

        // 使用反射工具
        ReflectionTestUtils.setField(executor, "pipelineTasks", tasks);

        // Mock task.execute(context) to do nothing
        doNothing().when(task1).execute(context);

        // Act
        TenantCleanPipelineContext result = executor.execute(context);

        // Assert
        assertFalse(result.isStopped());
        assertNull(result.getStopReason());
        verify(task1, times(1)).execute(context);
        verify(task1, never()).onFailed(any(), any());
    }

    /**
     * 用例描述：任务执行失败，触发中断流程
     * 预置条件：任务列表不为空，任务shouldExecute返回true，任务execute抛出异常
     * 输入参数：context.setDomainId("testDomain")
     * 预期结果：context.isStopped()为true，stopReason为"Task execution failed: Task1"
     */
    @Test
    public void test_execute_when_taskFailed_then_pipelineStopped() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomain");

        // Arrange
        when(task1.shouldExecute(context)).thenReturn(true);
        when(task1.getName()).thenReturn("Task1");

        List<TenantCleanPipelineTask> tasks = List.of(task1);

        // 使用反射工具
        ReflectionTestUtils.setField(executor, "pipelineTasks", tasks);

        doThrow(new RuntimeException("Task1 error")).when(task1).execute(context);
        doNothing().when(task1).onFailed(eq(context), any());

        // Act
        TenantCleanPipelineContext result = executor.execute(context);

        // Assert
        assertTrue(result.isStopped());  // 这里应该为true，因为task.execute会抛出异常
        assertEquals("Task execution failed: Task1", result.getStopReason());
        verify(task1, times(1)).execute(context);
        verify(task1, times(1)).onFailed(eq(context), any());
    }
}