/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.AgentMemoryConfig;
import com.openjiuwen.studio.agent.manager.dto.ControllerVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.entity.MemoryRepoEntity;
import com.openjiuwen.studio.agent.manager.mapper.MemoryRepoMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class AgentImportServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MemoryRepoMapper memoryRepoMapper;

    @InjectMocks
    private AgentImportService agentImportService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(agentImportService, "memoryRepoMapper", memoryRepoMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    /**
     * 测试verifyingAndReplaceWorkflowResourceInfo方法
     * 场景：workflowVO为null
     */
    @Test
    void testVerifyingAndReplaceWorkflowResourceInfo_withNullWorkflowVO() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(null);
            WorkflowVO workflowVO = null;

            // When & Then - 不应该抛出异常，直接返回
            invokeVerifyingAndReplaceWorkflowResourceInfo(workflowVO);
        }
    }

    /**
     * 测试verifyingAndReplaceWorkflowResourceInfo方法
     * 场景：记忆库存在，配置保持不变
     */
    @Test
    @SuppressWarnings("unchecked")
    void testVerifyingAndReplaceWorkflowResourceInfo_withValidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String memoryRepoId = "valid-memory-repo-id";

            WorkflowVO workflowVO = new WorkflowVO();
            workflowVO.setId("workflow-123");
            workflowVO.setName("Test Workflow");

            Map<String, Object> configs = new HashMap<>();
            Map<String, Object> memoryConfigs = new HashMap<>();
            memoryConfigs.put(CommonConstant.MEMORY_REPO_ID, memoryRepoId);
            configs.put(Constants.Workflow.MEMORY_CONFIG, memoryConfigs);
            workflowVO.setConfigs(configs);

            MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
            memoryRepoEntity.setId(memoryRepoId);
            memoryRepoEntity.setName("Test Memory Repo");

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(memoryRepoId, workspaceId)).thenReturn(memoryRepoEntity);

            // When
            invokeVerifyingAndReplaceWorkflowResourceInfo(workflowVO);

            // Then - 记忆库配置应该保持不变
            assertNotNull(workflowVO.getConfigs());
            Map<String, Object> actualMemoryConfigs = (Map<String, Object>) workflowVO.getConfigs().get(Constants.Workflow.MEMORY_CONFIG);
            assertNotNull(actualMemoryConfigs);
            assertEquals(memoryRepoId, actualMemoryConfigs.get(CommonConstant.MEMORY_REPO_ID));
        }
    }

    /**
     * 测试verifyingAndReplaceWorkflowResourceInfo方法
     * 场景：记忆库不存在，配置被清空
     */
    @Test
    @SuppressWarnings("unchecked")
    void testVerifyingAndReplaceWorkflowResourceInfo_withInvalidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            WorkflowVO workflowVO = new WorkflowVO();
            workflowVO.setId("workflow-123");
            workflowVO.setName("Test Workflow");

            Map<String, Object> configs = new HashMap<>();
            Map<String, Object> memoryConfigs = new HashMap<>();
            memoryConfigs.put(CommonConstant.MEMORY_REPO_ID, invalidMemoryRepoId);
            memoryConfigs.put("name", "Invalid Memory Repo");
            configs.put(Constants.Workflow.MEMORY_CONFIG, memoryConfigs);
            workflowVO.setConfigs(configs);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            // When
            invokeVerifyingAndReplaceWorkflowResourceInfo(workflowVO);

            // Then - 记忆库配置应该被清空
            assertNotNull(workflowVO.getConfigs());
            Map<String, Object> actualMemoryConfigs = (Map<String, Object>) workflowVO.getConfigs().get(Constants.Workflow.MEMORY_CONFIG);
            assertNotNull(actualMemoryConfigs);
            assertEquals("", actualMemoryConfigs.get(CommonConstant.MEMORY_REPO_ID));
            assertEquals(1, actualMemoryConfigs.size()); // 只有memory_repo_id字段
        }
    }

    /**
     * 测试verifyingAndReplaceWorkflowResourceInfo方法
     * 场景：没有配置记忆库
     */
    @Test
    void testVerifyingAndReplaceWorkflowResourceInfo_withoutMemoryConfig() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            WorkflowVO workflowVO = new WorkflowVO();
            workflowVO.setId("workflow-123");
            workflowVO.setName("Test Workflow");

            Map<String, Object> configs = new HashMap<>();
            workflowVO.setConfigs(configs);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("test-workspace");

            // When & Then - 不应该抛出异常
            invokeVerifyingAndReplaceWorkflowResourceInfo(workflowVO);
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：dsl为null
     */
    @Test
    void testVerifyingAndReplaceControllerResourceInfo_withNullDsl() {
        // When & Then - 不应该抛出异常，直接返回
        invokeVerifyingAndReplaceControllerResourceInfo(null);
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：dsl不是ControllerVO 和 Map 类型
     */
    @Test
    void testVerifyingAndReplaceControllerResourceInfo_withNonControllerVO() {
        // Given
        Object dsl = "not a ControllerVO";

        // When & Then - 不应该抛出异常，直接返回
        invokeVerifyingAndReplaceControllerResourceInfo(dsl);
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：记忆库存在，配置保持不变
     */
    @Test
    void testVerifyingAndReplaceControllerResourceInfo_withValidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String memoryRepoId = "valid-memory-repo-id";

            ControllerVO controllerVO = new ControllerVO();
            controllerVO.setId("controller-123");
            controllerVO.setName("Test Controller");

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(memoryRepoId);
            memoryConfig.setName("Test Memory Repo");
            memoryConfig.setDescription("Test Description");
            controllerVO.setMemoryConfig(memoryConfig);

            MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
            memoryRepoEntity.setId(memoryRepoId);
            memoryRepoEntity.setName("Test Memory Repo");

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(memoryRepoId, workspaceId)).thenReturn(memoryRepoEntity);

            // When
            invokeVerifyingAndReplaceControllerResourceInfo(controllerVO);

            // Then - 记忆库配置应该保持不变
            assertNotNull(controllerVO.getMemoryConfig());
            assertEquals(memoryRepoId, controllerVO.getMemoryConfig().getMemoryRepoId());
            assertEquals("Test Memory Repo", controllerVO.getMemoryConfig().getName());
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：记忆库不存在，配置被清空
     */
    @Test
    void testVerifyingAndReplaceControllerResourceInfo_withInvalidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            ControllerVO controllerVO = new ControllerVO();
            controllerVO.setId("controller-123");
            controllerVO.setName("Test Controller");

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(invalidMemoryRepoId);
            memoryConfig.setName("Invalid Memory Repo");
            memoryConfig.setDescription("Invalid Description");
            controllerVO.setMemoryConfig(memoryConfig);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            // When
            invokeVerifyingAndReplaceControllerResourceInfo(controllerVO);

            // Then - 记忆库配置应该被设置为null
            assertNull(controllerVO.getMemoryConfig());
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：没有配置记忆库
     */
    @Test
    void testVerifyingAndReplaceControllerResourceInfo_withoutMemoryConfig() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            ControllerVO controllerVO = new ControllerVO();
            controllerVO.setId("controller-123");
            controllerVO.setName("Test Controller");
            controllerVO.setMemoryConfig(null);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("test-workspace");

            // When & Then - 不应该抛出异常
            invokeVerifyingAndReplaceControllerResourceInfo(controllerVO);
            assertNull(controllerVO.getMemoryConfig());
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：controllerVO包含其他配置信息
     */
    @Test
    void testVerifyingAndReplaceControllerResourceInfo_withOtherConfigs() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            ControllerVO controllerVO = new ControllerVO();
            controllerVO.setId("controller-123");
            controllerVO.setName("Test Controller");
            controllerVO.setDescription("Test Description");
            controllerVO.setProjectId("project-123");
            controllerVO.setWorkspaceId(workspaceId);

            // 添加节点配置
            List<Map<String, Object>> nodes = new ArrayList<>();
            Map<String, Object> node = new HashMap<>();
            node.put("id", "node-1");
            node.put("type", "agent");
            nodes.add(node);
            // 由于ControllerVO的节点需要特定类型，我们这里使用mocked list
            // 在实际测试中可能需要设置具体的节点类型

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(invalidMemoryRepoId);
            memoryConfig.setName("Invalid Memory Repo");
            controllerVO.setMemoryConfig(memoryConfig);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            // When
            invokeVerifyingAndReplaceControllerResourceInfo(controllerVO);

            // Then - 记忆库配置应该被设置为null，其他配置保持不变
            assertNull(controllerVO.getMemoryConfig());
            assertEquals("controller-123", controllerVO.getId());
            assertEquals("Test Controller", controllerVO.getName());
            assertEquals("Test Description", controllerVO.getDescription());
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：dsl为MAP类型，且记忆库存在，配置保持不变
     */
    @Test
    @SuppressWarnings("unchecked")
    void testVerifyingAndReplaceControllerResourceInfo_map_withValidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String memoryRepoId = "valid-memory-repo-id";

            Map<String, Object> dsl = new LinkedHashMap<>();
            dsl.put("id", "controller-123");
            dsl.put("name", "Test Controller");

            Map<String, Object> memoryConfig = new HashMap<>();
            memoryConfig.put(CommonConstant.MEMORY_REPO_ID, memoryRepoId);
            memoryConfig.put("name", "Test Memory Repo");
            memoryConfig.put("description", "Test Description");
            dsl.put(Constants.MEMORY_CONFIG, memoryConfig);

            MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
            memoryRepoEntity.setId(memoryRepoId);
            memoryRepoEntity.setName("Test Memory Repo");

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(memoryRepoId, workspaceId)).thenReturn(memoryRepoEntity);

            // When
            invokeVerifyingAndReplaceControllerResourceInfo(dsl);

            // Then - 记忆库配置应该保持不变
            assertNotNull(dsl.get(Constants.MEMORY_CONFIG));
            assertEquals(memoryRepoId, ((Map<String, Object>) dsl.get(Constants.MEMORY_CONFIG)).get(CommonConstant.MEMORY_REPO_ID));
            assertEquals("Test Memory Repo", ((Map<String, Object>) dsl.get(Constants.MEMORY_CONFIG)).get("name"));
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：dsl为MAP类型，且记忆库不存在，配置被清空
     */
    @Test
    @SuppressWarnings("unchecked")
    void testVerifyingAndReplaceControllerResourceInfo_map_withInvalidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            Map<String, Object> dsl = new LinkedHashMap<>();
            dsl.put("id", "controller-123");
            dsl.put("name", "Test Controller");

            Map<String, Object> memoryConfig = new HashMap<>();
            memoryConfig.put(CommonConstant.MEMORY_REPO_ID, invalidMemoryRepoId);
            memoryConfig.put("name", "Test Memory Repo");
            memoryConfig.put("description", "Test Description");
            dsl.put(Constants.MEMORY_CONFIG, memoryConfig);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            // When
            invokeVerifyingAndReplaceControllerResourceInfo(dsl);

            // Then - 记忆库配置应该被清空
            assertNotNull(dsl.get(Constants.MEMORY_CONFIG));
            Map<String, Object> actualMemoryConfigs = (Map<String, Object>) dsl.get(Constants.Workflow.MEMORY_CONFIG);
            assertNotNull(actualMemoryConfigs);
            assertEquals("", actualMemoryConfigs.get(CommonConstant.MEMORY_REPO_ID));
            assertEquals(1, actualMemoryConfigs.size()); // 只有memory_repo_id字段
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：dsl为MAP类型，且没有配置记忆库
     */
    @Test
    void testVerifyingAndReplaceControllerResourceInfo_map_withoutMemoryConfig() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            Map<String, Object> dsl = new LinkedHashMap<>();
            dsl.put("id", "controller-123");
            dsl.put("name", "Test Controller");
            dsl.put(Constants.MEMORY_CONFIG, null);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("test-workspace");

            // When & Then - 不应该抛出异常
            invokeVerifyingAndReplaceControllerResourceInfo(dsl);
            assertNull(dsl.get(Constants.MEMORY_CONFIG));
        }
    }

    /**
     * 测试verifyingAndReplaceControllerResourceInfo方法
     * 场景：dsl为MAP类型，且controllerVO包含其他配置信息
     */
    @Test
    @SuppressWarnings("unchecked")
    void testVerifyingAndReplaceControllerResourceInfo_map_withOtherConfigs() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            String workspaceId = "test-workspace";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            Map<String, Object> dsl = new LinkedHashMap<>();
            dsl.put("id", "controller-123");
            dsl.put("name", "Test Controller");
            dsl.put("description", "Test Description");
            dsl.put("project_id", "project-123");
            dsl.put("workspace_id", workspaceId);

            // 添加节点配置
            List<Map<String, Object>> nodes = new ArrayList<>();
            Map<String, Object> node = new HashMap<>();
            node.put("id", "node-1");
            node.put("type", "agent");
            nodes.add(node);
            // 由于ControllerVO的节点需要特定类型，我们这里使用mocked list
            // 在实际测试中可能需要设置具体的节点类型

            Map<String, Object> memoryConfig = new HashMap<>();
            memoryConfig.put(CommonConstant.MEMORY_REPO_ID, invalidMemoryRepoId);
            memoryConfig.put("name", "Invalid Memory Repo");
            dsl.put(Constants.MEMORY_CONFIG, memoryConfig);

            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            // When
            invokeVerifyingAndReplaceControllerResourceInfo(dsl);

            // Then - 记忆库配置应该被清空，其他配置保持不变
            assertNotNull(dsl.get(Constants.MEMORY_CONFIG));
            Map<String, Object> actualMemoryConfigs = (Map<String, Object>) dsl.get(Constants.Workflow.MEMORY_CONFIG);
            assertNotNull(actualMemoryConfigs);
            assertEquals("", actualMemoryConfigs.get(CommonConstant.MEMORY_REPO_ID));
            assertEquals(1, actualMemoryConfigs.size()); // 只有memory_repo_id字段
            assertEquals("controller-123", dsl.get("id"));
            assertEquals("Test Controller", dsl.get("name"));
            assertEquals("Test Description", dsl.get("description"));
        }
    }

    /**
     * 使用反射调用私有方法verifyingAndReplaceWorkflowResourceInfo
     */
    private void invokeVerifyingAndReplaceWorkflowResourceInfo(WorkflowVO workflowVO) {
        try {
            Method method = AgentImportService.class.getDeclaredMethod("verifyingAndReplaceWorkflowResourceInfo", WorkflowVO.class);
            method.setAccessible(true);
            method.invoke(agentImportService, workflowVO);
        } catch (Exception e) {
            throw new AgentStudioException("Failed to invoke verifyingAndReplaceWorkflowResourceInfo");
        }
    }

    /**
     * 使用反射调用私有方法verifyingAndReplaceControllerResourceInfo
     */
    private void invokeVerifyingAndReplaceControllerResourceInfo(Object dsl) {
        try {
            Method method = AgentImportService.class.getDeclaredMethod("verifyingAndReplaceControllerResourceInfo", Object.class);
            method.setAccessible(true);
            method.invoke(agentImportService, dsl);
        } catch (Exception e) {
            throw new AgentStudioException("Failed to invoke verifyingAndReplaceControllerResourceInfo");
        }
    }
}
