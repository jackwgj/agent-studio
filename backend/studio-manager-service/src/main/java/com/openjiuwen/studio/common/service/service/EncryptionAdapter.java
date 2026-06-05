/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.service;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EncryptionAdapter {

    @Autowired(required = false)
    private LegacyCryptoHandler legacyCryptoHandler;

    /**
     * 加密方法 显式传入 domainId
     */
    public String encrypt(String text, String domainId) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        return fallbackEncrypt(text);
    }

    /**
     * 解密方法 (单参数，自动获取上下文)
     */
    public String decrypt(String text) {
        return fallbackDecrypt(text);
    }

    /**
     * 解密方法 (双参数，万能方法)
     */
    public String decrypt(String text, String domainId) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        return fallbackDecrypt(text);
    }

    /**
     * 解密兜底方法（保留给无 DomainID 的异步老数据擦屁股）
     */
    private String fallbackDecrypt(String text) {
        if (legacyCryptoHandler != null) {
            try {
                return legacyCryptoHandler.decrypt(text);
            } catch (Exception e) {
                log.error("Legacy/SCC decryption failed, returning original text.", e);
                return text;
            }
        }
        return text;
    }

    private String fallbackEncrypt(String text) {
        try {
            return legacyCryptoHandler.encrypt(text);
        } catch (Exception exception) {
            log.debug("Fallback Encryption: No legacy handler found. Saving as plaintext.");
        }
        return text;
    }
}
