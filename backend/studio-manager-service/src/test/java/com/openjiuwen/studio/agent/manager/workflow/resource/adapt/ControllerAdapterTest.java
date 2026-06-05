/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.adapt;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.AgentMemoryConfig;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.MemoryRepoEntity;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.MemoryRepoMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.ShareResourceManagerService;
import com.openjiuwen.studio.agent.manager.service.AgentCommonService;

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

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class ControllerAdapterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareResourceManagerService shareResourceManagerService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentMapper agentMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReleaseVersionMapper releaseVersionMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MemoryRepoMapper memoryRepoMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService obsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentCommonService agentCommonService;

    @InjectMocks
    private com.openjiuwen.studio.agent.manager.workflow.resource.adapt.ControllerAdapter controllerAdapter;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：agent的记忆库配置为null
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_withNullMemoryConfig() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            Agent agent = new Agent();
            agent.setAgentId("agent-123");
            agent.setName("Test Agent");
            agent.setMemoryConfig(null);

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn("test-workspace");

            // When
            invokeVerifyingAndReplaceResourceInfo(agent);

            // Then - 记忆库配置应该保持为null
            assertNull(agent.getMemoryConfig());
            verify(memoryRepoMapper, never()).selectByIdAndWorkspaceId(anyString(), anyString());
        }
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：记忆库存在，配置保持不变
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_withValidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            String workspaceId = "test-workspace";
            String memoryRepoId = "valid-memory-repo-id";

            Agent agent = new Agent();
            agent.setAgentId("agent-123");
            agent.setName("Test Agent");

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(memoryRepoId);
            memoryConfig.setName("Test Memory Repo");
            memoryConfig.setDescription("Test Description");
            agent.setMemoryConfig(memoryConfig);

            MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
            memoryRepoEntity.setId(memoryRepoId);
            memoryRepoEntity.setName("Test Memory Repo");

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(memoryRepoId, workspaceId)).thenReturn(memoryRepoEntity);

            // When
            invokeVerifyingAndReplaceResourceInfo(agent);

            // Then - 记忆库配置应该保持不变
            assertNotNull(agent.getMemoryConfig());
            assertEquals(memoryRepoId, agent.getMemoryConfig().getMemoryRepoId());
            assertEquals("Test Memory Repo", agent.getMemoryConfig().getName());
            assertEquals("Test Description", agent.getMemoryConfig().getDescription());
        }
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：记忆库不存在，配置被设置为null
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_withInvalidMemoryRepo() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            String workspaceId = "test-workspace";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            Agent agent = new Agent();
            agent.setAgentId("agent-123");
            agent.setName("Test Agent");

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(invalidMemoryRepoId);
            memoryConfig.setName("Invalid Memory Repo");
            memoryConfig.setDescription("Invalid Description");
            agent.setMemoryConfig(memoryConfig);

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            // When
            invokeVerifyingAndReplaceResourceInfo(agent);

            // Then - 记忆库配置应该被设置为null
            assertNull(agent.getMemoryConfig());
        }
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：记忆库ID为空字符串
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_withEmptyMemoryRepoId() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            Agent agent = new Agent();
            agent.setAgentId("agent-123");
            agent.setName("Test Agent");

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId("");
            memoryConfig.setName("Empty Memory Repo");
            agent.setMemoryConfig(memoryConfig);

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn("test-workspace");
            when(memoryRepoMapper.selectByIdAndWorkspaceId("", "test-workspace")).thenReturn(null);

            // When
            invokeVerifyingAndReplaceResourceInfo(agent);

            // Then - 记忆库配置应该被设置为null，因为空字符串ID对应的记忆库不存在
            assertNull(agent.getMemoryConfig());
        }
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：agent包含其他配置信息，验证只清除记忆库配置
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_withOtherConfigs() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            String workspaceId = "test-workspace";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            Agent agent = new Agent();
            agent.setAgentId("agent-123");
            agent.setName("Test Agent");
            agent.setDescription("Test Description");
            agent.setProjectId("project-123");
            agent.setWorkspaceId(workspaceId);
            agent.setTraceId("trace-123");
            agent.setStatus("draft");

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(invalidMemoryRepoId);
            memoryConfig.setName("Invalid Memory Repo");
            agent.setMemoryConfig(memoryConfig);

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            // When
            invokeVerifyingAndReplaceResourceInfo(agent);

            // Then - 只有记忆库配置应该被设置为null，其他配置保持不变
            assertNull(agent.getMemoryConfig());
            assertEquals("agent-123", agent.getAgentId());
            assertEquals("Test Agent", agent.getName());
            assertEquals("Test Description", agent.getDescription());
            assertEquals("project-123", agent.getProjectId());
            assertEquals(workspaceId, agent.getWorkspaceId());
            assertEquals("trace-123", agent.getTraceId());
            assertEquals("draft", agent.getStatus());
        }
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：workspaceId变化时的处理
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_withDifferentWorkspaceIds() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            String currentWorkspaceId = "current-workspace";
            String agentWorkspaceId = "agent-workspace";
            String memoryRepoId = "valid-memory-repo-id";

            Agent agent = new Agent();
            agent.setAgentId("agent-123");
            agent.setName("Test Agent");
            agent.setWorkspaceId(agentWorkspaceId);

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(memoryRepoId);
            memoryConfig.setName("Test Memory Repo");
            agent.setMemoryConfig(memoryConfig);

            MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
            memoryRepoEntity.setId(memoryRepoId);
            memoryRepoEntity.setName("Test Memory Repo");

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn(currentWorkspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(memoryRepoId, currentWorkspaceId)).thenReturn(memoryRepoEntity);

            // When
            invokeVerifyingAndReplaceResourceInfo(agent);

            // Then - 根据当前workspaceId查询，记忆库配置应该保持不变
            assertNotNull(agent.getMemoryConfig());
            assertEquals(memoryRepoId, agent.getMemoryConfig().getMemoryRepoId());
        }
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：记忆库mapper返回异常时的处理
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_withMapperException() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            String workspaceId = "test-workspace";
            String memoryRepoId = "exception-memory-repo-id";

            Agent agent = new Agent();
            agent.setAgentId("agent-123");
            agent.setName("Test Agent");

            AgentMemoryConfig memoryConfig = new AgentMemoryConfig();
            memoryConfig.setMemoryRepoId(memoryRepoId);
            memoryConfig.setName("Exception Memory Repo");
            agent.setMemoryConfig(memoryConfig);

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn(workspaceId);
            when(memoryRepoMapper.selectByIdAndWorkspaceId(memoryRepoId, workspaceId))
                .thenThrow(new RuntimeException("Database connection error"));

            // When & Then - 应该正常处理异常，不会影响memoryConfig的原始值
            assertThrows(RuntimeException.class, () -> invokeVerifyingAndReplaceResourceInfo(agent));
        }
    }

    /**
     * 测试verifyingAndReplaceResourceInfo方法
     * 场景：记忆库配置对象被多次验证
     */
    @Test
    void testVerifyingAndReplaceResourceInfo_multipleVerifications() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(null);
            // Given
            String workspaceId = "test-workspace";
            String validMemoryRepoId = "valid-memory-repo-id";
            String invalidMemoryRepoId = "invalid-memory-repo-id";

            when(RequestContextUtils.getRequestWorkspaceId()).thenReturn(workspaceId);

            // 第一次验证 - 有效记忆库
            Agent agent1 = new Agent();
            agent1.setAgentId("agent-1");
            agent1.setName("Test Agent 1");

            AgentMemoryConfig memoryConfig1 = new AgentMemoryConfig();
            memoryConfig1.setMemoryRepoId(validMemoryRepoId);
            memoryConfig1.setName("Valid Memory Repo");
            agent1.setMemoryConfig(memoryConfig1);

            MemoryRepoEntity memoryRepoEntity = MemoryRepoEntity.builder().build();
            memoryRepoEntity.setId(validMemoryRepoId);
            memoryRepoEntity.setName("Test Memory Repo");

            when(memoryRepoMapper.selectByIdAndWorkspaceId(validMemoryRepoId, workspaceId)).thenReturn(memoryRepoEntity);

            invokeVerifyingAndReplaceResourceInfo(agent1);
            assertNotNull(agent1.getMemoryConfig());

            // 第二次验证 - 无效记忆库
            Agent agent2 = new Agent();
            agent2.setAgentId("agent-2");
            agent2.setName("Test Agent 2");

            AgentMemoryConfig memoryConfig2 = new AgentMemoryConfig();
            memoryConfig2.setMemoryRepoId(invalidMemoryRepoId);
            memoryConfig2.setName("Invalid Memory Repo");
            agent2.setMemoryConfig(memoryConfig2);

            when(memoryRepoMapper.selectByIdAndWorkspaceId(invalidMemoryRepoId, workspaceId)).thenReturn(null);

            invokeVerifyingAndReplaceResourceInfo(agent2);
            assertNull(agent2.getMemoryConfig());
        }
    }

    /**
     * 使用反射调用私有方法verifyingAndReplaceResourceInfo
     *
     * @param agent 要验证的资源信息对象
     */
    private void invokeVerifyingAndReplaceResourceInfo(Agent agent) {
        try {
            Method method = ControllerAdapter.class.getDeclaredMethod("verifyingAndReplaceResourceInfo", Agent.class);
            method.setAccessible(true);
            method.invoke(controllerAdapter, agent);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new AgentStudioException("Failed to invoke verifyingAndReplaceResourceInfo");
        }
    }
}
