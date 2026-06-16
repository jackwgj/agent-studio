/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseServiceImpl;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.HistoryWorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.MemoryRepoMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.SkillMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.mcp.auth.IMcpBase;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceManager;
import com.openjiuwen.studio.agent.manager.service.plugin.IPlugin;
import com.openjiuwen.studio.agent.manager.service.plugin.IPluginBase;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.openjiuwen.studio.agent.manager.dto.ListAppRelationsQo;
import com.openjiuwen.studio.agent.manager.dto.ListResourceRelationsQo;
import com.openjiuwen.studio.agent.manager.dto.ListResourcesRelationsQo;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RelationManagementServiceTest {

    private ReleaseVersionMapper releaseVersionMapper;
    private AgentMapper agentMapper;
    private MappingMapper mappingMapper;
    private WorkflowMapper workflowMapper;
    private HistoryWorkflowMapper historyWorkflowMapper;
    private PluginMapper pluginMapper;
    private ToolMapper toolMapper;
    private WorkspaceMapper workspaceMapper;
    private MemoryRepoMapper memoryRepoMapper;
    private IPlugin pluginService;
    private ModelServiceManager modelServiceManager;
    private ShareResourceManagerService shareResourceManagerService;
    private MgObsService mgObsService;
    private IPluginBase pluginBase;
    private ResourceService resourceService;
    private ShareInnerService shareInnerService;
    private IMcpBase mcpBase;
    private SkillMapper skillMapper;
    private KnowledgeBaseServiceImpl knowledgeBaseService;
    private McpServiceDao serviceDao;

    private RelationManagementService relationManagementService;

    @BeforeEach
    void setUp() {
        releaseVersionMapper = mock(ReleaseVersionMapper.class);
        agentMapper = mock(AgentMapper.class);
        mappingMapper = mock(MappingMapper.class);
        workflowMapper = mock(WorkflowMapper.class);
        historyWorkflowMapper = mock(HistoryWorkflowMapper.class);
        pluginMapper = mock(PluginMapper.class);
        toolMapper = mock(ToolMapper.class);
        workspaceMapper = mock(WorkspaceMapper.class);
        memoryRepoMapper = mock(MemoryRepoMapper.class);
        pluginService = mock(IPlugin.class);
        modelServiceManager = mock(ModelServiceManager.class);
        shareResourceManagerService = mock(ShareResourceManagerService.class);
        mgObsService = mock(MgObsService.class);
        pluginBase = mock(IPluginBase.class);
        resourceService = mock(ResourceService.class);
        shareInnerService = mock(ShareInnerService.class);
        mcpBase = mock(IMcpBase.class);
        skillMapper = mock(SkillMapper.class);
        knowledgeBaseService = mock(KnowledgeBaseServiceImpl.class);
        serviceDao = mock(McpServiceDao.class);

        MockitoAnnotations.openMocks(this);
        relationManagementService = new RelationManagementService();
        ReflectionTestUtils.setField(relationManagementService, "releaseVersionMapper", releaseVersionMapper);
        ReflectionTestUtils.setField(relationManagementService, "agentMapper", agentMapper);
        ReflectionTestUtils.setField(relationManagementService, "mappingMapper", mappingMapper);
        ReflectionTestUtils.setField(relationManagementService, "workflowMapper", workflowMapper);
        ReflectionTestUtils.setField(relationManagementService, "historyWorkflowMapper", historyWorkflowMapper);
        ReflectionTestUtils.setField(relationManagementService, "pluginMapper", pluginMapper);
        ReflectionTestUtils.setField(relationManagementService, "toolMapper", toolMapper);
        ReflectionTestUtils.setField(relationManagementService, "workspaceMapper", workspaceMapper);
        ReflectionTestUtils.setField(relationManagementService, "memoryRepoMapper", memoryRepoMapper);
        ReflectionTestUtils.setField(relationManagementService, "pluginService", pluginService);
        ReflectionTestUtils.setField(relationManagementService, "modelServiceManager", modelServiceManager);
        ReflectionTestUtils.setField(relationManagementService, "shareResourceManagerService", shareResourceManagerService);
        ReflectionTestUtils.setField(relationManagementService, "mgObsService", mgObsService);
        ReflectionTestUtils.setField(relationManagementService, "pluginBase", pluginBase);
        ReflectionTestUtils.setField(relationManagementService, "resourceService", resourceService);
        ReflectionTestUtils.setField(relationManagementService, "shareInnerService", shareInnerService);
        ReflectionTestUtils.setField(relationManagementService, "mcpBase", mcpBase);
        ReflectionTestUtils.setField(relationManagementService, "skillMapper", skillMapper);
        ReflectionTestUtils.setField(relationManagementService, "knowledgeBaseService", knowledgeBaseService);
        ReflectionTestUtils.setField(relationManagementService, "serviceDao", serviceDao);
        ReflectionTestUtils.setField(relationManagementService, "toolBoundLimit", 20);
        ReflectionTestUtils.setField(relationManagementService, "knowledgeRepoBoundLimit", 3);
        ReflectionTestUtils.setField(relationManagementService, "workflowBoundLimit", 5);
        ReflectionTestUtils.setField(relationManagementService, "controllerWorkflowLimit", 5);
        ReflectionTestUtils.setField(relationManagementService, "controllerSubAgentLimit", 3);
        ReflectionTestUtils.setField(relationManagementService, "mcpBoundLimit", 5);
        ReflectionTestUtils.setField(relationManagementService, "defaultMcpIcon", "default-mcp-icon");
    }

    @Test
    void testDeleteAgentResourceMapping_Success() {
        assertDoesNotThrow(() ->
            relationManagementService.deleteAgentResourceMapping("agent-1", List.of("0.0.1")));
    }

    @Test
    void testListResourcesRelations_EmptyResult() {
        ListResourcesRelationsQo qo = new ListResourcesRelationsQo();
        qo.setResourceIds(List.of("resource-1"));
        qo.setResourceType("tool");
        qo.setWorkspaceId("w1");
        qo.setOffset(0);
        qo.setLimit(10);

        when(mappingMapper.getByIds(anyList())).thenReturn(Collections.emptyList());

        var result = relationManagementService.listResourcesRelations("p1", qo);

        assertNotNull(result);
    }

    @Test
    void testListAppRelations_EmptyResult() {
        ListAppRelationsQo qo = new ListAppRelationsQo();
        qo.setWorkspaceId("w1");
        qo.setResourceType("tool");

        var result = relationManagementService.listAppRelations("p1", "app-1", qo);

        assertNotNull(result);
    }

    @Test
    void testListResourceRelations_EmptyResult() {
        ListResourceRelationsQo qo = new ListResourceRelationsQo();
        qo.setWorkspaceId("w1");
        qo.setResourceType("tool");
        qo.setOffset(0);
        qo.setLimit(10);

        when(mappingMapper.selectByResourceIdAndVersionId(anyString(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        var result = relationManagementService.listResourceRelations("p1", "resource-1", qo);

        assertNotNull(result);
    }
}
