/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.UrlCheckUtils;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthInfoReq;
import com.openjiuwen.studio.agent.manager.entity.ProviderExportMetadata;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthMetadataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.UserModelServiceProviderMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;

public class ProviderMgmtServiceTest {
    private ProviderMgmtService mgmtService;
    @Mock
    private ProviderAuthDataMapper authMapper;



    @Mock
    private UserModelServiceProviderMapper userModelServiceProviderMapper;

    @Mock
    private UserModelServiceProviderMapper userProviderMapper;

    @Mock
    private ProviderAuthService authService;

    @Mock
    private ProviderAuthMetadataMapper metadataMapper;

    @Mock
    private UrlCheckUtils urlCheckUtils;

    @Mock
    private ModelLicenseCtrlService licenseCtrlService;

    @BeforeEach
    public void beforeEach() {
        mgmtService = new ProviderMgmtService();

        MockitoAnnotations.openMocks(this); // 初始化 @Mock 标注的对象
        ReflectionTestUtils.setField(mgmtService, "licenseCtrlService", licenseCtrlService);
        // 强制把 mock 的 authMapper 注入到 service 实例的 "authMapper" 字段中
        ReflectionTestUtils.setField(mgmtService, "authMapper", authMapper);
        ReflectionTestUtils.setField(mgmtService, "authService", authService);
        ReflectionTestUtils.setField(mgmtService, "userModelServiceProviderMapper", userModelServiceProviderMapper);
        ReflectionTestUtils.setField(mgmtService, "metadataMapper", metadataMapper);
        ReflectionTestUtils.setField(mgmtService, "urlCheckUtils", urlCheckUtils);
        ReflectionTestUtils.setField(mgmtService, "userProviderMapper", userProviderMapper);
    }

    @Test
    public void queryAndAuthCheckExceptionTest() {
        UserModelServiceProviderMapper mapper = Mockito.mock(UserModelServiceProviderMapper.class);
        ReflectionTestUtils.setField(mgmtService, "userModelServiceProviderMapper", mapper);

        String workspace = "default";
        String project = "project";

        when(mapper.selectById("null")).thenReturn(null);
        try {
            mgmtService.queryAndAuthCheck(project, workspace, "null", true);
            Assertions.fail();
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_PROVIDER_NOT_EXIST, e.getErrorCode());
        }

        ModelServiceProvider provider = new ModelServiceProvider().setProjectId("test_project")
            .setWorkspaceId("test_workspace");
        when(mapper.selectById("id")).thenReturn(provider);
        try {
            mgmtService.queryAndAuthCheck(project, workspace, "id", true);
            Assertions.fail();
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_PROVIDER_NOT_EXIST, e.getErrorCode());
        }

        try {
            provider = new ModelServiceProvider().setProjectId(project).setWorkspaceId("test_workspace");
            when(mapper.selectById("id")).thenReturn(provider);
            mgmtService.queryAndAuthCheck(project, workspace, "id", true);
            Assertions.fail();
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_PROVIDER_NOT_EXIST, e.getErrorCode());
        }

        try {
            provider = new ModelServiceProvider().setProjectId(project).setWorkspaceId("test_workspace");
            when(mapper.selectById("id")).thenReturn(provider);
            mgmtService.queryAndAuthCheck(project, workspace, "id", true);
            Assertions.fail();
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_PROVIDER_NOT_EXIST, e.getErrorCode());
        }

        try {
            provider = new ModelServiceProvider().setProjectId(project).setWorkspaceId("SYSTEM");
            when(mapper.selectById("id")).thenReturn(provider);
            mgmtService.queryAndAuthCheck(project, workspace, "id", false);
            Assertions.fail();
        } catch (AgentStudioException e) {
            assertEquals(StudioError.MD_PROVIDER_NOT_EXIST, e.getErrorCode());
        }
    }

    @Test
    public void queryAndAuthCheckTest() {
        UserModelServiceProviderMapper mapper = Mockito.mock(UserModelServiceProviderMapper.class);
        ReflectionTestUtils.setField(mgmtService, "userModelServiceProviderMapper", mapper);
        
        String workspace = "default";
        String project = "project";

        ModelServiceProvider provider = new ModelServiceProvider().setProjectId(project).setWorkspaceId(workspace);
        when(mapper.selectById("id")).thenReturn(provider);
        ModelServiceProvider ans = mgmtService.queryAndAuthCheck(project, workspace, "id", true);
        assertEquals(ans, provider);

        provider = new ModelServiceProvider().setProjectId(project).setWorkspaceId("SYSTEM");
        when(mapper.selectById("id")).thenReturn(provider);
        ans = mgmtService.queryAndAuthCheck(project, workspace, "id", true);
        assertEquals(ans, provider);
    }
    @Test
    void testUpdateModelServiceProviderAuthInfo_FullCoverage() {
        // 1. 准备基础 Mock 数据
        String projectId = "p1";
        String workspaceId = "w1";
        String id = "id1";

        // 解决 539/550 行的 NPE：确保 providerAuthData 有值
        ProviderAuthData mockData = new ProviderAuthData();
        mockData.setId(id);

        // 解决 543 行的 NPE：创建一个 mock 的 Auth 配置对象
        ProviderAuth mockAuthCfg = mock(ProviderAuth.class);

        // 2. 包装 Service 为 Spy，接管内部方法 queryAuthInfoAndCheck (解决 Mapper 为空的问题)
        ProviderMgmtService serviceSpy = spy( mgmtService);
        doReturn(mockData).when(serviceSpy).queryAuthInfoAndCheck(any(), any(), any());

        // --- 路径 1: 覆盖 541(假) 和 545(真) ---
        // 逻辑：authInfo 为空 -> authCfg 为 null -> 547 行返回
        ProviderAuthInfoReq body = new ProviderAuthInfoReq();
        body.setAuthInfo("");

        serviceSpy.updateModelServiceProviderAuthInfo(projectId, workspaceId, id, true, body);
        verify(authService, never()).updateAuthInfo(any());

        // --- 路径 2: 覆盖 541(真), 545(假), 549(真) ---
        // 逻辑：authInfo 有值 -> authCfg 不为 null -> availableCheck 为 true
        reset(authService);
        body.setAuthInfo("some-info");
        body.setAuthType("SOME_TYPE");

        // 核心：必须 Mock 这个调用，否则 543 行 authCfg.validCheck() 会报 NPE
        when(authService.getProviderAuth(anyString(), anyString())).thenReturn(mockAuthCfg);

        serviceSpy.updateModelServiceProviderAuthInfo(projectId, workspaceId, id, true, body);

        verify(mockAuthCfg, times(1)).validCheck(); // 验证校验被触发
        verify(authService, times(1)).updateAuthInfo(mockData); // 验证最终更新被触发

        // --- 路径 3: 覆盖 549(假) ---
        // 逻辑：availableCheck 为 false -> 跳过 550 行日志打印
        reset(authService);
        // 这里依然需要 mockAuthCfg 保持非空，否则会卡在 545 行提前返回
        when(authService.getProviderAuth(anyString(), anyString())).thenReturn(mockAuthCfg);
        serviceSpy.updateModelServiceProviderAuthInfo(projectId, workspaceId, id, false, body);

        verify(authService, times(1)).updateAuthInfo(mockData);
    }
    @Test
    void testQueryAuthInfoAndCheck_AllBranches() {
        String pId = "p1";
        String wId = "w1";
        String id = "id1";

        when(authMapper.queryByProviderId(pId, wId, id)).thenReturn(null);
        assertThrows(AgentStudioException.class, () -> {
            mgmtService.queryAuthInfoAndCheck(pId, wId, id);
        });

        ProviderAuthData matchData = new ProviderAuthData();
        matchData.setWorkspaceId(wId);
        when(authMapper.queryByProviderId(pId, wId, id)).thenReturn(matchData);

        ProviderAuthData result1 = mgmtService.queryAuthInfoAndCheck(pId, wId, id);
        assertNotNull(result1);
        assertEquals(wId, result1.getWorkspaceId());

        ProviderAuthData systemData = new ProviderAuthData();
        systemData.setWorkspaceId("SYSTEM");
        when(authMapper.queryByProviderId(pId, "different_w", id)).thenReturn(systemData);

        ProviderAuthData result2 = mgmtService.queryAuthInfoAndCheck(pId, "different_w", id);
        assertNotNull(result2);
        assertEquals("SYSTEM", result2.getWorkspaceId());

        ProviderAuthData otherData = new ProviderAuthData();
        otherData.setWorkspaceId("other_workspace");
        when(authMapper.queryByProviderId(pId, wId, id)).thenReturn(otherData);

        ProviderAuthData result3 = mgmtService.queryAuthInfoAndCheck(pId, wId, id);
        assertNull(result3);
    }

    @Test
    void testCreateProviderFromMetadata_FullCoverage() {
        String pId = "p1";
        String wId = "w1";

        ProviderExportMetadata metadata = new ProviderExportMetadata();

        ModelServiceProvider provider = new ModelServiceProvider();
        provider.setId("id-123");
        provider.setProviderName("TestProvider"); // 必须设置，对应 584 行
        provider.setProviderUrl("https://test.com"); // 对应 592 行

        ProviderAuthMetadata authMetadata = new ProviderAuthMetadata();
        authMetadata.setAuthType("API_KEY");
        authMetadata.setAuthInfo("some-key");

        metadata.setModelServiceProviderMetadata(provider);
        metadata.setProviderAuthMetadata(authMetadata);
        metadata.setProviderAuthData(new ProviderAuthData());


        doNothing().when(licenseCtrlService).canAccessIntegrationModel();

        when(userProviderMapper.selectUserDataByName(eq(pId), eq(wId), eq("TestProvider")))
            .thenReturn(new ArrayList<>());

        ProviderAuth mockAuthCfg = mock(ProviderAuth.class);
        when(authService.getProviderAuth(any(), any())).thenReturn(mockAuthCfg);

        mgmtService.createProviderFromMetadata(pId, wId, metadata);

        verify(userProviderMapper).selectUserDataByName(pId, wId, "TestProvider");

        verify(userModelServiceProviderMapper, times(1)).insert(any());
    }

    @Test
    void testCreateProvider_RollbackWhenAuthDataFails() {
        ProviderExportMetadata metadata = createValidMetadata();
        when(userProviderMapper.selectUserDataByName(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        ProviderAuth mockAuth = mock(ProviderAuth.class);
        when(authService.getProviderAuth(any(), any())).thenReturn(mockAuth);
        doThrow(new RuntimeException("Mock DB Error"))
            .when(authService).createAuthInfo(any());
        assertThrows(AgentStudioException.class, () -> {
            mgmtService.createProviderFromMetadata("proj_1", "ws_1", metadata);
        });
        verify(userModelServiceProviderMapper).deleteById(any());
        verify(metadataMapper).deleteById(any());
    }
    private ProviderExportMetadata createValidMetadata() {
        ProviderExportMetadata metadata = new ProviderExportMetadata();
        ModelServiceProvider provider = new ModelServiceProvider();
        provider.setId("test-provider-id");
        provider.setProviderName("TestProvider");
        provider.setProviderUrl("https://api.example.com");
        metadata.setModelServiceProviderMetadata(provider);
        ProviderAuthMetadata authMetadata = new ProviderAuthMetadata();
        authMetadata.setId("test-auth-id");
        authMetadata.setAuthType("API_KEY");
        metadata.setProviderAuthMetadata(authMetadata);
        ProviderAuthData authData = new ProviderAuthData();
        metadata.setProviderAuthData(authData);
        return metadata;
    }

}



