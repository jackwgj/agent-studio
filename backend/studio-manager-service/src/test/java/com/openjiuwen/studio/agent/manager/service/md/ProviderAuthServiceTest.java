/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.common.dto.md.NotAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthConfig;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthMetadataMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class ProviderAuthServiceTest {

    @Mock
    private ProviderAuthMetadataMapper metadataMapper;

    @Mock
    private ProviderAuthDataMapper authMapper;

    @Mock
    private MgObsService obsService;

    @Mock
    private RedisClient redisClient;

    @Mock
    private RedisLock redisLock;

    @InjectMocks
    private ProviderAuthService providerAuthService;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(providerAuthService, "upgradeSyncEnable", false);
        lenient().when(redisClient.getLock(anyString())).thenReturn(redisLock);
        lenient().when(redisLock.tryLock(any(Duration.class))).thenReturn(true);
    }

    @Test
    void testCreateAuthInfo_Success() {
        ProviderAuthData authData = new ProviderAuthData()
            .setId("auth-1")
            .setProviderId("provider-1")
            .setProjectId("project-1")
            .setAuthType("API_KEY")
            .setAuthInfo("{\"api_key\":\"test\"}");

        providerAuthService.createAuthInfo(authData);

        verify(authMapper).insert(authData);
        verify(obsService).putObject(anyString(), anyString());
        verify(redisClient).set(eq("MODEL_AUTH_auth-1"), anyString());
    }

    @Test
    void testUpdateAuthInfo_Success() {
        ProviderAuthData authData = new ProviderAuthData()
            .setId("auth-1")
            .setProviderId("provider-1")
            .setProjectId("project-1")
            .setAuthType("API_KEY")
            .setAuthInfo("{\"api_key\":\"updated\"}");

        providerAuthService.updateAuthInfo(authData);

        verify(authMapper).updateAuthData(eq("auth-1"), eq("API_KEY"), anyString(), anyLong());
        verify(obsService).putObject(anyString(), anyString());
        verify(redisClient).set(eq("MODEL_AUTH_auth-1"), anyString());
    }

    @Test
    void testDeleteByProvider_Success() {
        ProviderAuthData data1 = new ProviderAuthData().setId("auth-1").setProjectId("p1").setProviderId("prov-1");
        ProviderAuthData data2 = new ProviderAuthData().setId("auth-2").setProjectId("p1").setProviderId("prov-1");
        when(authMapper.selectByProviderId("p1", "w1", "prov-1")).thenReturn(List.of(data1, data2));

        providerAuthService.deleteByProvider("p1", "w1", "prov-1");

        verify(authMapper).clearByProviderId("p1", "w1", "prov-1");
        verify(obsService, times(2)).deleteObject(anyString());
        verify(redisClient).delete("MODEL_AUTH_auth-1");
        verify(redisClient).delete("MODEL_AUTH_auth-2");
    }

    @Test
    void testDeleteByProvider_EmptyList() {
        when(authMapper.selectByProviderId("p1", "w1", "prov-1")).thenReturn(Collections.emptyList());

        providerAuthService.deleteByProvider("p1", "w1", "prov-1");

        verify(authMapper).clearByProviderId("p1", "w1", "prov-1");
        verify(obsService, never()).deleteObject(anyString());
    }

    @Test
    void testBackupByProvider_Success() {
        ProviderAuthData data1 = new ProviderAuthData().setId("auth-1").setProjectId("p1").setProviderId("prov-1");
        when(authMapper.selectByProviderId("p1", "w1", "prov-1")).thenReturn(List.of(data1));

        providerAuthService.backupByProvider("p1", "w1", "prov-1");

        verify(authMapper).insertBackup(any(ProviderAuthData.class));
    }

    @Test
    void testDeleteById_Success() {
        providerAuthService.deleteById("p1", "prov-1", "auth-1");

        verify(authMapper).clearById("auth-1");
        verify(obsService).deleteObject(contains("auth-1"));
        verify(redisClient).delete("MODEL_AUTH_auth-1");
    }

    @Test
    void testBackupById_Success() {
        ProviderAuthData data = new ProviderAuthData().setId("auth-1").setProjectId("p1");
        when(authMapper.selectById("auth-1")).thenReturn(data);

        providerAuthService.backupById("auth-1");

        verify(authMapper).insertBackup(any(ProviderAuthData.class));
    }

    @Test
    void testGetProviderAuth_ApiKey() {
        ProviderAuth result = providerAuthService.getProviderAuth("API_KEY", "{\"api_key\":\"test-key\"}");
        assertNotNull(result);
    }

    @Test
    void testGetProviderAuth_NoAuth() {
        ProviderAuth result = providerAuthService.getProviderAuth("NO_AUTH", "");
        assertNotNull(result);
        assertInstanceOf(NotAuthInfo.class, result);
    }

    @Test
    void testGetProviderAuth_CustomApiKey() {
        ProviderAuth result = providerAuthService.getProviderAuth("CUSTOM_APIKEY", "{\"key\":\"value\"}");
        assertNotNull(result);
    }

    @Test
    void testGetProviderAuth_UnsupportedType() {
        assertThrows(AgentStudioException.class, () ->
            providerAuthService.getProviderAuth("UNSUPPORTED", "data"));
    }

    @Test
    void testTransToAuthConfig_WithAuthConfig() {
        ProviderAuthMetadata metadata = new ProviderAuthMetadata()
            .setId("meta-1")
            .setProviderId("prov-1")
            .setAuthType("NO_AUTH")
            .setAuthInfo("info")
            .setAuthId("auth-id")
            .setAuthConfig("config")
            .setAuthUrl("http://auth.url");

        ProviderAuthConfig result = providerAuthService.transToAuthConfig(metadata);

        assertNotNull(result);
        assertEquals("meta-1", result.getMetadataId());
        assertEquals("prov-1", result.getProviderId());
        assertEquals("NO_AUTH", result.getAuthType());
        assertEquals("auth-id", result.getAuthId());
        assertTrue(result.isIsRecord());
        assertEquals("http://auth.url", result.getAuthUrl());
    }

    @Test
    void testTransToAuthConfig_WithoutAuthConfig() {
        ProviderAuthMetadata metadata = new ProviderAuthMetadata()
            .setId("meta-1")
            .setProviderId("prov-1")
            .setAuthType("API_KEY")
            .setAuthInfo("raw-info")
            .setAuthUrl("http://auth.url");

        ProviderAuthConfig result = providerAuthService.transToAuthConfig(metadata);

        assertNotNull(result);
        assertFalse(result.isIsRecord());
        assertEquals("raw-info", result.getAuthInfo());
    }

    @Test
    void testSelectProviderMetadataAndPermissionCheck_Success() {
        ProviderAuthMetadata meta = new ProviderAuthMetadata()
            .setId("meta-1")
            .setProjectId("p1")
            .setWorkspaceId("w1");
        when(metadataMapper.selectByProvider("prov-1")).thenReturn(List.of(meta));

        List<ProviderAuthMetadata> result = providerAuthService
            .selectProviderMetadataAndPermissionCheck("p1", "w1", "prov-1", false);

        assertEquals(1, result.size());
    }

    @Test
    void testSelectProviderMetadataAndPermissionCheck_EmptyMetadata() {
        when(metadataMapper.selectByProvider("prov-1")).thenReturn(Collections.emptyList());

        assertThrows(AgentStudioException.class, () ->
            providerAuthService.selectProviderMetadataAndPermissionCheck("p1", "w1", "prov-1", false));
    }

    @Test
    void testSelectProviderMetadataAndPermissionCheck_WrongProject() {
        ProviderAuthMetadata meta = new ProviderAuthMetadata()
            .setId("meta-1")
            .setProjectId("other-project")
            .setWorkspaceId("w1");
        when(metadataMapper.selectByProvider("prov-1")).thenReturn(List.of(meta));

        assertThrows(AgentStudioException.class, () ->
            providerAuthService.selectProviderMetadataAndPermissionCheck("p1", "w1", "prov-1", false));
    }

    @Test
    void testSelectProviderMetadataAndPermissionCheck_WrongWorkspace() {
        ProviderAuthMetadata meta = new ProviderAuthMetadata()
            .setId("meta-1")
            .setProjectId("p1")
            .setWorkspaceId("other-workspace");
        when(metadataMapper.selectByProvider("prov-1")).thenReturn(List.of(meta));

        assertThrows(AgentStudioException.class, () ->
            providerAuthService.selectProviderMetadataAndPermissionCheck("p1", "w1", "prov-1", false));
    }

    @Test
    void testSelectProviderMetadataAndPermissionCheck_ContainPlatform() {
        ProviderAuthMetadata meta = new ProviderAuthMetadata()
            .setId("meta-1")
            .setProjectId("SYSTEM")
            .setWorkspaceId("SYSTEM");
        when(metadataMapper.selectByProvider("prov-1")).thenReturn(List.of(meta));

        List<ProviderAuthMetadata> result = providerAuthService
            .selectProviderMetadataAndPermissionCheck("p1", "w1", "prov-1", true);

        assertEquals(1, result.size());
    }

    @Test
    void testSyncUpgradeAuthDatas_Success() {
        ReflectionTestUtils.setField(providerAuthService, "upgradeSyncEnable", true);
        ProviderAuthData data = new ProviderAuthData()
            .setId("auth-1")
            .setProjectId("p1")
            .setProviderId("prov-1");
        when(authMapper.queryNotSyncDatas()).thenReturn(List.of(data));

        providerAuthService.syncUpgradeAuthDatas();

        verify(obsService).putObject(anyString(), anyString());
        verify(redisClient).set(eq("MODEL_AUTH_auth-1"), anyString());
        verify(authMapper).updateSyncStatus("auth-1");
    }

    @Test
    void testSyncUpgradeAuthDatas_LockFailed() throws Exception {
        ReflectionTestUtils.setField(providerAuthService, "upgradeSyncEnable", true);
        when(redisLock.tryLock(any(Duration.class))).thenReturn(false);

        providerAuthService.syncUpgradeAuthDatas();

        verify(authMapper, never()).queryNotSyncDatas();
    }

    @Test
    void testSyncAuthDataToNewPath_NoNeedSync() {
        when(redisClient.get("MODEL_AUTH_UPDATE_TIME_KEY")).thenReturn("1000");

        providerAuthService.syncAuthDataToNewPath();

        verify(authMapper, never()).selectAllAuthByTime(anyLong());
    }

    @Test
    void testSyncAuthDataToNewPath_WithRecentUpdate() {
        String recentTime = String.valueOf(System.currentTimeMillis() - 1000);
        when(redisClient.get("MODEL_AUTH_UPDATE_TIME_KEY")).thenReturn(recentTime);
        when(authMapper.selectAllAuthByTime(anyLong())).thenReturn(Collections.emptyList());

        providerAuthService.syncAuthDataToNewPath();

        verify(redisClient).set(eq("MODEL_AUTH_UPDATE_TIME_KEY"), anyString());
    }
}
