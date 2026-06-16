/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.manager.dto.ExportResourceParams;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceMgmtService;
import com.openjiuwen.studio.agent.manager.service.md.RouterStrategyMgmtService;
import com.openjiuwen.studio.agent.manager.workflow.resource.adapt.ResourceAdapterFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentExportServiceTest {

    private WorkflowMapper workflowMapper;
    private MappingMapper mappingMapper;
    private AgentMapper agentMapper;
    private MgObsService obsService;
    private ModelServiceMgmtService modelServiceMgmtService;
    private RouterStrategyMgmtService strategyMgmtService;
    private SkuManageService skuManageService;
    private ResourceAdapterFactory resourceAdapterFactory;
    private I18nUtil i18nUtil;

    private AgentExportService agentExportService;

    @BeforeEach
    void setUp() {
        workflowMapper = mock(WorkflowMapper.class);
        mappingMapper = mock(MappingMapper.class);
        agentMapper = mock(AgentMapper.class);
        obsService = mock(MgObsService.class);
        modelServiceMgmtService = mock(ModelServiceMgmtService.class);
        strategyMgmtService = mock(RouterStrategyMgmtService.class);
        skuManageService = mock(SkuManageService.class);
        resourceAdapterFactory = mock(ResourceAdapterFactory.class);
        i18nUtil = mock(I18nUtil.class);

        MockitoAnnotations.openMocks(this);
        agentExportService = new AgentExportService();
        ReflectionTestUtils.setField(agentExportService, "workflowMapper", workflowMapper);
        ReflectionTestUtils.setField(agentExportService, "mappingMapper", mappingMapper);
        ReflectionTestUtils.setField(agentExportService, "agentMapper", agentMapper);
        ReflectionTestUtils.setField(agentExportService, "obsService", obsService);
        ReflectionTestUtils.setField(agentExportService, "modelServiceMgmtService", modelServiceMgmtService);
        ReflectionTestUtils.setField(agentExportService, "strategyMgmtService", strategyMgmtService);
        ReflectionTestUtils.setField(agentExportService, "skuManageService", skuManageService);
        ReflectionTestUtils.setField(agentExportService, "resourceAdapterFactory", resourceAdapterFactory);
        ReflectionTestUtils.setField(agentExportService, "i18nUtil", i18nUtil);
        ReflectionTestUtils.setField(agentExportService, "jacksonObjectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(agentExportService, "envType", "public");
        ReflectionTestUtils.setField(agentExportService, "importMaxLen", 10);
    }

    @Test
    void testExportResource_ExceedsMaxLength() {
        ExportResourceParams params = new ExportResourceParams();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ids.add("id-" + i);
        }
        params.setResourceIds(ids);
        params.setResourceType("AGENT");

        assertThrows(AgentStudioException.class, () ->
            agentExportService.exportResource("p1", "w1", params));
    }

    @Test
    void testExportResource_UnsupportedResourceType() {
        ExportResourceParams params = new ExportResourceParams();
        params.setResourceIds(List.of("id-1"));
        params.setResourceType("UNKNOWN_TYPE");

        assertThrows(Exception.class, () ->
            agentExportService.exportResource("p1", "w1", params));
    }

    @Test
    void testExportResource_NullResourceIds() {
        ExportResourceParams params = new ExportResourceParams();
        params.setResourceIds(null);
        params.setResourceType("AGENT");

        assertThrows(Exception.class, () ->
            agentExportService.exportResource("p1", "w1", params));
    }

    @Test
    void testExportResource_EmptyResourceIds() {
        ExportResourceParams params = new ExportResourceParams();
        params.setResourceIds(List.of());
        params.setResourceType("WORKFLOW");

        assertThrows(Exception.class, () ->
            agentExportService.exportResource("p1", "w1", params));
    }
}
