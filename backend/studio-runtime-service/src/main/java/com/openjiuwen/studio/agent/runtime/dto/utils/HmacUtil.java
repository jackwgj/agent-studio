/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.dto.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacUtil {
    private static final String DEFAULT_ALG = "HmacSHA256";
    private HmacUtil() {
    }

    public static String digest2Base64(String data, byte[] secretKey) throws GeneralSecurityException {
        byte[] macData = digest2Byte(data, secretKey);
        return StringUtils.newStringUtf8(Base64.encodeBase64(macData, false));
    }

    public static byte[] digest2Byte(String data, byte[] secretKey) throws GeneralSecurityException {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, DEFAULT_ALG);
        Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(bytes);
    }
}
