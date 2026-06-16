/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.AppInfo;
import com.openjiuwen.studio.agent.manager.dto.AppListRsp;
import com.openjiuwen.studio.agent.manager.dto.AutoAddResultJsonObject;
import com.openjiuwen.studio.agent.manager.dto.ListAppsQo;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;
import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;
import com.openjiuwen.studio.agent.manager.dto.WorkflowInfo;
import com.openjiuwen.studio.agent.manager.entity.App;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.AgentVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.AppMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.models.AskModelReq;
import com.openjiuwen.studio.agent.manager.rce.service.JiuWenService;
import com.openjiuwen.studio.agent.manager.service.asset.AssetFreeTrialMgmtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AppManagementServiceTest {

    @Mock
    private AppMapper appMapper;

    @Mock
    private AgentCommonService agentCommonService;

    @Mock
    private JiuWenService jiuWenService;

    @Mock
    private MgObsService obsService;

    @Mock
    private IrAdapterService irAdapterService;

    @Mock
    private RelationManagementService relationManagementService;

    @Mock
    private AgentImportExportService agentImportExportService;

    @Mock
    private WorkflowValidationService workflowValidationService;

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private AgentVersionMapper agentVersionMapper;

    @Mock
    private MappingMapper mappingMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private AgentManagementService agentManagementService;

    @Mock
    private AssetFreeTrialMgmtService assetFreeTrialMgmtService;

    @Mock
    private SkuManageService skuManageService;

    @Mock
    private WorkflowManagementService workflowManagementService;

    private AppManagementService appManagementService;

    @BeforeEach
    void setUp() {
        appManagementService = new AppManagementService(appMapper, agentCommonService, jiuWenService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "obsService", obsService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "irAdapterService", irAdapterService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "relationManagementService", relationManagementService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "agentImportExportService", agentImportExportService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "workflowValidationService", workflowValidationService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "agentMapper", agentMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "workflowMapper", workflowMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "agentVersionMapper", agentVersionMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "mappingMapper", mappingMapper);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "messageSource", messageSource);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "agentManagementService", agentManagementService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "assetFreeTrialMgmtService", assetFreeTrialMgmtService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "skuManageService", skuManageService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "workflowManagementService", workflowManagementService);
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "contentReviewConfigs", "{}");
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "selectedResourceIds", "");
        org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "freeTrialQuotaLimit", 10);
    }

    @Test
    void testListApps_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class);
             MockedStatic<LanguageUtils> langUtils = mockStatic(LanguageUtils.class)) {

            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");
            langUtils.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.CHINESE);

            ListAppsQo query = new ListAppsQo();
            query.setOffset(0);
            query.setLimit(10);
            query.setSelected(false);

            App app = new App();
            app.setAppId("app-1");
            app.setResourceId("res-1");
            List<App> appList = List.of(app);

            when(appMapper.selectBySearchCriteria(any(SearchCriteria.class))).thenReturn(appList);
            when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Official");

            Map<String, Integer> quotaMap = new HashMap<>();
            quotaMap.put("res-1", 5);
            when(assetFreeTrialMgmtService.queryFreeTrialUsageQuota(any(List.class), anyString())).thenReturn(quotaMap);

            AppListRsp result = appManagementService.listApps("proj-1", query);
            assertNotNull(result);
            assertNotNull(result.getAppList());
        }
    }

    @Test
    void testListApps_WithSelectedResourceIds() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class);
             MockedStatic<LanguageUtils> langUtils = mockStatic(LanguageUtils.class)) {

            reqCtx.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain-1");
            langUtils.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.CHINESE);

            org.springframework.test.util.ReflectionTestUtils.setField(appManagementService, "selectedResourceIds", "res-1,res-2");

            ListAppsQo query = new ListAppsQo();
            query.setOffset(0);
            query.setLimit(10);
            query.setSelected(true);

            App app = new App();
            app.setAppId("app-1");
            app.setResourceId("res-1");
            when(appMapper.selectByResourceIds(any())).thenReturn(List.of(app));
            when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenReturn("Official");
            when(assetFreeTrialMgmtService.queryFreeTrialUsageQuota(any(List.class), anyString())).thenReturn(new HashMap<>());

            AppListRsp result = appManagementService.listApps("proj-1", query);
            assertNotNull(result);
        }
    }

    @Test
    void testCopyApp_NullSourceType_ThrowsException() {
        SearchCriteria body = new SearchCriteria();
        body.setSource(null);

        assertThrows(AgentStudioException.class,
            () -> appManagementService.copyApp("proj-1", "app-1", "ws-1", "ws-2", body));
    }

    @Test
    void testCopyApp_WorkflowType_Success() {
        SearchCriteria body = new SearchCriteria();
        body.setSource("workflow");

        WorkflowInfo workflowInfo = new WorkflowInfo();
        workflowInfo.setWorkflowId("wf-new");
        when(workflowManagementService.copyWorkflow(anyString(), anyString(), anyString(), any(Boolean.class)))
            .thenReturn(workflowInfo);

        AppInfo result = appManagementService.copyApp("proj-1", "app-1", "ws-1", "ws-2", body);
        assertNotNull(result);
        assertEquals("wf-new", result.getResourceId());
    }

    @Test
    void testGeneratePrologue_Success() {
        try (MockedStatic<LanguageUtils> langUtils = mockStatic(LanguageUtils.class)) {
            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(AskModelReq.builder().build());
            when(jiuWenService.callModel(any(), anyString())).thenReturn("Hello, I am an assistant.");

            AutoAddResultJsonObject result = appManagementService.generatePrologue(
                "deploy-1", "type-1", "model-1", new ModelConfig(), "query", "ws-1");
            assertNotNull(result);
            assertEquals("Hello, I am an assistant.", result.getPrologue());
        }
    }

    @Test
    void testGeneratePrologue_BlankResponse_ReturnsDefault() {
        try (MockedStatic<LanguageUtils> langUtils = mockStatic(LanguageUtils.class)) {
            langUtils.when(LanguageUtils::isChinese).thenReturn(true);

            when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(AskModelReq.builder().build());
            when(jiuWenService.callModel(any(), anyString())).thenReturn("");

            AutoAddResultJsonObject result = appManagementService.generatePrologue(
                "deploy-1", "type-1", "model-1", new ModelConfig(), "query", "ws-1");
            assertNotNull(result);
            assertNotNull(result.getPrologue());
        }
    }

    @Test
    void testGeneratePrologue_ExceptionThrown() {
        when(agentCommonService.buildAskModelReq(anyString(), anyString(), anyString(), any(), anyString()))
            .thenReturn(AskModelReq.builder().build());
        when(jiuWenService.callModel(any(), anyString())).thenThrow(new RuntimeException("model error"));

        assertThrows(AgentStudioException.class,
            () -> appManagementService.generatePrologue("deploy-1", "type-1", "model-1", new ModelConfig(), "query", "ws-1"));
    }
}
