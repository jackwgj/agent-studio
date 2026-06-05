/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface ServiceProvider {
    String getIssuer();

    String getServiceUrl();

    PrivateKeyEntry getPrivateKeyEntry();

    PublicKey getPublicKey();

    Certificate getCertificate();

    /**
     * 消息摘要算法
     *
     * @return
     */
    String getDigestAlgorithm();

    /**
     * 签名算法
     *
     * @return
     */
    String getSignatureAlgorithm();
}
