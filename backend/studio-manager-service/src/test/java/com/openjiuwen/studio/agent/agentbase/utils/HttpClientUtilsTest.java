/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
class HttpClientUtilsTest {
    @InjectMocks
    private HttpClientUtils httpClientUtils;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_getSafeTlsVersion_should_return_not_null() throws Exception {

        // When
        String[] result = HttpClientUtils.getSafeTlsVersion();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void test_getSafeTlsCiphers_should_return_not_null() throws Exception {

        // When
        String[] result = HttpClientUtils.getSafeTlsCiphers();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void test_getHttpClient_should_return_not_null() throws Exception {

        // When
        CloseableHttpClient result = HttpClientUtils.getHttpClient();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    public void testServerTrusted() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new HttpClientUtils.IgnoreVerifyTrustManager().checkServerTrusted(null, "RSA"));
    }

    @Test
    public void testCheckClientTrusted() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new HttpClientUtils.IgnoreVerifyTrustManager().checkClientTrusted(null, "RSA"));
    }

    @Test
    public void testGetAcceptedIssuers() {
        new HttpClientUtils.IgnoreVerifyTrustManager().getAcceptedIssuers();
    }

}