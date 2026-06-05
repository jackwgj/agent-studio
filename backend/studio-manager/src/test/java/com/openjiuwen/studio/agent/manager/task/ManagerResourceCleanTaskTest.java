/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.AppMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class ManagerResourceCleanTaskTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentMapper agentMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AppMapper appMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MappingMapper mappingMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowMapper workflowMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @InjectMocks
    private ManagerResourceCleanTask managerResourceCleanTask;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(managerResourceCleanTask, "softDeleteTtlDays", 730);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_cleanOldSoftDeleteResource_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(mappingMapper.deleteBatchByAppId(anyString(), eq(null), eq(true))).thenReturn(0);

            when(appMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            List<String> appIds = new ArrayList<>();
            appIds.add("not_empty");
            when(appMapper.findAppToBeDeleted(any(LocalDateTime.class))).thenReturn(appIds);

            List<String> workflowIds = new ArrayList<>();
            workflowIds.add("not_empty");
            when(workflowMapper.findWorkflowToBeDeleted(anyLong())).thenReturn(workflowIds);
            when(workflowMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);

            when(agentMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            List<String> agentIds = new ArrayList<>();
            agentIds.add("not_empty");
            when(agentMapper.findAgentToBeDeleted(any(LocalDateTime.class))).thenReturn(agentIds);

            // When
            managerResourceCleanTask.cleanOldSoftDeleteResource();
        });
    }

    @Test
    void test_cleanOldSoftDeleteResource_should_not_throw_exception1() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(mappingMapper.deleteBatchByAppId(anyString(), eq(null), eq(true))).thenReturn(0);

            when(appMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            when(appMapper.findAppToBeDeleted(any(LocalDateTime.class))).thenReturn(null);

            when(workflowMapper.findWorkflowToBeDeleted(anyLong())).thenReturn(null);
            when(workflowMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);

            when(agentMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            when(agentMapper.findAgentToBeDeleted(any(LocalDateTime.class))).thenReturn(null);

            // When
            managerResourceCleanTask.cleanOldSoftDeleteResource();
        });
    }

    @Test
    void test_cleanOldSoftDeleteResource_should_not_throw_exception3() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(mappingMapper.deleteBatchByAppId(anyString(), eq(null), eq(true))).thenReturn(0);

            doThrow(NullPointerException.class).when(mgObsService).deleteObsObjects(anyString());

            when(appMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            List<String> appIds = new ArrayList<>();
            appIds.add("not_empty");
            when(appMapper.findAppToBeDeleted(any(LocalDateTime.class))).thenReturn(appIds);

            List<String> workflowIds = new ArrayList<>();
            workflowIds.add("not_empty");
            when(workflowMapper.findWorkflowToBeDeleted(anyLong())).thenReturn(workflowIds);
            when(workflowMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);

            when(agentMapper.deleteByPrimaryKeys(anyList())).thenReturn(0);
            List<String> agentIds = new ArrayList<>();
            agentIds.add("not_empty");
            when(agentMapper.findAgentToBeDeleted(any(LocalDateTime.class))).thenReturn(agentIds);

            // When
            managerResourceCleanTask.cleanOldSoftDeleteResource();
        });
    }
}
