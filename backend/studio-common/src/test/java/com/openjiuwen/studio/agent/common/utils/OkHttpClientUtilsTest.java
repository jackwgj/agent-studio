/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Test;

class OkHttpClientUtilsTest {

    private final OkHttpClientUtils okHttpClientUtils = new OkHttpClientUtils();

    @Test
    void testGetSSLSocketFactory_NotNull() throws Exception {
        SSLSocketFactory factory = OkHttpClientUtils.SSLSocketClient.getSSLSocketFactory();
        assertNotNull(factory);
    }

    @Test
    void testGetHostnameVerifier_NonEmptyHostname() {
        HostnameVerifier verifier = OkHttpClientUtils.SSLSocketClient.getHostnameVerifier();
        assertTrue(verifier.verify("example.com", null));
    }

    @Test
    void testGetHostnameVerifier_EmptyHostname() {
        HostnameVerifier verifier = OkHttpClientUtils.SSLSocketClient.getHostnameVerifier();
        assertFalse(verifier.verify("", null));
    }

    @Test
    void testGetX509TrustManager_NotNull() {
        X509TrustManager trustManager = OkHttpClientUtils.SSLSocketClient.getX509TrustManager();
        assertNotNull(trustManager);
        assertNotNull(trustManager.getAcceptedIssuers());
    }

    private X509TrustManager getInnerTrustManager() throws Exception {
        Method method = OkHttpClientUtils.SSLSocketClient.class.getDeclaredMethod("getTrustManager");
        method.setAccessible(true);
        TrustManager[] trustManagers = (TrustManager[]) method.invoke(null);
        return (X509TrustManager) trustManagers[0];
    }

    @Test
    void testCheckClientTrusted_NullChain() throws Exception {
        X509TrustManager tm = getInnerTrustManager();
        assertThrows(IllegalArgumentException.class, () -> tm.checkClientTrusted(null, "RSA"));
    }

    @Test
    void testCheckClientTrusted_EmptyChain() throws Exception {
        X509TrustManager tm = getInnerTrustManager();
        assertThrows(IllegalArgumentException.class,
            () -> tm.checkClientTrusted(new X509Certificate[] {}, "RSA"));
    }

    @Test
    void testCheckServerTrusted_NullChain() throws Exception {
        X509TrustManager tm = getInnerTrustManager();
        assertThrows(IllegalArgumentException.class, () -> tm.checkServerTrusted(null, "RSA"));
    }

    @Test
    void testCheckServerTrusted_EmptyChain() throws Exception {
        X509TrustManager tm = getInnerTrustManager();
        assertThrows(IllegalArgumentException.class,
            () -> tm.checkServerTrusted(new X509Certificate[] {}, "RSA"));
    }

    @Test
    void testGetAcceptedIssuers() throws Exception {
        X509TrustManager tm = getInnerTrustManager();
        X509Certificate[] issuers = tm.getAcceptedIssuers();
        assertNotNull(issuers);
        assertEquals(0, issuers.length);
    }
}
