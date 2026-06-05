/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.plugin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.SgovAuthInfo;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelHttpClientService;
import com.openjiuwen.studio.agent.runtime.service.plugin.impl.PluginHisSgovAdapter;
import com.openjiuwen.studio.agent.runtime.service.plugin.impl.PluginTransformImpl;

import io.swagger.v3.oas.models.security.SecurityScheme;

import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PluginSgovAuthAdapterTest {
    private MockedStatic<SpringBeanUtils> springBeanUtilsMock;

    private MockedStatic<CryptoUtils> sccMock;

    private CloseableHttpClient clientMock;

    private RuntimeModelHttpClientService httpClientServiceMock;

    @Mock
    private HttpEntity httpEntity;

    @BeforeEach
    public void beforeEach() {
        springBeanUtilsMock = Mockito.mockStatic(SpringBeanUtils.class, RETURNS_DEEP_STUBS);
        httpClientServiceMock = Mockito.mock(RuntimeModelHttpClientService.class);
        clientMock = Mockito.mock(CloseableHttpClient.class);

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
    public void sgovAuthTest() throws IOException {
        SgovAuthInfo auth = new SgovAuthInfo(
            "{\"appId\":\"app\",\"credential\":\"cre\",\"sgovUrl\":\"http://127.0.0.1/test\",\"userId\":\"uu\", \"scenarioUuid\":\"uuid\"}");
        auth.setAuthId("authId");

        when(httpClientServiceMock.getModelRequestHttpClient(Mockito.anyString())).thenReturn(clientMock);

        CloseableHttpResponse responseMock = Mockito.mock(CloseableHttpResponse.class);
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<String> handler = invocation.getArgument(1); // 获取第二个参数，即ResponseHandler
                return handler.handleResponse(responseMock); // 执行Handler并返回其结果
            });

        when(responseMock.getCode()).thenReturn(200);

        when(responseMock.getEntity()).thenReturn(new StringEntity("{\"result\":\"token\"}"));

        PluginHisSgovAdapter adapter = new PluginHisSgovAdapter();
        Map<String, String> headers = adapter.requestHeaderHandler(auth, new HashMap<>());
        Assertions.assertEquals("{Authorization=token, cust-userid=uu, cust-scenariouuid=uuid}", headers.toString());
    }

    @Test
    public void sgovExceptionTest() throws IOException {
        SgovAuthInfo auth = new SgovAuthInfo(
            "{\"appId\":\"app\",\"credential\":\"cre\",\"sgovUrl\":\"http://127.0.0.1/test\"}");

        when(httpClientServiceMock.getModelRequestHttpClient(Mockito.anyString())).thenReturn(clientMock);

        CloseableHttpResponse responseMock = Mockito.mock(CloseableHttpResponse.class);
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<String> handler = invocation.getArgument(1); // 获取第二个参数，即ResponseHandler
                return handler.handleResponse(responseMock); // 执行Handler并返回其结果
            });

        when(responseMock.getCode()).thenReturn(200);

        PluginHisSgovAdapter adapter = new PluginHisSgovAdapter();
        try {
            adapter.requestHeaderHandler(auth, new HashMap<>());
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.PLUGIN_AUTH_DATA_INVALID, e.getErrorCode());
        }
    }

    @Test
    void testException() throws IOException {
        SgovAuthInfo auth =
            new SgovAuthInfo("{\"appId\":\"app\",\"credential\":\"cre\",\"sgovUrl\":\"http://127.0.0.1/test\"}");

        PluginHisSgovAdapter adapter = new PluginHisSgovAdapter();
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenThrow(new ConnectTimeoutException(""));
        try {
            adapter.requestHeaderHandler(auth, new HashMap<>());
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.PLUGIN_AUTH_DATA_INVALID, e.getErrorCode());
        }
    }

    @Test
    void test_PluginTransformImpl() {
        PluginTransformImpl pluginTransform = new PluginTransformImpl();
        pluginTransform.transformAuthInfo2HisIAMAuthInfo(new SecurityScheme());

        pluginTransform.transformAuthInfo2SgovAuthInfo(new SecurityScheme());
    }

    @Test
    public void sgovAuthTestException() throws IOException {
        SgovAuthInfo auth = new SgovAuthInfo(
                "{\"appId\":\"app\",\"credential\":\"cre\",\"sgovUrl\":\"http://127.0.0.1/test\"}");

        when(httpClientServiceMock.getModelRequestHttpClient(Mockito.anyString())).thenReturn(clientMock);

        CloseableHttpResponse responseMock = Mockito.mock(CloseableHttpResponse.class);
        when(clientMock.execute(any(), ArgumentMatchers.<HttpClientResponseHandler<String>>any()))
            .thenAnswer(invocation -> {
                HttpClientResponseHandler<String> handler = invocation.getArgument(1); // 获取第二个参数，即ResponseHandler
                return handler.handleResponse(responseMock); // 执行Handler并返回其结果
            });

        when(responseMock.getEntity()).thenReturn(httpEntity);

        when(responseMock.getCode()).thenReturn(200);

        PluginHisSgovAdapter adapter = new PluginHisSgovAdapter();
        try {
            adapter.requestHeaderHandler(auth, new HashMap<>());
        } catch (AgentStudioException e) {
            Assertions.assertEquals(StudioError.PLUGIN_AUTH_DATA_INVALID, e.getErrorCode());
        }
    }
}
