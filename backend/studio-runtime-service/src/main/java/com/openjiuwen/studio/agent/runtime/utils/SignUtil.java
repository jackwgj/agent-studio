/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 功能描述
 *
 */
public class SignUtil {

    public static final String STRING_AND = "&";

    public static final String STRING_EQUAL = "=";

    public static final String CLOUDSOA_FORMAT =
        SpringBeanUtils.getProperty("gm.algorithm.scas.signHeader", "CLOUDSOA-HMAC-SHA256")
            + " appid={0},timestamp={1},signature=\"{2}\"";

    public static final String sign = SpringBeanUtils.getProperty("gm.algorithm.scas.sign", "HmacSHA256");

    public static String signGet(String method, String url, String query, String body, String timestamp, String appId,
        String key) {
        String strBuilder = method + STRING_AND + url + STRING_AND + getCanonicalQueryString(query) + STRING_AND + body
            + STRING_AND + "appid" + STRING_EQUAL + appId + STRING_AND + "timestamp" + STRING_EQUAL + timestamp;
        return MessageFormat.format(CLOUDSOA_FORMAT, appId, timestamp, hmacShaEncrypt(strBuilder, key));
    }

    // HMAC算法计算认证码
    private static String hmacShaEncrypt(String encryptText, String encryptKey) {
        Mac mac;
        SecretKeySpec secretkey;
        byte[] hash;
        try {
            // 创建HMAC的Mac实例
            mac = Mac.getInstance(sign);
            // 生成密钥
            secretkey = new SecretKeySpec(null == encryptKey ? new byte[0] : Hex.decodeHex(encryptKey.toCharArray()),
                sign);
            // 初始化Mac实例，并设置密钥
            mac.init(secretkey);
            // 计算认证码
            hash = mac.doFinal(encryptText.getBytes(StandardCharsets.UTF_8));
        } catch (InvalidKeyException | NoSuchAlgorithmException | DecoderException e) {
            return null;
        }
        // base64 设置
        return Base64.getEncoder().encodeToString(hash);
    }

    private static String getCanonicalQueryString(String queryString) {
        SortedMap<String, String> params = new TreeMap<>();
        if (!StringUtils.isEmpty(queryString)) {
            String[] var2 = StringUtils.splitByWholeSeparatorPreserveAllTokens(queryString, STRING_AND);
            for (String pair : var2) {
                int idx = pair.indexOf(STRING_EQUAL);
                params.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return toString(params);
    }

    public static String toString(SortedMap<String, String> params) {
        StringBuilder sb = new StringBuilder(128);
        Map.Entry<String, String> item;
        String key;
        for (Iterator<Map.Entry<String, String>> var2 = params.entrySet().iterator(); var2.hasNext(); sb.append(key)
            .append(STRING_EQUAL)
            .append(item.getValue())) {
            item = var2.next();
            key = item.getKey();
            if (!sb.isEmpty()) {
                sb.append(STRING_AND);
            }
        }
        return sb.toString();
    }
}
