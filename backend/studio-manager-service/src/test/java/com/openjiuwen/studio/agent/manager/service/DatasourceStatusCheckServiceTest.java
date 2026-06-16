/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.rce.models.DatasourceStatusCheckRsp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasourceStatusCheckServiceTest {

    @Mock
    private I18nUtil i18nUtil;

    @Mock
    private OkHttpClientUtils okHttpClientUtils;

    @InjectMocks
    private DatasourceStatusCheckService datasourceStatusCheckService;

    private ExecutorService fixedThreadPool;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(datasourceStatusCheckService, "agentRuntimeEndpoint", "http://localhost:8080");
        ReflectionTestUtils.setField(datasourceStatusCheckService, "datasourceCheckPoolSize", 2);
        ReflectionTestUtils.setField(datasourceStatusCheckService, "datasourceCheckTimeOut", 5);
        fixedThreadPool = Executors.newFixedThreadPool(2);
        ReflectionTestUtils.setField(datasourceStatusCheckService, "fixedThreadPool", fixedThreadPool);
    }

    @Test
    void testDatasourceStatusCheck_ConnectionFailed() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            okhttp3.OkHttpClient mockClient = mock(okhttp3.OkHttpClient.class);
            when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);

            okhttp3.Call mockCall = mock(okhttp3.Call.class);
            when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);
            when(mockCall.execute()).thenThrow(new java.io.IOException("Connection refused"));

            DatasourceStatusCheckRsp result = datasourceStatusCheckService.datasourceStatusCheck("ds1");
            assertNotNull(result);
            assertEquals("failed", result.getStatus());
        }
    }

    @Test
    void testDatasourceStatusCheck_Success() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            okhttp3.OkHttpClient mockClient = mock(okhttp3.OkHttpClient.class);
            when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);

            okhttp3.Call mockCall = mock(okhttp3.Call.class);
            when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);

            okhttp3.Response mockResponse = mock(okhttp3.Response.class);
            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            doNothing().when(mockResponse).close();

            DatasourceStatusCheckRsp result = datasourceStatusCheckService.datasourceStatusCheck("ds1");
            assertNotNull(result);
            assertEquals("success", result.getStatus());
        }
    }

    @Test
    void testDatasourceStatusCheck_FailedWithBody() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            okhttp3.OkHttpClient mockClient = mock(okhttp3.OkHttpClient.class);
            when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);

            okhttp3.Call mockCall = mock(okhttp3.Call.class);
            when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);

            okhttp3.Response mockResponse = mock(okhttp3.Response.class);
            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(false);
            doNothing().when(mockResponse).close();

            okhttp3.ResponseBody mockBody = mock(okhttp3.ResponseBody.class);
            when(mockResponse.body()).thenReturn(mockBody);
            when(mockBody.string()).thenReturn("{\"error_msg\":\"connection refused\"}");

            DatasourceStatusCheckRsp result = datasourceStatusCheckService.datasourceStatusCheck("ds1");
            assertNotNull(result);
            assertEquals("failed", result.getStatus());
            assertEquals("connection refused", result.getErrorMessage());
        }
    }

    @Test
    void testDatasourceStatusCheck_FailedWithNullBody() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            okhttp3.OkHttpClient mockClient = mock(okhttp3.OkHttpClient.class);
            when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);

            okhttp3.Call mockCall = mock(okhttp3.Call.class);
            when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);

            okhttp3.Response mockResponse = mock(okhttp3.Response.class);
            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(false);
            when(mockResponse.body()).thenReturn(null);
            when(mockResponse.code()).thenReturn(500);
            doNothing().when(mockResponse).close();

            DatasourceStatusCheckRsp result = datasourceStatusCheckService.datasourceStatusCheck("ds1");
            assertNotNull(result);
            assertEquals("failed", result.getStatus());
            assertTrue(result.getErrorMessage().contains("500"));
        }
    }

    @Test
    void testRetrieveDatasourceBySql_Success() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            okhttp3.OkHttpClient mockClient = mock(okhttp3.OkHttpClient.class);
            when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);

            okhttp3.Call mockCall = mock(okhttp3.Call.class);
            when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);

            okhttp3.Response mockResponse = mock(okhttp3.Response.class);
            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(true);
            doNothing().when(mockResponse).close();

            okhttp3.ResponseBody mockBody = mock(okhttp3.ResponseBody.class);
            when(mockResponse.body()).thenReturn(mockBody);
            when(mockBody.string()).thenReturn("{\"output_list\":[[\"val1\"]],\"row_num\":1}");

            var result = datasourceStatusCheckService.retrieveDatasourceBySql("ds1", "select * from t", null);
            assertNotNull(result);
            assertEquals(1, result.getRowNum());
        }
    }

    @Test
    void testRetrieveDatasourceBySql_Failed() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStatic = mockStatic(RequestContextUtils.class)) {
            mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("token");

            okhttp3.OkHttpClient mockClient = mock(okhttp3.OkHttpClient.class);
            when(okHttpClientUtils.getHttpClient()).thenReturn(mockClient);

            okhttp3.Call mockCall = mock(okhttp3.Call.class);
            when(mockClient.newCall(any(okhttp3.Request.class))).thenReturn(mockCall);

            okhttp3.Response mockResponse = mock(okhttp3.Response.class);
            when(mockCall.execute()).thenReturn(mockResponse);
            when(mockResponse.isSuccessful()).thenReturn(false);
            doNothing().when(mockResponse).close();

            okhttp3.ResponseBody mockBody = mock(okhttp3.ResponseBody.class);
            when(mockResponse.body()).thenReturn(mockBody);
            when(mockBody.toString()).thenReturn("error");

            assertThrows(AgentStudioException.class, () ->
                datasourceStatusCheckService.retrieveDatasourceBySql("ds1", "select * from t", null));
        }
    }
}
