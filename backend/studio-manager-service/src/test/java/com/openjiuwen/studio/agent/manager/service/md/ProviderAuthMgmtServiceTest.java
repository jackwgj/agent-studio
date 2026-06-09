/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

public class ProviderAuthMgmtServiceTest {
    @Test
    public void removeAuthConfigTest() {
        ProviderAuthDataMapper authDataMapper = Mockito.mock(ProviderAuthDataMapper.class);
        ProviderAuthService authService = Mockito.mock(ProviderAuthService.class);
        Mockito.doNothing().when(authService).deleteByProvider(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(authService).deleteById(Mockito.any(), Mockito.any(), Mockito.any());

        ProviderAuthMgmtService service = new ProviderAuthMgmtService();
        ReflectionTestUtils.setField(service, "authService", authService);
        ReflectionTestUtils.setField(service, "authDataMapper", authDataMapper);

        Mockito.when(authDataMapper.selectById("not_exist")).thenReturn(null);
        Mockito.when(authDataMapper.selectById("other_project"))
            .thenReturn(new ProviderAuthData().setProjectId("other_project"));

        String project = "project_id";
        String workspace = "test_workspace";

        Mockito.when(authDataMapper.selectById("other_workspace"))
            .thenReturn(new ProviderAuthData().setProjectId(project).setWorkspaceId("other_workspace"));

        Mockito.when(authDataMapper.selectById("IAM"))
            .thenReturn(new ProviderAuthData().setProjectId(project).setWorkspaceId(workspace).setAuthType("IAM"));

        Mockito.when(authDataMapper.selectById("NO_AUTH"))
            .thenReturn(new ProviderAuthData().setProjectId(project).setWorkspaceId(workspace).setAuthType("NO_AUTH"));

        Mockito.when(authDataMapper.selectById("ok"))
            .thenReturn(new ProviderAuthData().setProjectId(project).setWorkspaceId(workspace).setAuthType("API_KEY"));

        try {
            service.removeAuthConfig(project, workspace, "not_exist");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_AUTH_DATA_NOT_EXIST, e.getErrorCode());
        }

        try {
            service.removeAuthConfig(project, workspace, "other_project");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_USER_NO_PERMISSION_DELETE, e.getErrorCode());
        }

        try {
            service.removeAuthConfig(project, workspace, "other_workspace");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_USER_NO_PERMISSION_DELETE, e.getErrorCode());
        }

        try {
            service.removeAuthConfig(project, workspace, "IAM");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_NOT_SUPPORT_DELETE, e.getErrorCode());
        }

        try {
            service.removeAuthConfig(project, workspace, "NO_AUTH");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_NOT_SUPPORT_DELETE, e.getErrorCode());
        }

        service.removeAuthConfig(project, workspace, "ok");
    }

    @Test
    public void removeProviderAuthCfgTest() {
        ProviderAuthService authService = Mockito.mock(ProviderAuthService.class);
        Mockito.doNothing().when(authService).deleteByProvider(Mockito.any(), Mockito.any(), Mockito.any());

        ProviderAuthMgmtService service = new ProviderAuthMgmtService();
        ReflectionTestUtils.setField(service, "authService", authService);

        String project = "project_id";
        String workspace = "test_workspace";

        Mockito.when(authService.selectProviderMetadataAndPermissionCheck("IAM", workspace, "provider", true))
            .thenReturn(Collections.singletonList(new ProviderAuthMetadata().setProjectId(project).setAuthType("IAM")));

        Mockito.when(authService.selectProviderMetadataAndPermissionCheck("NO_AUTH", workspace, "provider", true))
            .thenReturn(
                Collections.singletonList(new ProviderAuthMetadata().setProjectId(project).setAuthType("NO_AUTH")));

        Mockito.when(authService.selectProviderMetadataAndPermissionCheck("ok", workspace, "provider", true))
            .thenReturn(
                Collections.singletonList(new ProviderAuthMetadata().setProjectId(project).setAuthType("API_KEY")));

        try {
            service.removeProviderAuthCfg(project, workspace, "", "");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_REQUEST_PARAM_AUTH_INVALID, e.getErrorCode());
        }

        try {
            service.removeProviderAuthCfg("IAM", workspace, "provider", "");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_NOT_SUPPORT_DELETE, e.getErrorCode());
        }

        try {
            service.removeProviderAuthCfg("NO_AUTH", workspace, "provider", "");
            Assertions.fail();
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.MD_PROVIDER_AUTH_NOT_SUPPORT_DELETE, e.getErrorCode());
        }

        service.removeProviderAuthCfg("ok", workspace, "provider", "");
    }
}
