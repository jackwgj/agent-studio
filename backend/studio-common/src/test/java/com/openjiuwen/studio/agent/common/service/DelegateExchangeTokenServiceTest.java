/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.service;

import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DelegateExchangeTokenServiceTest {

    @Mock
    private OkHttpClientUtils okHttpClientUtils;

    @InjectMocks
    private DelegateExchangeTokenService service;

    @Test
    void testDelegateExchangeToken_Success() throws IOException {
        ReflectionTestUtils.setField(service, "opSvcProjectId", "op-proj-id");
        ReflectionTestUtils.setField(service, "itepDispatcherEndpoint", "http://dispatcher");
        ReflectionTestUtils.setField(service, "agencyTokenUrl", "/agency/%s/token");

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        Response mockResponse = mock(Response.class);

        when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);
        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.header("X-Subject-Token")).thenReturn("new-token-123");

        String result = service.delegateExchangeToken("proj-1", "dom-1", "op-token");
        assertEquals("new-token-123", result);
    }

    @Test
    void testDelegateExchangeToken_NoTokenHeader() throws IOException {
        ReflectionTestUtils.setField(service, "opSvcProjectId", "op-proj-id");
        ReflectionTestUtils.setField(service, "itepDispatcherEndpoint", "http://dispatcher");
        ReflectionTestUtils.setField(service, "agencyTokenUrl", "/agency/%s/token");

        OkHttpClient mockClient = mock(OkHttpClient.class);
        Call mockCall = mock(Call.class);
        Response mockResponse = mock(Response.class);

        when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);
        when(mockClient.newCall(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.header("X-Subject-Token")).thenReturn(null);

        String result = service.delegateExchangeToken("proj-1", "dom-1", "op-token");
        assertNull(result);
    }
}
