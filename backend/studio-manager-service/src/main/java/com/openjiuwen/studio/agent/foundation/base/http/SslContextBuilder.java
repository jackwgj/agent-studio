/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.base.http;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

@Slf4j
public class SslContextBuilder {

    /**
     * 根据配置构建SSLContext，支持：不校验；根据指定的信任证书库配置；根据JRE默认信任证书库配置
     *
     * @return SSLContext
     */
    public static SSLContext buildCommonSslContext(boolean sslEnabled) {
        if (sslEnabled) {
            return SSLContexts.createDefault();
        } else {
            return buildNoopSslContext();
        }
    }

    private static SSLContext buildNoopSslContext() {
        try {
            TrustStrategy trustStrategy = (x509Certificates, authType) -> true;
            return SSLContexts.custom().loadTrustMaterial(null, trustStrategy).build();
        } catch (GeneralSecurityException e) {
            log.error("Failed to buildNoopSslContext", e.getMessage(), e);
            throw new AgentBaseException(ErrorCode.SERVER_INTERNAL_ERROR, e);
        }
    }
}
