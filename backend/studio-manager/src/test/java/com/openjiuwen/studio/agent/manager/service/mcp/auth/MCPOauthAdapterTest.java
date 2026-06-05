/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.dto.auth.OauthInfo;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class MCPOauthAdapterTest extends BaseTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EncryptionAdapter encryptionAdapter;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OkHttpClientUtils okHttpClientUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OkHttpClient okHttpClient;

    @InjectMocks
    private McpOauthAdapter mCPOauthAdapter;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(mCPOauthAdapter, "okHttpClientUtils", okHttpClientUtils);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void testRequestHeaderHandler() throws Exception {
        // 模拟oauthInfo的属性
        OauthInfo oauthInfo = mock(OauthInfo.class);
        when(oauthInfo.getEndpointUrl()).thenReturn("http://example.com");
        when(oauthInfo.getClientId()).thenReturn("clientId");
        when(oauthInfo.getScope()).thenReturn("scope");
        when(encryptionAdapter.decrypt("clientSecret")).thenReturn("decryptedSecret");

        when(okHttpClientUtils.getHttpClient()).thenReturn(okHttpClient);
        Call call = Mockito.mock(Call.class);
        when(okHttpClient.newCall(any())).thenReturn(call);

        when(call.execute()).thenAnswer(invocationOnMock -> {
            Response response = mock(Response.class);
            when(response.code()).thenReturn(200);

            // 创建一个 ResponseBody 的 mock 对象
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
        // 模拟oauthInfo的属性
        OauthInfo oauthInfo = mock(OauthInfo.class);
        when(oauthInfo.getEndpointUrl()).thenReturn("http://example.com");
        when(oauthInfo.getClientId()).thenReturn("clientId");
        when(oauthInfo.getScope()).thenReturn("scope");
        when(encryptionAdapter.decrypt("clientSecret")).thenReturn("decryptedSecret");

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
}
