/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.AgentExportEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ToolEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowExportEntity;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceData;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceMgmtService;
import com.openjiuwen.studio.agent.manager.service.plugin.IPlugin;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentImportExportServiceTest {

    @Mock
    private PluginMapper pluginMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private ModelServiceMapper modelServiceMapper;

    @Mock
    private ShareResourceManagerService shareResourceManagerService;

    @Mock
    private SkuManageService skuManageService;

    @Mock
    private ModelServiceMgmtService modelServiceMgmtService;

    private AgentImportExportService agentImportExportService;

    @BeforeEach
    void setUp() {
        agentImportExportService = new AgentImportExportService();
        ReflectionTestUtils.setField(agentImportExportService, "pluginMapper", pluginMapper);
        ReflectionTestUtils.setField(agentImportExportService, "workflowMapper", workflowMapper);
        ReflectionTestUtils.setField(agentImportExportService, "agentMapper", agentMapper);
        ReflectionTestUtils.setField(agentImportExportService, "modelServiceMapper", modelServiceMapper);
        ReflectionTestUtils.setField(agentImportExportService, "shareResourceManagerService", shareResourceManagerService);
        ReflectionTestUtils.setField(agentImportExportService, "skuManageService", skuManageService);
        ReflectionTestUtils.setField(agentImportExportService, "modelServiceMgmtService", modelServiceMgmtService);
        ReflectionTestUtils.setField(agentImportExportService, "opSvcProjectId", "op-svc-project");
        ReflectionTestUtils.setField(agentImportExportService, "defaultImportModelApi", "http://default-api");
        ReflectionTestUtils.setField(agentImportExportService, "defaultImportModelIcon", "default-icon");
        ReflectionTestUtils.setField(agentImportExportService, "importModelEnable", true);
    }

    @Test
    void testIsToolExistInWorkspace_ToolExists() {
        ToolEntity tool = new ToolEntity();
        tool.setToolId("tool-1");
        tool.setTraceId("trace-1");
        List<PluginEntity> existingPlugins = List.of(new PluginEntity());
        when(pluginMapper.selectByTraceIdAndWorkspaceId("proj-1", "ws-1", "trace-1"))
            .thenReturn(existingPlugins);

        Boolean result = agentImportExportService.isToolExistInWorkspace("proj-1", "ws-1", tool);

        assertTrue(result);
    }

    @Test
    void testIsToolExistInWorkspace_ToolNotExists() {
        ToolEntity tool = new ToolEntity();
        tool.setToolId("tool-1");
        tool.setTraceId("");
        when(pluginMapper.selectByTraceIdAndWorkspaceId("proj-1", "ws-1", "tool-1"))
            .thenReturn(Collections.emptyList());

        Boolean result = agentImportExportService.isToolExistInWorkspace("proj-1", "ws-1", tool);

        assertFalse(result);
    }

    @Test
    void testIsToolExistInWorkspace_BlankTraceIdUsesToolId() {
        ToolEntity tool = new ToolEntity();
        tool.setToolId("tool-id-fallback");
        tool.setTraceId(null);
        when(pluginMapper.selectByTraceIdAndWorkspaceId("proj-1", "ws-1", "tool-id-fallback"))
            .thenReturn(Collections.emptyList());

        Boolean result = agentImportExportService.isToolExistInWorkspace("proj-1", "ws-1", tool);

        assertFalse(result);
        assertEquals("tool-id-fallback", tool.getTraceId());
    }

    @Test
    void testIsPluginExistInWorkspace_PluginExists() {
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId("plugin-1");
        plugin.setTraceId("trace-p1");
        List<PluginEntity> existingPlugins = List.of(new PluginEntity());
        when(pluginMapper.selectByTraceIdAndWorkspaceId("proj-1", "ws-1", "trace-p1"))
            .thenReturn(existingPlugins);

        Boolean result = agentImportExportService.isPluginExistInWorkspace("proj-1", "ws-1", plugin);

        assertTrue(result);
    }

    @Test
    void testIsPluginExistInWorkspace_PluginNotExists() {
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId("plugin-1");
        plugin.setTraceId(null);
        when(pluginMapper.selectByTraceIdAndWorkspaceId("proj-1", "ws-1", "plugin-1"))
            .thenReturn(Collections.emptyList());

        Boolean result = agentImportExportService.isPluginExistInWorkspace("proj-1", "ws-1", plugin);

        assertFalse(result);
    }

    @Test
    void testHasModelServiceExist_NullModel() {
        boolean result = agentImportExportService.hasModelServiceExist(null, "proj-1", "ws-1");
        assertFalse(result);
    }

    @Test
    void testHasModelServiceExist_MatchingProjectAndWorkspace() {
        ModelServiceBase model = new ModelServiceBase();
        model.setProjectId("proj-1");
        model.setWorkspaceId("ws-1");

        boolean result = agentImportExportService.hasModelServiceExist(model, "proj-1", "ws-1");

        assertTrue(result);
    }

    @Test
    void testHasModelServiceExist_SystemProject() {
        ModelServiceBase model = new ModelServiceBase();
        model.setProjectId(CommonConstant.SYSTEM_DEFAULT);
        model.setWorkspaceId("ws-1");

        boolean result = agentImportExportService.hasModelServiceExist(model, "proj-1", "ws-1");

        assertTrue(result);
    }

    @Test
    void testHasModelServiceExist_SystemWorkspace() {
        ModelServiceBase model = new ModelServiceBase();
        model.setProjectId("proj-1");
        model.setWorkspaceId(CommonConstant.SYSTEM_DEFAULT);

        boolean result = agentImportExportService.hasModelServiceExist(model, "proj-1", "ws-1");

        assertTrue(result);
    }

    @Test
    void testHasModelServiceExist_DifferentProjectAndWorkspace() {
        ModelServiceBase model = new ModelServiceBase();
        model.setProjectId("other-proj");
        model.setWorkspaceId("other-ws");

        boolean result = agentImportExportService.hasModelServiceExist(model, "proj-1", "ws-1");

        assertFalse(result);
    }

    @Test
    void testBuildProviderReq_Success() {
        var result = agentImportExportService.buildProviderReq();

        assertNotNull(result);
        assertEquals(CommonConstant.ProviderParam.IMPORT_DEFAULT_PROVIDER_NAME, result.getProviderName());
        assertEquals(CommonConstant.ProviderParam.IMPORT_DEFAULT_PROVIDER_NAME, result.getProviderNameEn());
        assertEquals("{}", result.getAuthInfo());
        assertEquals(CommonConstant.ProviderParam.NO_AUTH, result.getAuthType());
        assertEquals("default-icon", result.getLogo());
        assertEquals("import default create", result.getDescription());
    }

    @Test
    void testBuildProviderReq_FieldsNotNull() {
        var result = agentImportExportService.buildProviderReq();

        assertNotNull(result.getProviderName());
        assertNotNull(result.getAuthInfo());
        assertNotNull(result.getAuthType());
    }

    @Test
    void testIsOwnSharePlugin_NoShareResource() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            when(shareResourceManagerService.queryShareResourceEntityByResourceId("tool-1"))
                .thenReturn(null);

            boolean result = agentImportExportService.isOwnSharePlugin("tool-1");

            assertTrue(result);
        }
    }

    @Test
    void testIsOwnSharePlugin_SameWorkspace() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            ShareResourceEntity shareEntity = new ShareResourceEntity();
            shareEntity.setWorkspaceId("ws-1");
            when(shareResourceManagerService.queryShareResourceEntityByResourceId("tool-1"))
                .thenReturn(shareEntity);

            boolean result = agentImportExportService.isOwnSharePlugin("tool-1");

            assertTrue(result);
        }
    }

    @Test
    void testIsOwnSharePlugin_DifferentWorkspace() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            ShareResourceEntity shareEntity = new ShareResourceEntity();
            shareEntity.setWorkspaceId("ws-other");
            when(shareResourceManagerService.queryShareResourceEntityByResourceId("tool-1"))
                .thenReturn(shareEntity);

            boolean result = agentImportExportService.isOwnSharePlugin("tool-1");

            assertFalse(result);
        }
    }

    @Test
    void testIsOwnSharePlugin_ToolIdWithHash() {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestWorkspaceId).thenReturn("ws-1");
            when(shareResourceManagerService.queryShareResourceEntityByResourceId("tool-abc"))
                .thenReturn(null);

            boolean result = agentImportExportService.isOwnSharePlugin("tool-abc#extra");

            assertTrue(result);
        }
    }

    @Test
    void testGetValidatedModels_EmptyModelIds() {
        List<ModelServiceData> result = agentImportExportService.getValidatedModels("proj-1", "ws-1", Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetValidatedModels_NullModelIds() {
        List<ModelServiceData> result = agentImportExportService.getValidatedModels("proj-1", "ws-1", null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetValidatedModels_ModelsFound() {
        List<String> modelIds = List.of("m1", "m2");
        List<ModelServiceData> models = List.of(new ModelServiceData(), new ModelServiceData());
        when(modelServiceMapper.queryByIds(modelIds, "proj-1", "ws-1")).thenReturn(models);

        List<ModelServiceData> result = agentImportExportService.getValidatedModels("proj-1", "ws-1", modelIds);

        assertEquals(2, result.size());
    }

    @Test
    void testGetValidatedModels_ModelsNotFound() {
        List<String> modelIds = List.of("m1");
        when(modelServiceMapper.queryByIds(modelIds, "proj-1", "ws-1")).thenReturn(Collections.emptyList());

        List<ModelServiceData> result = agentImportExportService.getValidatedModels("proj-1", "ws-1", modelIds);

        assertTrue(result.isEmpty());
    }
}
