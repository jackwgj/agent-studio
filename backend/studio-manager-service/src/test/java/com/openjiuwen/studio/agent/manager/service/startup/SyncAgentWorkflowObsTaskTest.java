/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.startup;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.AgentVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.AppMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseChannelMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.prompt.engineering.service.stratup.UpdatePromptService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class SyncAgentWorkflowObsTaskTest {

    @Mock
    private RedisClient redisClient;

    @Mock
    private MgObsService obsService;

    @Mock
    private AppMapper appMapper;

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private PluginMapper pluginMapper;

    @Mock
    private AgentVersionMapper agentVersionMapper;

    @Mock
    private ReleaseChannelMapper releaseChannelMapper;

    @Mock
    private McpUpdateService mcpUpdateService;

    @Mock
    private UpdatePromptService updatePromptService;

    @Mock
    private RedisLock redisLock;

    @Mock
    private Executor taskExecutor;

    @Mock
    private ResourceLoader resourceLoader;

    private SyncAgentWorkflowObsTask syncTask;

    @BeforeEach
    void setUp() throws Exception {
        syncTask = new SyncAgentWorkflowObsTask(resourceLoader);
        ReflectionTestUtils.setField(syncTask, "redisClient", redisClient);
        ReflectionTestUtils.setField(syncTask, "obsService", obsService);
        ReflectionTestUtils.setField(syncTask, "appMapper", appMapper);
        ReflectionTestUtils.setField(syncTask, "agentMapper", agentMapper);
        ReflectionTestUtils.setField(syncTask, "workflowMapper", workflowMapper);
        ReflectionTestUtils.setField(syncTask, "pluginMapper", pluginMapper);
        ReflectionTestUtils.setField(syncTask, "agentVersionMapper", agentVersionMapper);
        ReflectionTestUtils.setField(syncTask, "releaseChannelMapper", releaseChannelMapper);
        ReflectionTestUtils.setField(syncTask, "mcpUpdateService", mcpUpdateService);
        ReflectionTestUtils.setField(syncTask, "updatePromptService", updatePromptService);
        ReflectionTestUtils.setField(syncTask, "taskExecutor", taskExecutor);
        ReflectionTestUtils.setField(syncTask, "isHcs", false);
        ReflectionTestUtils.setField(syncTask, "isCustom", false);
        ReflectionTestUtils.setField(syncTask, "opSvcProjectId", "op-project-id");
        lenient().when(redisClient.getLock(anyString())).thenReturn(redisLock);
        lenient().when(redisLock.tryLock(any(Duration.class))).thenReturn(true);
    }

    @Test
    void testRun_NotHcs() {
        ReflectionTestUtils.setField(syncTask, "isHcs", false);

        syncTask.run();

        verify(redisClient, never()).getLock(anyString());
    }

    @Test
    void testRun_HcsWithLockSuccess() {
        ReflectionTestUtils.setField(syncTask, "isHcs", true);

        syncTask.run();

        verify(redisClient).getLock("SYNC_PRE_AGENT_WORKFLOW_OBS_LOCK");
        verify(taskExecutor, times(3)).execute(any(Runnable.class));
    }

    @Test
    void testRun_HcsWithLockFailed() throws Exception {
        ReflectionTestUtils.setField(syncTask, "isHcs", true);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(false);

        syncTask.run();

        verify(taskExecutor, never()).execute(any(Runnable.class));
    }

    @Test
    void testRun_HcsWithException() {
        ReflectionTestUtils.setField(syncTask, "isHcs", true);
        when(redisClient.getLock(anyString())).thenThrow(new RuntimeException("lock error"));

        assertDoesNotThrow(() -> syncTask.run());
    }

    @Test
    void testGetOBSPath_ValidUrl() {
        String result = SyncAgentWorkflowObsTask.getOBSPath("/path/to/obs/workflow/test.json");
        assertEquals("workflow/test.json", result);
    }

    @Test
    void testGetOBSPath_NullUrl() {
        assertNull(SyncAgentWorkflowObsTask.getOBSPath(null));
    }

    @Test
    void testGetOBSPath_NoObsInUrl() {
        assertNull(SyncAgentWorkflowObsTask.getOBSPath("/path/to/file.json"));
    }

    @Test
    void testGetOBSPath_ObsAtEnd() {
        String result = SyncAgentWorkflowObsTask.getOBSPath("/path/to/obs/");
        assertEquals("", result);
    }

    @Test
    void testUploadPreObsFiles_NullPath() {
        syncTask.uploadPreObsFiles(null);
        verify(obsService, never()).putObject(anyString(), anyString());
    }

    @Test
    void testUploadPreObsFiles_EmptyPath() {
        syncTask.uploadPreObsFiles("");
        verify(obsService, never()).putObject(anyString(), anyString());
    }

    @Test
    void testUpdateProjectId_Success() {
        syncTask.updateProjectId();

        verify(appMapper).updateProjectIdByAppIds(eq("op-project-id"), anyList());
        verify(agentMapper).updateProjectIdByAgentIds(eq("op-project-id"), anyList());
        verify(workflowMapper).updateProjectIdByIds(eq("op-project-id"), anyList());
        verify(agentVersionMapper).updateProjectIdByVersionIds(eq("op-project-id"), anyList());
        verify(releaseChannelMapper).updateProjectIdByIds(eq("op-project-id"), anyList());
    }
}
