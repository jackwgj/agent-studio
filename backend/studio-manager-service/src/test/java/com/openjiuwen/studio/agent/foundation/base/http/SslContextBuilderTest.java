/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

@ExtendWith(MockitoExtension.class)
public class SslContextBuilderTest {
    @BeforeEach
    void setUp() {
    }

    @Test
    void testBuildCommonSslContext_sslEnabled_true() {
        // Arrange
        try (MockedStatic<SSLContexts> mockedStatic = mockStatic(SSLContexts.class)) {
            SSLContext mockContext = mock(SSLContext.class);
            mockedStatic.when(SSLContexts::createDefault).thenReturn(mockContext);
            // Act
            SSLContext result = SslContextBuilder.buildCommonSslContext(true);
            // Assert
            assertNotNull(result);
            mockedStatic.verify(SSLContexts::createDefault);
        }
    }

    @Test
    void testBuildCommonSslContext_sslEnabled_false()
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContext mockContext = mock(SSLContext.class);
        SSLContextBuilder mockBuilder = mock(SSLContextBuilder.class);
        lenient().when(mockBuilder.loadTrustMaterial(nullable(KeyStore.class), any())).thenReturn(mockBuilder);
        lenient().when(mockBuilder.build()).thenReturn(mockContext);
        try (MockedStatic<SSLContexts> mockedStatic = mockStatic(SSLContexts.class)) {
            mockedStatic.when(SSLContexts::custom).thenReturn(mockBuilder);
            SSLContext result = SslContextBuilder.buildCommonSslContext(false);
            assertNotNull(result);
        }
    }
}
