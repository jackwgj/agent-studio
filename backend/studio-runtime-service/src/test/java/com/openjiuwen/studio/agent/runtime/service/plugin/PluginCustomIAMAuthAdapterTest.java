/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.md.CustomIAMAuthInfo;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelHttpClientService;
import com.openjiuwen.studio.agent.runtime.service.plugin.impl.PluginCustomIAMAdapter;
import com.openjiuwen.studio.agent.runtime.service.plugin.impl.PluginTransformImpl;

import io.swagger.v3.oas.models.security.SecurityScheme;

import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class PluginCustomIAMAuthAdapterTest {
    private MockedStatic<SpringBeanUtils> springBeanUtilsMock;

    private MockedStatic<CryptoUtils> sccMock;

    private CloseableHttpClient clientMock;

    private RuntimeModelHttpClientService httpClientServiceMock;

    @BeforeEach
    public void beforeEach() {
        springBeanUtilsMock = Mockito.mockStatic(SpringBeanUtils.class, RETURNS_DEEP_STUBS);
        httpClientServiceMock = mock(RuntimeModelHttpClientService.class);
        clientMock = mock(CloseableHttpClient.class);

        sccMock = Mockito.mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS);

        springBeanUtilsMock.when(() -> SpringBeanUtils.getBean(RuntimeModelHttpClientService.class))
            .thenReturn(httpClientServiceMock);

        sccMock.when(() -> CryptoUtils.decrypt(Mockito.anyString())).thenReturn("**");
    }

    @AfterEach
    public void afterEach() {
        springBeanUtilsMock.close();
        sccMock.close();
    }

    @Test
    public void customIamAuthTest() throws IOException {
        CustomIAMAuthInfo auth = new CustomIAMAuthInfo(
            "{\"iamProject\":\"iamProject\",\"iamDomain\":\"iamDomain\",\"iamUrl\":\"http://127.0.0.1/test\",\"iamUser\":\"iamUser\",\"iamPassword\":\"iamPassword\"}");

        when(httpClientServiceMock.getModelRequestHttpClient(Mockito.anyString())).thenReturn(clientMock);

        CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<String> handler = invocation.getArgument(1); // 获取第二个参数，即ResponseHandler
                return handler.handleResponse(responseMock); // 执行Handler并返回其结果
            });

        when(responseMock.getCode()).thenReturn(200);
        when(responseMock.getFirstHeader("X-Subject-Token")).thenReturn(new BasicHeader("X-Auth-Token", "token"));

        PluginCustomIAMAdapter adapter = new PluginCustomIAMAdapter();
        Map<String, String> headers = adapter.requestHeaderHandler(auth, new HashMap<>());
        Assertions.assertEquals("{X-Auth-Token=token}", headers.toString());
    }

    @Test
    public void customIamAkSkAuthTest() throws IOException {
        CustomIAMAuthInfo auth = new CustomIAMAuthInfo(
            "{\"iamProject\":\"iamProject\",\"iamDomain\":\"iamDomain\",\"iamUrl\":\"http://127.0.0.1/test\",\"iamAK\":\"iamAK\",\"iamSK\":\"iamSK\"}");

        when(httpClientServiceMock.getModelRequestHttpClient(Mockito.anyString())).thenReturn(clientMock);

        CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<String> handler = invocation.getArgument(1); // 获取第二个参数，即ResponseHandler
                return handler.handleResponse(responseMock); // 执行Handler并返回其结果
            });

        when(responseMock.getCode()).thenReturn(200);
        when(responseMock.getFirstHeader("X-Subject-Token")).thenReturn(new BasicHeader("X-Auth-Token", "token"));

        PluginCustomIAMAdapter adapter = new PluginCustomIAMAdapter();
        Map<String, String> headers = adapter.requestHeaderHandler(auth, new HashMap<>());
        Assertions.assertEquals("{X-Auth-Token=token}", headers.toString());
    }

    @Test
    public void customIamAuthExceptionTest() throws IOException {
        CustomIAMAuthInfo auth = new CustomIAMAuthInfo(
            "{\"iamProject\":\"iamProject\",\"iamDomain\":\"iamDomain\",\"iamUrl\":\"http://127.0.0.1/test\",\"iamUser\":\"iamUser\",\"iamPassword\":\"iamPassword\"}");

        when(httpClientServiceMock.getModelRequestHttpClient(Mockito.anyString())).thenReturn(clientMock);

        CloseableHttpResponse responseMock = mock(CloseableHttpResponse.class);
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<String> handler = invocation.getArgument(1); // 获取第二个参数，即ResponseHandler
                return handler.handleResponse(responseMock); // 执行Handler并返回其结果
            });

        when(responseMock.getCode()).thenReturn(400);

        PluginCustomIAMAdapter adapter = new PluginCustomIAMAdapter();
        try {
            Map<String, String> headers = adapter.requestHeaderHandler(auth, new HashMap<>());
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.PLUGIN_AUTH_DATA_INVALID, e.getErrorCode());
        }
    }

    @Test
    void testTimeout() throws IOException {
        CustomIAMAuthInfo auth = new CustomIAMAuthInfo(
            "{\"iamProject\":\"iamProject\",\"iamDomain\":\"iamDomain\",\"iamUrl\":\"http://127.0.0.1/test\",\"iamUser\":\"iamUser\",\"iamPassword\":\"iamPassword\"}");
        PluginCustomIAMAdapter adapter = new PluginCustomIAMAdapter();
        when(httpClientServiceMock.getModelRequestHttpClient(Mockito.anyString())).thenReturn(clientMock);
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenThrow(ConnectTimeoutException.class);
        try {
            Map<String, String> headers = adapter.requestHeaderHandler(auth, new HashMap<>());
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.PLUGIN_AUTH_DATA_INVALID, e.getErrorCode());
        }
    }

    @Test
    public void test_transformAuthInfo2CustomIAMAuthInfo_normal_case() throws Exception {
        // Given
        SecurityScheme securityScheme = mock(SecurityScheme.class);
        Map<String, Object> extensions = new HashMap<>();
        extensions.put(Constant.OpenAPI.CUSTOM_IAM_URL, "https://test/v3/auth/tokens");
        extensions.put(Constant.OpenAPI.CUSTOM_IAM_DOMAIN, "example.com");
        extensions.put(Constant.OpenAPI.CUSTOM_IAM_PROJECT, "project1");
        extensions.put(Constant.OpenAPI.CUSTOM_IAM_USER, "user1");
        extensions.put(Constant.OpenAPI.CUSTOM_IAM_PASSWORD, "password1");
        extensions.put(Constant.OpenAPI.CUSTOM_IAM_AK, "ak1");
        extensions.put(Constant.OpenAPI.CUSTOM_IAM_SK, "sk1");
        when(securityScheme.getExtensions()).thenReturn(extensions);

        // When
        PluginTransformImpl pluginTransformImpl = new PluginTransformImpl();
        CustomIAMAuthInfo result = pluginTransformImpl.transformAuthInfo2CustomIAMAuthInfo(securityScheme);
    }
}
