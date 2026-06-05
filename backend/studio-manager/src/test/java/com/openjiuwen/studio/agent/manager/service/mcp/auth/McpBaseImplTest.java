/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.common.dto.auth.OauthInfo;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
class McpBaseImplTest extends BaseTest {

    @MockitoSpyBean
    private EncryptionAdapter encryptionAdapter;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OkHttpClientUtils okHttpClientUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OkHttpClient okHttpClient;

    @InjectMocks
    private McpOauthAdapter mCPOauthAdapter;

    private AutoCloseable mockitoCloseable;

    @Autowired
    private McpBaseImpl mcpBase;

    private MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(mCPOauthAdapter, "okHttpClientUtils", okHttpClientUtils);
        ReflectionTestUtils.setField(mcpBase, "encryptionAdapter", encryptionAdapter);

        // 设置上下文
        requestContextUtilsMockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn("test_domain_id");
        requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn("test_project_id");

        Mockito.doReturn("E1-{fake-uuid}-fake_encrypt_result")
                .when(encryptionAdapter).encrypt(anyString(), any());

        Mockito.doReturn("fake_decrypt_result")
                .when(encryptionAdapter).decrypt(anyString());

        Mockito.doReturn("fake_decrypt_result")
                .when(encryptionAdapter).decrypt(anyString(), any());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (requestContextUtilsMockedStatic != null) {
            requestContextUtilsMockedStatic.close();
        }
        mockitoCloseable.close();
    }

    @Test
    void testRequestHeaderHandler() throws Exception {
        OauthInfo oauthInfo = mock(OauthInfo.class);
        when(oauthInfo.getEndpointUrl()).thenReturn("http://example.com");
        when(oauthInfo.getClientId()).thenReturn("clientId");
        when(oauthInfo.getScope()).thenReturn("scope");

        when(okHttpClientUtils.getHttpClient()).thenReturn(okHttpClient);
        Call call = Mockito.mock(Call.class);
        when(okHttpClient.newCall(any())).thenReturn(call);

        when(call.execute()).thenAnswer(invocationOnMock -> {
            Response response = mock(Response.class);
            when(response.code()).thenReturn(200);

            ResponseBody responseBody = mock(ResponseBody.class);
            when(response.body()).thenReturn(responseBody);
            when(responseBody.string()).thenReturn("{\"access_token\":\"dummyToken\"}");

            return response;
        });

        Map<String, String> headers = new HashMap<>();
        Map<String, String> result = mCPOauthAdapter.requestHeaderHandler(oauthInfo, headers);

        assertEquals("Bearer dummyToken", result.get("Authorization"));
    }

    @Test
    void testRequestHeaderHandler_ClientIdNotEmpty_failure() throws Exception {
        OauthInfo oauthInfo = mock(OauthInfo.class);
        when(oauthInfo.getEndpointUrl()).thenReturn("http://example.com");
        when(oauthInfo.getClientId()).thenReturn("clientId");
        when(oauthInfo.getScope()).thenReturn("scope");

        when(okHttpClientUtils.getHttpClient()).thenReturn(okHttpClient);
        Call call = Mockito.mock(Call.class);
        when(okHttpClient.newCall(any())).thenReturn(call);

        when(call.execute()).thenAnswer(invocationOnMock -> {
            Response response = mock(Response.class);
            when(response.code()).thenReturn(401);

            return response;
        });

        Map<String, String> headers = new HashMap<>();
        Assertions.assertThrows(AgentStudioException.class, () -> mCPOauthAdapter.requestHeaderHandler(oauthInfo, headers));

        when(call.execute()).thenAnswer(invocationOnMock -> {
            throw new IOException();
        });
        Assertions.assertThrows(AgentStudioException.class, () -> mCPOauthAdapter.requestHeaderHandler(oauthInfo, headers));
    }

    @Test
    void test_anonymizeAuthInfo() throws Exception{
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setClientId("clientId");
        oauthInfo.setClientSecret("clientSecret");
        oauthInfo.setEndpointUrl("http://example.com");

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.OAUTH);
        authInfo.setCustomOauth(oauthInfo);

        mcpBase.anonymizeAuthInfo(authInfo);
    }

    @Test
    void test_encryptedAuthInfo() throws Exception{
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setClientId("clientId");
        oauthInfo.setClientSecret("clientSecret");
        oauthInfo.setEndpointUrl("http://example.com");

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.OAUTH);
        authInfo.setCustomOauth(oauthInfo);

        mcpBase.encryptedAuthInfo(authInfo);
    }

    @Test
    void test_decryptedAuthInfo() throws Exception{
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setClientId("clientId");
        oauthInfo.setClientSecret("clientSecret");
        oauthInfo.setEndpointUrl("http://example.com");

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.OAUTH);
        authInfo.setCustomOauth(oauthInfo);
        mcpBase.encryptedAuthInfo(authInfo);
        mcpBase.decryptedAuthInfo(authInfo);
    }

    @Test
    void test_anonymizeAuthInfo_oauth() throws Exception{
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.SERVICE);
        authInfo.setAuthKeys(List.of(authKeyInfo));

        mcpBase.anonymizeAuthInfo(authInfo);
    }

    @Test
    void test_encryptedAuthInfo_apikey() throws Exception{
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();


        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.SERVICE);
        authInfo.setAuthKeys(List.of(authKeyInfo));

        mcpBase.encryptedAuthInfo(authInfo);

        authKeyInfo.setTargetName("targetName");
        authKeyInfo.setAuthKey("authKey");
        mcpBase.encryptedAuthInfo(authInfo);
    }

    @Test
    void test_decryptedAuthInfo_apikey() throws Exception{
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();


        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.SERVICE);
        authKeyInfo.setTargetName("targetName");
        authKeyInfo.setAuthKey("authKey");
        authInfo.setAuthKeys(List.of(authKeyInfo));
        mcpBase.encryptedAuthInfo(authInfo);
        mcpBase.decryptedAuthInfo(authInfo);
    }

    @Test
    public void test_exchangeTokenByAuth_api_key_headers_domain() {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setDomain(AuthInfo.DomainEnum.HEADERS);

        authInfo.setAuthKeys(List.of(createAuthKeyInfo("key1", "value1")));

        Map<String, String> headers = new HashMap<>();
        String mcpUrl = "http://example.com";

        mcpBase.exchangeTokenByAuth(authInfo, headers, mcpUrl);
    }

    @Test
    public void test_exchangeTokenByAuth_api_key_query_domain() throws URISyntaxException {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setDomain(AuthInfo.DomainEnum.QUERY);

        authInfo.setAuthKeys(List.of(createAuthKeyInfo("key1", "value1")));

        Map<String, String> headers = new HashMap<>();
        String mcpUrl = "http://example.com";

        mcpBase.exchangeTokenByAuth(authInfo, headers, mcpUrl);
    }

    @Test
    public void test_exchangeTokenByAuth_default_return() {
        AuthInfo authInfo = null;
        Map<String, String> headers = new HashMap<>();
        String mcpUrl = "http://example.com";

        String result = mcpBase.exchangeTokenByAuth(authInfo, headers, mcpUrl);

        assertEquals("http://example.com", result);
    }

    private AuthKeyInfo createAuthKeyInfo(String authKey, String targetName) {
        AuthKeyInfo authKeyInfo = mock(AuthKeyInfo.class);
        when(authKeyInfo.getAuthKey()).thenReturn(authKey);
        when(authKeyInfo.getTargetName()).thenReturn(targetName);
        return authKeyInfo;
    }

    @Test
    void test_validAuthInfo_null_auth_info() {
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setClientId("clientId");
        oauthInfo.setClientSecret("clientSecret");
        oauthInfo.setEndpointUrl("http://example.com");

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.OAUTH);
        authInfo.setCustomOauth(oauthInfo);

        mcpBase.validAuthInfo(authInfo);
    }

    @Test
    void test_validAuthInfo_throw_auth_info() {
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setClientId("clientId");
        oauthInfo.setClientSecret("clientSecret");
        oauthInfo.setEndpointUrl("");

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.OAUTH);
        authInfo.setCustomOauth(oauthInfo);

        Assertions.assertThrows(AgentStudioException.class, () -> mcpBase.validAuthInfo(authInfo));

    }

    @Test
    void test_encryptedAuthInfo_apikey_success() throws Exception{
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();


        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.SERVICE);
        authInfo.setAuthKeys(List.of(authKeyInfo));

        authKeyInfo.setTargetName("targetName");
        authKeyInfo.setAuthKey("authKey");
        mcpBase.validAuthInfo(authInfo);
    }

    @Test
    void test_encryptedAuthInfo_apikey_failure() throws Exception{
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.SERVICE);
        authInfo.setAuthKeys(List.of(authKeyInfo));

        authKeyInfo.setTargetName("");
        authKeyInfo.setAuthKey("authKey");
        Assertions.assertThrows(AgentStudioException.class, () -> mcpBase.validAuthInfo(authInfo));
    }

    @Test
    void test_parseMcpInput2ResourceParameter() {
        McpServiceEntity req = new McpServiceEntity();
        String validJson = "{\n  \"mcpServers\": {\n    \"example-sse\": {\n      \"url\": \"http://fakeIp:38341/sse\",\n      \"headers\": {\n        \"test1\": \"123\"\n      }\n    }\n  }\n}";
        req.setServerConfig(validJson);
        req.setId("1c8a6f8868c34d61ae2e0549a33f0a9c");
        req.setFcInstanceStatus("creatingStack");
        req.setFcInstanceId("creatingStack");
        Mockito.doReturn(validJson).when(encryptionAdapter).decrypt(anyString());
        Mockito.doReturn(validJson).when(encryptionAdapter).decrypt(anyString(), any());

        mcpBase.parseMcpInput2ResourceParameter(req);
    }

    @Test
    void test_validOauthUrl() {
        Assertions.assertThrows(AgentStudioException.class, () -> mcpBase.validOauthUrl("http://example.com"));
    }

    @Test
    void test_validIsNeedToUpdateAuthInfo() {
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setClientId("clientId");
        oauthInfo.setClientSecret("clientSecret");
        oauthInfo.setEndpointUrl("http://example.com");

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.OAUTH);
        authInfo.setCustomOauth(oauthInfo);

        mcpBase.validIsNeedToUpdateAuthInfo(authInfo, authInfo);
    }

    @Test
    void test_validIsNeedToUpdateAuthInfo_apikey() {
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();

        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.SERVICE);
        authInfo.setAuthKeys(List.of(authKeyInfo));

        authKeyInfo.setTargetName("targetName");
        authKeyInfo.setAuthKey("authKey");
        mcpBase.validIsNeedToUpdateAuthInfo(authInfo, authInfo);

    }

    @Test
    @DisplayName("覆盖 maskKey 异常分支 1：解密抛出 AgentStudioException (如 403 跨租户)，捕获并返回占位符")
    void testMaskKey_ThrowsAgentStudioException() {
        // 准备测试数据
        String cipherKey = "E1-{fake-uuid}-cipher";

        // 模拟底层 KMS 校验拦截（如 KMS.0307 无权限）抛出 WushanException/AgentStudioException
        when(encryptionAdapter.decrypt(cipherKey))
                .thenThrow(new AgentStudioException(StudioError.KMS_CRYPTO_ERROR));

        // 通过反射调用私有方法
        String result = ReflectionTestUtils.invokeMethod(mcpBase, "maskKey", cipherKey);

        // 断言：没有崩溃，成功返回脱敏占位符
        assertEquals(CommonConstant.ANONYMIZED_TEXT, result);
    }

    @Test
    @DisplayName("覆盖 maskKey 异常分支 2：解密抛出通用 Exception，捕获并返回占位符")
    void testMaskKey_ThrowsGeneralException() {
        String cipherKey = "E1-{fake-uuid}-cipher";

        // 模拟底层抛出未知的运行时异常（如网络超时、空指针等）
        when(encryptionAdapter.decrypt(cipherKey))
                .thenThrow(new RuntimeException("Unexpected network or KMS failure"));

        String result = ReflectionTestUtils.invokeMethod(mcpBase, "maskKey", cipherKey);

        // 断言：安全兜底，返回脱敏占位符
        assertEquals(CommonConstant.ANONYMIZED_TEXT, result);
    }

    @Test
    @DisplayName("覆盖 maskKey 长度分支：解密成功，且明文长度刚好为 1")
    void testMaskKey_DecryptedKeyLengthIsOne() {
        String cipherKey = "cipher_for_single_char";
        String decryptedKey = "X"; // 模拟用户的明文密码只有 1 位

        // 模拟解密成功
        when(encryptionAdapter.decrypt(cipherKey)).thenReturn(decryptedKey);

        String result = ReflectionTestUtils.invokeMethod(mcpBase, "maskKey", cipherKey);

        // 预期结果：第一位是 'X'，后面拼接去掉首位的脱敏符
        String expected = "X" + CommonConstant.ANONYMIZED_TEXT.substring(1);
        assertEquals(expected, result);
    }

}
