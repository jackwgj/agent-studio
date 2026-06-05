/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.bo.WfImportDataWrapper;
import com.openjiuwen.studio.agent.manager.dto.QueryShareResourceInfoByResourceIdQo;
import com.openjiuwen.studio.agent.manager.dto.QuerySharedResourceListQo;
import com.openjiuwen.studio.agent.manager.dto.ResourceVersionInfo;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceRequestInfo;
import com.openjiuwen.studio.agent.manager.dto.ShareResourceResp;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.MappingEntity;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.entity.ShareInfo;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareScopeEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.enums.relation.ReferenceTypeEnum;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareResourceMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareScopeMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;
import com.openjiuwen.studio.agent.manager.service.plugin.IPlugin;
import com.openjiuwen.studio.prompt.engineering.entity.PePromptTemplate;
import com.openjiuwen.studio.prompt.engineering.mapper.PePromptTemplateMapper;

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

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class ShareResourceManagerServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareResourceMapper shareResourceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareScopeMapper shareScopeMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentMapper agentMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowMapper workflowMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PluginMapper pluginMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReleaseVersionMapper releaseVersionMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkspaceMapper workspaceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MappingMapper mappingMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IPlugin pluginAdapter;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServiceDao mcpServiceDao;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PePromptTemplateMapper pePromptTemplateMapper;

    @InjectMocks
    private ShareResourceManagerService shareResourceManagerService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_createOrUpdateResourceShare_should_return_true() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserDomainId())
                .thenReturn("test");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("test");

            WorkflowEntity workflowEntity = new WorkflowEntity();
            workflowEntity.setTraceId("test");
            when(workflowMapper.getWorkflowEntityByWorkspaceId(anyString(), anyString(), anyString())).thenReturn(
                workflowEntity);

            Agent agent = new Agent();
            agent.setTraceId("test");
            when(agentMapper.selectByProjectIdAndWorkspaceId(anyString(), anyString(), anyString())).thenReturn(agent);

            PluginEntity pluginEntity = new PluginEntity();
            pluginEntity.setTraceId("test");
            when(pluginMapper.selectByPrimaryKeyAndWorkspace(anyString(), anyString(), anyString())).thenReturn(
                pluginEntity);

            when(shareResourceMapper.insert(any(ShareResourceEntity.class))).thenReturn(0);
            when(shareResourceMapper.updateShareResourceEntity(any(ShareResourceEntity.class))).thenReturn(0);
            ShareResourceEntity shareResourceOld = new ShareResourceEntity();
            when(shareResourceMapper.selectShareResourceEntityById(anyString(), anyString(), anyString())).thenReturn(
                shareResourceOld);

            List<MappingEntity> mappingEntities = new ArrayList<>();
            MappingEntity mappingEntity = new MappingEntity();
            mappingEntity.setResourceType("workflow");
            mappingEntity.setReferenceType(ReferenceTypeEnum.SHARE.getValue());
            mappingEntity.setResourceId("123456");
            mappingEntity.setResourceVersion("123");
            mappingEntity.setAppVersion("1234");
            mappingEntities.add(mappingEntity);
            when(mappingMapper.selectByAppIdAndAppVersion(anyString(), eq(null), eq(null), eq(null))).thenReturn(
                mappingEntities);

            when(shareScopeMapper.batchInsert(anyList())).thenReturn(0);
            List<ShareScopeEntity> shareScopeEntitiesOld = new ArrayList<>();
            ShareScopeEntity shareScopeEntity = new ShareScopeEntity();
            shareScopeEntitiesOld.add(shareScopeEntity);
            when(shareScopeMapper.selectShareScopesByResourceId(anyString())).thenReturn(shareScopeEntitiesOld);
            when(shareScopeMapper.batchDelete(anyString(), anyList())).thenReturn(0);

            List<WorkspaceEntity> workspaceEntityList = new ArrayList<>();
            WorkspaceEntity workspaceEntity = new WorkspaceEntity();
            workspaceEntity.setId("123");
            workspaceEntityList.add(workspaceEntity);
            when(workspaceMapper.getWorkspaceByTenantId(anyString())).thenReturn(workspaceEntityList);

            List<ReleaseVersion> versionList = new ArrayList<>();
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setVersionId("123");
            versionList.add(releaseVersion);
            when(releaseVersionMapper.selectByAppId(anyString())).thenReturn(versionList);

            ShareResourceRequestInfo body = new ShareResourceRequestInfo();
            body.setResourceId("123456");
            body.setResourceType(ShareResourceRequestInfo.ResourceTypeEnum.WORKFLOW);
            body.setAuthWorkspaceIdList(List.of("123"));
            body.setVersionList(List.of("123"));

            // When
            Boolean result = shareResourceManagerService.createOrUpdateShareResource("test", "test",
                body);

            // Then
            assertTrue(result);

            mappingEntity.setResourceType("agent");
            body.setResourceType(ShareResourceRequestInfo.ResourceTypeEnum.CONTROLLER);

            result = shareResourceManagerService.createOrUpdateShareResource("test", "test",
                body);

            // Then
            assertTrue(result);

            mappingEntity.setResourceType("plugin");
            body.setResourceType(ShareResourceRequestInfo.ResourceTypeEnum.PLUGIN);

            result = shareResourceManagerService.createOrUpdateShareResource("test", "test",
                body);

            // Then
            assertTrue(result);


            mappingEntity.setResourceType("mcp");
            body.setResourceType(ShareResourceRequestInfo.ResourceTypeEnum.MCP);

            result = shareResourceManagerService.createOrUpdateShareResource("test", "test",
                body);

            // Then
            assertTrue(result);

            mappingEntity.setResourceType("prompt");
            body.setResourceType(ShareResourceRequestInfo.ResourceTypeEnum.PROMPT);

            result = shareResourceManagerService.createOrUpdateShareResource("test", "test",
                body);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_getResourceShareInfo_should_return_not_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            List<ShareScopeEntity> shareScopeEntities = new ArrayList<>();
            ShareScopeEntity shareScopeEntity = new ShareScopeEntity();
            shareScopeEntity.setWorkspaceId("workspace_002");

            ShareScopeEntity shareScopeEntity1 = new ShareScopeEntity();
            shareScopeEntity1.setWorkspaceId("workspace_003");
            shareScopeEntities.add(shareScopeEntity1);
            when(shareScopeMapper.selectShareScopesByResourceId("resource_001")).thenReturn(shareScopeEntities);

            ShareResourceEntity shareResourceEntity = new ShareResourceEntity();
            shareResourceEntity.setWorkspaceId("workspace_001");
            List<ResourceVersionInfo> resourceVersionList = new ArrayList<>();
            ResourceVersionInfo resourceVersionInfo = new ResourceVersionInfo();
            resourceVersionInfo.setVersionId("123");
            resourceVersionList.add(resourceVersionInfo);
            shareResourceEntity.setVersionList(JSONObject.toJSONString(resourceVersionList));
            when(shareResourceMapper.selectShareResourceEntityByResourceId(anyString())).thenReturn(
                shareResourceEntity);
            when(shareResourceMapper.selectShareResourceEntityById(anyString(), anyString(), anyString())).thenReturn(
                shareResourceEntity);

            when(shareScopeMapper.selectShareScopesByResourceId("resource_001")).thenReturn(shareScopeEntities);

            when(mgObsService.downloadObsFile(anyString()))
                .thenReturn(null);

            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("workspace_001");
            QueryShareResourceInfoByResourceIdQo getResourceShareInfoQo = new QueryShareResourceInfoByResourceIdQo();
            getResourceShareInfoQo.setWorkspaceId("workspace_002");
            ShareResourceResp result = shareResourceManagerService.queryShareResourceInfoByResourceId(
                "project_001", "resource_001", getResourceShareInfoQo);

            assertNotNull(result);
        }
    }

    @Test
    void test_getResourceShareInfo_should_throw_exception() throws Exception {
        assertThrows(AgentStudioException.class, () -> {
            String projectId = "project_001";
            String resourceId = "resource_001";

            List<ShareScopeEntity> shareScopeEntities = new ArrayList<>();
            ShareScopeEntity shareScopeEntity1 = new ShareScopeEntity();
            shareScopeEntity1.setWorkspaceId("workspace_001");

            ShareScopeEntity shareScopeEntity2 = new ShareScopeEntity();
            shareScopeEntity2.setWorkspaceId("workspace_002");
            shareScopeEntities.add(shareScopeEntity1);
            shareScopeEntities.add(shareScopeEntity2);
            when(shareScopeMapper.selectShareScopesByResourceId(anyString())).thenReturn(shareScopeEntities);

            ShareResourceEntity shareResourceEntity = new ShareResourceEntity();
            shareResourceEntity.setResourceId(resourceId);
            shareResourceEntity.setWorkspaceId("workspace_003");
            when(shareResourceMapper.selectShareResourceEntityByResourceId(resourceId)).thenReturn(
                shareResourceEntity);
            when(shareResourceMapper.selectShareResourceEntityById(anyString(), anyString(), anyString())).thenReturn(
                shareResourceEntity);

            when(mgObsService.downloadObsFile(anyString()))
                .thenReturn(null);

            QueryShareResourceInfoByResourceIdQo getResourceShareInfoQo = new QueryShareResourceInfoByResourceIdQo();
            getResourceShareInfoQo.setWorkspaceId("workspace_004");
            ShareResourceResp result = shareResourceManagerService.queryShareResourceInfoByResourceId(
                projectId, resourceId, getResourceShareInfoQo);
        });
    }

    @Test
    void test_listShareResources_should_return_not_null() throws Exception {
        // Given
        List<WorkflowEntity> workflows = new ArrayList<>();
        WorkflowEntity workflowEntity = new WorkflowEntity();
        workflowEntity.setId("test");
        workflowEntity.setName("test");
        workflowEntity.setCode("test");
        workflowEntity.setDescription("test");
        workflowEntity.setAvatar("test");
        workflows.add(workflowEntity);
        when(workflowMapper.selectByWorkflowIds(anyString(), eq(null), anyList())).thenReturn(workflows);

        List<PluginEntity> plugins = new ArrayList<>();
        PluginEntity pluginEntity = new PluginEntity();
        pluginEntity.setPluginId("test");
        pluginEntity.setPluginDisplayName("test");
        pluginEntity.setPluginChineseName("test");
        pluginEntity.setPluginDesc("test");
        pluginEntity.setIcon("test");
        pluginEntity.setRequestInfo(JSONObject.toJSONString(workflowEntity));
        plugins.add(pluginEntity);
        when(pluginMapper.selectByIds(anyList())).thenReturn(plugins);

        List<ShareResourceEntity> shareResourceEntities = new ArrayList<>();
        ShareResourceEntity shareResourceEntity = new ShareResourceEntity();
        shareResourceEntity.setResourceId("test");
        shareResourceEntities.add(shareResourceEntity);
        when(shareResourceMapper.selectSharedResourceByAuthWorkspaceIdAndType(any(), any(), any(), any())).thenReturn(
            shareResourceEntities);

        List<Agent> agents = new ArrayList<>();
        Agent agent = new Agent();
        agent.setAgentId("test");
        agent.setName("test");
        agent.setDescription("test");
        agent.setIcon("test");
        agents.add(agent);
        when(agentMapper.selectByAgentIds(anyString(), anyString(), anyList())).thenReturn(agents);

        QuerySharedResourceListQo listShareResourcesQo = new QuerySharedResourceListQo();
        listShareResourcesQo.setResourceType("workflow");
        listShareResourcesQo.setWorkspaceId("test");

        // When
        ShareResourceListRsp result = shareResourceManagerService.querySharedResourceList("test",
            listShareResourcesQo);

        assertNotNull(result);

        listShareResourcesQo.setResourceType("controller");

        result = shareResourceManagerService.querySharedResourceList("test",
            listShareResourcesQo);

        // Then
        assertNotNull(result);

        listShareResourcesQo.setResourceType("plugin");

        result = shareResourceManagerService.querySharedResourceList("test",
            listShareResourcesQo);

        // Then
        assertNotNull(result);

        listShareResourcesQo.setResourceType("mcp");
        List<McpServiceEntity> mcpServiceEntities = new ArrayList<>();
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        mcpServiceEntity.setId("test");
        mcpServiceEntities.add(mcpServiceEntity);
        when(mcpServiceDao.listMcpServiceByIdAndProjectId(any(), any())).thenReturn(mcpServiceEntities);

        result = shareResourceManagerService.querySharedResourceList("test",
            listShareResourcesQo);

        // Then
        assertNotNull(result);


        listShareResourcesQo.setResourceType("prompt");
        List<PePromptTemplate> pePromptTemplates = new ArrayList<>();
        PePromptTemplate pePromptTemplate = new PePromptTemplate();
        pePromptTemplate.setId("test");
        pePromptTemplates.add(pePromptTemplate);
        when(pePromptTemplateMapper.batchQueryTemplateByIds(any(), anyString())).thenReturn(pePromptTemplates);

        result = shareResourceManagerService.querySharedResourceList("test",
            listShareResourcesQo);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_cancelResourceShare_should_return_true() throws Exception {
        // Given
        when(shareResourceMapper.deleteByPrimaryKey(anyString())).thenReturn(0);
        ShareResourceEntity shareResource = new ShareResourceEntity();
        when(shareResourceMapper.selectShareResourceEntityById(anyString(), anyString(), anyString())).thenReturn(
            shareResource);

        List<MappingEntity> mappingEntities = new ArrayList<>();
        MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setReferenceType("direct");
        mappingEntities.add(mappingEntity);
        when(mappingMapper.selectByResourceId(anyString())).thenReturn(mappingEntities);

        when(shareScopeMapper.deleteByResourceId(anyString())).thenReturn(0);

        // When
        Boolean result = shareResourceManagerService.cancelShareResource("test", "test", "test",
            "test");

        // Then
        assertTrue(result);
    }

    @Test
    void test_isListIncluded_should_return_true() throws Exception {
        // Given
        List<String> source = new ArrayList<>();
        source.add("test");

        List<String> target = new ArrayList<>();
        target.add("test");

        // When
        boolean result = shareResourceManagerService.isListIncluded(source, target);

        // Then
        assertTrue(result);
    }

    @Test
    void test_importShareInfo_should_throw_exception() throws Exception {
        assertThrows(AgentStudioException.class, () -> {
            List<MappingEntity> importMappingEntityList = buildMappingEntityList();

            String resourceId = "subWorkflow_001";
            String resourceWorkspace = "workspace_003";

            ShareResourceEntity existedShareResourceEntity = buildExistedShareResourceEntity(resourceId,
                resourceWorkspace);

            when(shareResourceMapper.selectShareResourceEntityByResourceId("subWorkflow_001")).thenReturn(
                existedShareResourceEntity);

            List<ShareScopeEntity> shareScopeEntities = buildShareScopeEntityList();

            when(shareScopeMapper.selectShareScopesByResourceId(resourceId)).thenReturn(shareScopeEntities);

            when(pluginAdapter.getPluginId(anyString())).thenReturn(resourceId);

            shareResourceManagerService.validateShareReferences(importMappingEntityList, "workspace_002");
        });
    }

    @Test
    void test_importShareInfo_should_not_throw_exception() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            String projectId = "project_001";
            String workspaceId = "workspace_001";
            String resourceId = "resource_001";
            String domainId = "domain_001";

            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain_001");

            List<WorkspaceEntity> workspaceEntityList = new ArrayList<>();
            WorkspaceEntity workspaceEntity = new WorkspaceEntity();
            workspaceEntity.setId("workspace_001");
            workspaceEntityList.add(workspaceEntity);

            when(workspaceMapper.selectWorkspaceListByDomainId(projectId, domainId)).thenReturn(workspaceEntityList);

            ShareScopeEntity existedShareScopeEntity = new ShareScopeEntity();
            existedShareScopeEntity.setWorkspaceId("workspace_003");
            ArrayList<ShareScopeEntity> existedShareScopeEntityList = new ArrayList<>();
            existedShareScopeEntityList.add(existedShareScopeEntity);

            when(shareScopeMapper.insert(any(ShareScopeEntity.class))).thenReturn(0);

            when(shareResourceMapper.insert(any(ShareResourceEntity.class))).thenReturn(0);
            when(shareScopeMapper.selectShareScopesByResourceId(resourceId)).thenReturn(existedShareScopeEntityList);

            ShareInfo importShareInfo = new ShareInfo();
            importShareInfo.setResourceId(resourceId);
            List<ShareScopeEntity> importShareScopeList = new ArrayList<>();
            ShareScopeEntity shareScopeEntity = new ShareScopeEntity();
            shareScopeEntity.setWorkspaceId(workspaceId);
            importShareScopeList.add(shareScopeEntity);
            importShareInfo.setShareScopeEntityList(importShareScopeList);
            WfImportDataWrapper wfImportDataWrapper = new WfImportDataWrapper();
            shareResourceManagerService.importShareInfo(projectId, importShareInfo, wfImportDataWrapper);
        }
    }

    private List<MappingEntity> buildMappingEntityList() {
        ArrayList<MappingEntity> mappingEntityList = new ArrayList<>();

        MappingEntity mappingEntity = new MappingEntity();
        mappingEntity.setMappingId("1");
        mappingEntity.setAppId("workflow_001");
        mappingEntity.setAppVersion("v1.0");
        mappingEntity.setAppType("workflow");
        mappingEntity.setAppName("workflow_test");
        mappingEntity.setResourceId("subWorkflow_001");
        mappingEntity.setResourceType("workflow");
        mappingEntity.setResourceName("subWorkflow_test");
        mappingEntity.setResourceVersion("v1.1");
        mappingEntity.setReferenceType(ReferenceTypeEnum.SHARE.getValue());

        mappingEntityList.add(mappingEntity);

        return mappingEntityList;
    }

    private ShareResourceEntity buildExistedShareResourceEntity(String resourceId, String workspaceId) {
        ShareResourceEntity shareResourceEntity = new ShareResourceEntity();
        shareResourceEntity.setResourceId(resourceId);
        shareResourceEntity.setTraceId(resourceId);
        shareResourceEntity.setResourceType("workflow");
        shareResourceEntity.setWorkspaceId(workspaceId);
        return shareResourceEntity;
    }

    private List<ShareScopeEntity> buildShareScopeEntityList() {
        List<ShareScopeEntity> shareScopeEntityList = new ArrayList<>();

        ShareScopeEntity shareScopeEntity = new ShareScopeEntity();
        shareScopeEntity.setResourceId("subWorkflow_001");
        shareScopeEntity.setId("1");
        shareScopeEntity.setProjectId("project_001");
        shareScopeEntity.setResourceType("workflow");
        shareScopeEntity.setTenantId("domain_001");
        shareScopeEntityList.add(shareScopeEntity);
        return shareScopeEntityList;
    }

}
