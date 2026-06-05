/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.crypt.Ciphers;

import org.springframework.stereotype.Component;

@Component
public class CryptoUtils {
    private static Ciphers ciphers;

    public CryptoUtils(Ciphers ciphersParam) {
        ciphers = ciphersParam;
    }

    /**
     * 加密
     *
     * @param plainText 待加密文本
     * @return 加密后的文本
     */
    public static String encrypt(String plainText) {
        return ciphers.encrypt(plainText);
    }

    /**
     * 解密
     *
     * @param cipher 加密后的文本
     * @return 解密后的文本
     */
    public static String decrypt(String cipher) {
        return ciphers.decrypt(cipher);
    }

    /**
     * 将scc密文转为kms密文
     *
     * @param cipher 原密文
     * @return 加密后的文本
     */
    public static String toKmsEncrypt(String cipher) {
        return cipher;
    }
}
