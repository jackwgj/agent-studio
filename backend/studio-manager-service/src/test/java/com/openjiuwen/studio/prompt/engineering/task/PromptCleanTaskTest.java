/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.task;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.v2.PromptEngineerDataSetService;

import com.openjiuwen.studio.prompt.engineering.task.PromptCleanTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PromptCleanTaskTest {

    @InjectMocks
    private PromptCleanTask promptCleanTask;

    @Mock
    private PromptEngineerDataSetService promptEngineerDataSetService;

    @Mock
    private PromptTaskMapper promptTaskMapper;

    @Mock
    private PePromptTemplateMapper pePromptTemplateMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用反射设置私有变量
        setPrivateField(promptCleanTask, "promptTaskMapper", promptTaskMapper);
        setPrivateField(promptCleanTask, "lastSuccessfulTemplateCleanTime", LocalDateTime.of(2024, 1, 1, 0, 0));
        setPrivateField(promptCleanTask, "lastSuccessfulTaskCleanTime", LocalDateTime.of(2024, 1, 1, 0, 0));
        setPrivateField(promptCleanTask, "lastSuccessfulTemplateCleanCount", 5);
        setPrivateField(promptCleanTask, "lastSuccessfulTaskCleanCount", 5);
    }

    /**
     * 通过反射设置私有字段的值
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void test_cleanOldSoftDeletedTemplates_withTemplates() {
        // Given: 模拟有模板需要清理
        List<String> templateIds = Arrays.asList("id1", "id2", "id3");
        when(pePromptTemplateMapper.findTemplatesToBeDeleted(any(LocalDateTime.class))).thenReturn(templateIds);
        doNothing().when(pePromptTemplateMapper).deleteTemplatesPermanently(templateIds);

        // When: 执行清理方法
        promptCleanTask.cleanOldSoftDeletedTemplates();

        // Then: 验证方法调用和状态更新
        verify(pePromptTemplateMapper, times(1)).findTemplatesToBeDeleted(any(LocalDateTime.class));
        verify(pePromptTemplateMapper, times(1)).deleteTemplatesPermanently(templateIds);
    }

    @Test
    void test_cleanOldSoftDeletedTemplates_noTemplates() {
        // Given: 模拟没有模板需要清理
        when(pePromptTemplateMapper.findTemplatesToBeDeleted(any(LocalDateTime.class))).thenReturn(new ArrayList<>());

        // When: 执行清理方法
        promptCleanTask.cleanOldSoftDeletedTemplates();
    }

    @Test
    void test_cleanOldSoftDeletedTemplates_exception() {
        // Given: 模拟清理时抛出异常
        List<String> templateIds = Arrays.asList("id1", "id2");
        when(pePromptTemplateMapper.findTemplatesToBeDeleted(any(LocalDateTime.class))).thenReturn(templateIds);
        doThrow(new RuntimeException("Delete failed")).when(pePromptTemplateMapper).deleteTemplatesPermanently(templateIds);

        // When: 执行清理方法
        promptCleanTask.cleanOldSoftDeletedTemplates();

        // Then: 验证方法调用和异常处理
        verify(pePromptTemplateMapper, times(1)).findTemplatesToBeDeleted(any(LocalDateTime.class));
        verify(pePromptTemplateMapper, times(1)).deleteTemplatesPermanently(templateIds);
    }

    /**
     * 测试有优化任务需要清理的情况
     */
    @Test
    void test_cleanOldSoftDeletedTasks_withTasks() {
        // Given: 模拟有优化任务需要清理
        List<String> taskIds = Arrays.asList("task1", "task2");

        // 模拟 findTasksToBeDeleted 返回任务 ID 列表
        when(promptTaskMapper.findTasksToBeDeleted(any(LocalDateTime.class))).thenReturn(taskIds);

        // 模拟 selectByIds 返回任务详情
        List<PromptTaskEntity> taskDetails = new ArrayList<>();

        // 添加模拟数据
        PromptTaskEntity task1 = new PromptTaskEntity();
        task1.setId("task1");
        task1.setProjectId("project1");
        task1.setWorkspaceId("workspace1");

        PromptTaskEntity task2 = new PromptTaskEntity();
        task2.setId("task2");
        task2.setProjectId("project2");
        task2.setWorkspaceId("workspace2");

        taskDetails.add(task1);
        taskDetails.add(task2);

        doReturn(taskDetails).when(promptTaskMapper).selectByIds(taskIds);
        doNothing().when(promptEngineerDataSetService).deleteDateSet(anyString(), anyString(), anyString());
        doNothing().when(promptTaskMapper).deleteTasksPermanently(taskIds);

        // When: 执行清理方法
        promptCleanTask.cleanOldSoftDeletedTasks();
    }
    /**
     * 测试没有优化任务需要清理的情况
     */
    @Test
    void test_cleanOldSoftDeletedTasks_noTasks() {
        // Given: 模拟没有优化任务需要清理
        when(promptTaskMapper.findTasksToBeDeleted(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // When: 执行清理方法
        promptCleanTask.cleanOldSoftDeletedTasks();

        // Then: 验证方法调用
        verify(promptTaskMapper, times(1)).findTasksToBeDeleted(any(LocalDateTime.class));
        verify(promptTaskMapper, never()).deleteTasksPermanently(any());
    }

    /**
     * 测试清理优化任务时抛出异常的情况
     */
    @Test
    void test_cleanOldSoftDeletedTasks_exception() {
        // Given: 模拟清理时抛出异常
        List<String> taskIds = Arrays.asList("task1", "task2");
        when(promptTaskMapper.findTasksToBeDeleted(any(LocalDateTime.class))).thenReturn(taskIds);
        doThrow(new RuntimeException("Delete failed")).when(promptTaskMapper).deleteTasksPermanently(taskIds);

        // When: 执行清理方法
        promptCleanTask.cleanOldSoftDeletedTasks();

        // Then: 验证方法调用和异常处理
        verify(promptTaskMapper, times(1)).findTasksToBeDeleted(any(LocalDateTime.class));
        verify(promptTaskMapper, times(1)).deleteTasksPermanently(taskIds);
    }

    @Test
    void test_getTaskStatus() {
        // When: 获取任务状态
        promptCleanTask.getTaskStatus();
    }

}
