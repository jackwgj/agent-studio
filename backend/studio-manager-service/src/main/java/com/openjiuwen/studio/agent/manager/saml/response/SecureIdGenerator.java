/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.saml.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SecureIdGenerator {
    private static final Logger log = LoggerFactory.getLogger(SecureIdGenerator.class);
    private static final char[] CHAR_MAPPING =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final SecureRandom SECURE_RANDOM;

    static {
        SecureRandom secureRandom1;
        try {
            secureRandom1 = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            // 回退到默认的 SecureRandom
            secureRandom1 = new SecureRandom();
        }
        SECURE_RANDOM = secureRandom1;
    }

    public String generateSecureId() {
        byte[] bytes = new byte[20];

        SECURE_RANDOM.nextBytes(bytes);

        char[] chars = new char[40];
        for (int i = 0; i < bytes.length; i++) {
            int left = (bytes[i] >> 4) & 0x0f;
            int right = bytes[i] & 0x0f;
            chars[i * 2] = CHAR_MAPPING[left];
            chars[i * 2 + 1] = CHAR_MAPPING[right];
        }

        String result = "s2" + String.valueOf(chars);

        return result;
    }
}
