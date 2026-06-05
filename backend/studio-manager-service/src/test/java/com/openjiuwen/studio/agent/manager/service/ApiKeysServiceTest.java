/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.ApiKeyRequest;
import com.openjiuwen.studio.agent.manager.dto.ApiKeysListResp;
import com.openjiuwen.studio.agent.manager.dto.ApikeyResponse;
import com.openjiuwen.studio.agent.manager.dto.QueryApiKeysQo;
import com.openjiuwen.studio.agent.manager.entity.ApiKeysEntity;
import com.openjiuwen.studio.agent.manager.mapper.ApiKeysMapper;
import com.openjiuwen.studio.agent.manager.service.auth.apikey.ApiKeysService;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ApiKeysServiceTest {

    @Mock
    private ApiKeysMapper apiKeysMapper;

    @Mock
    private RedisClient redisClient;

    @InjectMocks
    @Spy
    private ApiKeysService apiKeysService;

    private MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic;

    @BeforeEach
    public void setUp() {
        requestContextUtilsMockedStatic = mockStatic(RequestContextUtils.class);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("testDomainId");
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn("testUserName");
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("testUserId");
        ReflectionTestUtils.setField(apiKeysService, "apiKeyEnable", true);
    }

    @AfterEach
    public void tearDown() {
        if (requestContextUtilsMockedStatic != null) {
            requestContextUtilsMockedStatic.close();
        }
    }

    @Test
    public void testCreateApiKeySuccess() throws Exception {
        String projectId = "testProjectId";
        String workspaceId = "testWorkspaceId";
        ApiKeyRequest apiKeyRequest = new ApiKeyRequest()
            .setApikeyName("testTag")
            .setApikeyDescription("testDescription");

        // 模拟随机生成的 API Key 原始值
        byte[] mockApiKeyBytes = new byte[32];
        String mockApiKeyHex = "sk-0000000000000000000000000000000000000000000000000000000000000000";
        String encryptedValue = "encrypted_mock_value";

        // 模拟 genSecureRandomByte 返回固定值
        doReturn(mockApiKeyBytes).when(apiKeysService).genSecureRandomByte(32);

        try (MockedStatic<CryptoUtils> mgSccMockedStatic = mockStatic(CryptoUtils.class)) {
            // 模拟加密方法
            mgSccMockedStatic.when(() -> CryptoUtils.encrypt(mockApiKeyHex))
                .thenReturn(encryptedValue);

            // 模拟 apiKeysMapper 和 redisClient
            doNothing().when(apiKeysMapper).createApiKey(any(ApiKeysEntity.class));
            doNothing().when(redisClient).set(anyString(), anyString());

            // 调用测试方法
            ApikeyResponse response = apiKeysService.createApiKey(projectId, workspaceId, apiKeyRequest);

            // 验证结果
            assertNotNull(response);
            assertNotNull(response.getId());
            assertEquals(mockApiKeyHex, response.getApikey());

            verify(redisClient, times(1)).set(eq(mockApiKeyHex), anyString());
            verify(apiKeysMapper, times(1)).createApiKey(any(ApiKeysEntity.class));
        }
    }

    @Test
    public void testCreateApiKeyFailure() throws Exception {
        // 模拟 genSecureRandomByte 抛出异常
        doThrow(new RuntimeException("Random generation failed"))
            .when(apiKeysService).genSecureRandomByte(32);

        ApiKeyRequest apiKeyRequest = new ApiKeyRequest().setApikeyName("testTag");

        AgentStudioException exception = assertThrows(AgentStudioException.class, () ->
            apiKeysService.createApiKey("testProjectId", "testWorkspaceId", apiKeyRequest));

        assertEquals(StudioError.API_KEY_CREATE_ERROR, exception.getErrorCode());
    }

    @Test
    public void testSyncApiKeysToRedis() {
        List<ApiKeysEntity> apiKeys = new ArrayList<>();
        ApiKeysEntity key = new ApiKeysEntity();
        String encryptedValue = "encrypted_mock_value";
        String decryptedValue = "sk-0000000000000000000000000000000000000000000000000000000000000000";

        key.setApiKeyId(UUID.randomUUID().toString());
        key.setApiKeyValue(encryptedValue);
        key.setProjectId("testProjectId");
        key.setWorkspaceId("testWorkspaceId");
        key.setDomainId("testDomainId");
        key.setUserId("testUserId");
        apiKeys.add(key);

        // 模拟 apiKeysMapper.selectList
        Mockito.lenient().when(apiKeysMapper.selectList(isNull(), isNull(), eq(0), eq(0), isNull()))
            .thenReturn(apiKeys);

        // 模拟 redisClient.exists
        Mockito.lenient().when(redisClient.exists(anyString())).thenReturn(false);

        try (MockedStatic<CryptoUtils> mgSccMockedStatic = mockStatic(CryptoUtils.class)) {
            // 模拟 decrypt
            mgSccMockedStatic.when(() -> CryptoUtils.decrypt(encryptedValue))
                .thenReturn(decryptedValue);

            // 模拟 encrypt
            mgSccMockedStatic.when(() -> CryptoUtils.encrypt(anyString()))
                .thenReturn("mocked_encrypted_key");

            // 执行方法
            apiKeysService.syncApiKeysToRedis();

            // 验证 redisClient.set 被调用一次
            verify(redisClient, times(1)).set(anyString(), anyString());
        }
    }

    @Test
    public void testQueryApiKeys() {
        String projectId = "testProjectId";
        String workspaceId = "testWorkspaceId";
        QueryApiKeysQo queryApiKeysQo = new QueryApiKeysQo();
        queryApiKeysQo.setPageNum(1);
        queryApiKeysQo.setPageSize(10);
        queryApiKeysQo.setWorkspaceId(workspaceId);

        List<ApiKeysEntity> apiKeys = new ArrayList<>();
        ApiKeysEntity key = new ApiKeysEntity();
        key.setApiKeyName("testName");
        apiKeys.add(key);

        Mockito.lenient().when(apiKeysMapper.selectList(eq(projectId), eq(workspaceId), eq(10), eq(0), isNull()))
            .thenReturn(apiKeys);

        ApiKeysListResp result = apiKeysService.queryApiKeys(projectId, queryApiKeysQo);
        assertEquals(0, result.getTotal());
    }

    @Test
    public void testDeleteApiKey() {
        String apiKeyId = UUID.randomUUID().toString();
        String projectId = "testProjectId";
        String workspaceId = "testWorkspaceId";
        String userId = "testUserId";
        String domainId = "testDomainId";
        String apiKeyValue = "sk-0000000000000000000000000000000000000000000000000000000000000000";
        String encryptedValue = "encrypted_mock_value";

        ApiKeysEntity mockKey = new ApiKeysEntity();
        mockKey.setApiKeyId(apiKeyId);
        mockKey.setProjectId(projectId);
        mockKey.setWorkspaceId(workspaceId);
        mockKey.setUserId(userId);
        mockKey.setDomainId(domainId);
        mockKey.setApiKeyValue(encryptedValue);

        when(apiKeysMapper.selectById(anyString())).thenReturn(mockKey);
        doNothing().when(apiKeysMapper).deleteById(anyString());
        when(redisClient.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> apiKeysService.deleteApiKey(apiKeyId, projectId, workspaceId));
        verify(apiKeysMapper, times(1)).deleteById(eq(apiKeyId));
        verify(redisClient, times(1)).delete(eq(encryptedValue));
    }

    @Test
    public void testDeleteApiKeyNotFound() {
        String apiKeyId = UUID.randomUUID().toString();
        when(apiKeysMapper.selectById(anyString())).thenReturn(null);

        AgentStudioException exception = assertThrows(AgentStudioException.class, () ->
            apiKeysService.deleteApiKey(apiKeyId, "testProjectId", "testWorkspaceId"));

        assertEquals(StudioError.API_KEY_AUTH_ERROR, exception.getErrorCode());
    }

    @Test
    public void testGenSecureRandomByte() throws Exception {
        // 调用 genSecureRandomByte 方法，参数为 32
        byte[] result = apiKeysService.genSecureRandomByte(32);

        // 验证返回的字节数组长度是否为 32
        assertNotNull(result);
        assertEquals(32, result.length);

        // 验证字节数组中的值是否为随机值（非全 0）
        boolean allZero = true;
        for (byte b : result) {
            if (b != 0) {
                allZero = false;
                break;
            }
        }
        assertFalse(allZero, "生成的字节数组不应全为 0");
    }

    @Test
    public void testCreateApiKeyWhenApiKeyDisabled() {
        // 设置 apiKeyEnable 为 false
        ReflectionTestUtils.setField(apiKeysService, "apiKeyEnable", false);

        // 构造请求参数
        ApiKeyRequest apiKeyRequest = new ApiKeyRequest()
            .setApikeyName("testTag")
            .setApikeyDescription("testDescription");

        // 预期抛出 AgentStudioException，错误码为 API_KEY_UNABLE
        AgentStudioException exception = assertThrows(AgentStudioException.class, () ->
            apiKeysService.createApiKey("testProjectId", "testWorkspaceId", apiKeyRequest)
        );

        // 验证错误码是否正确
        assertEquals(StudioError.API_KEY_UNABLE, exception.getErrorCode());
    }

    @Test
    public void testQueryApiKeysWhenApiKeyDisabled() {
        // 设置 apiKeyEnable 为 false，模拟 API Key 功能被禁用
        ReflectionTestUtils.setField(apiKeysService, "apiKeyEnable", false);

        // 构造请求参数
        QueryApiKeysQo queryApiKeysQo = new QueryApiKeysQo();
        queryApiKeysQo.setPageNum(1);
        queryApiKeysQo.setPageSize(10);

        // 预期抛出 AgentStudioException
        AgentStudioException exception = assertThrows(AgentStudioException.class, () ->
            apiKeysService.queryApiKeys("testProjectId", queryApiKeysQo)
        );

        // 验证错误码是否为 API_KEY_UNABLE
        assertEquals(StudioError.API_KEY_UNABLE, exception.getErrorCode());
    }

    @Test
    public void testDeleteApiKeyWhenApiKeyDisabled() {
        // 设置 apiKeyEnable 为 false，模拟 API Key 功能被禁用
        ReflectionTestUtils.setField(apiKeysService, "apiKeyEnable", false);

        // 构造参数
        String apiKeyId = UUID.randomUUID().toString();
        String projectId = "testProjectId";
        String workspaceId = "testWorkspaceId";

        // 预期抛出 AgentStudioException
        AgentStudioException exception = assertThrows(AgentStudioException.class, () ->
            apiKeysService.deleteApiKey(apiKeyId, projectId, workspaceId)
        );

        // 验证错误码是否为 API_KEY_UNABLE
        assertEquals(StudioError.API_KEY_UNABLE, exception.getErrorCode());
    }
}
