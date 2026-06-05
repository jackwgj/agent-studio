/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.rce.models.DatasourceStatusCheckRsp;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * DatasourceStatusCheckService测试类
 *
 */
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
@Slf4j
public class DatasourceStatusCheckServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<CryptoUtils> mockedCryptoUtils;

    @Autowired
    DatasourceStatusCheckService datasourceStatusCheckService;

    @MockitoBean
    private OkHttpClientUtils okHttpClientUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OkHttpClient okHttpClient;

    @BeforeAll
    public static void init() {
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(Constants.TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_USER_ID);

        mockedCryptoUtils = Mockito.mockStatic(CryptoUtils.class);
        mockedCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("decrypt");
        mockedCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("encrypt");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedCryptoUtils.close();
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(datasourceStatusCheckService, "okHttpClientUtils", okHttpClientUtils);
        ReflectionTestUtils.setField(datasourceStatusCheckService, "datasourceCheckTimeOut", 1);
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
    }

    @Test
    void testCreateDatasource() throws IOException {
        when(okHttpClientUtils.getHttpClient()).thenReturn(okHttpClient);
        Call call = Mockito.mock(Call.class);
        when(okHttpClient.newCall(any())).thenReturn(call);
        when(call.execute()).thenAnswer(invocationOnMock -> {
            throw new TimeoutException("bad request");
        });
        DatasourceStatusCheckRsp resp = datasourceStatusCheckService.datasourceStatusCheck("test_id");
        Assertions.assertEquals("数据库连接失败。", resp.getErrorMessage());
    }
}
